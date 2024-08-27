/*
 * MIT License
 *
 * Copyright 2023-present, Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.shopify.checkoutsheetkit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.InstrumentationType.histogram
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

internal class CheckoutWebView(context: Context, attributeSet: AttributeSet? = null) :
    BaseWebView(context, attributeSet) {

    override val recoverErrors = true
    override val variant = "standard"
    override val cspSchema = CheckoutBridge.SCHEMA_VERSION_NUMBER
    var isPreload = false

    private val checkoutBridge = CheckoutBridge(CheckoutWebViewEventProcessor(NoopEventProcessor()))
    private var loadComplete = false
        set(value) {
            field = value
            dispatchWhenPresentedAndLoaded(value, presented)
        }
    private var presented = false
        set(value) {
            field = value
            dispatchWhenPresentedAndLoaded(loadComplete, value)
        }

    private fun dispatchWhenPresentedAndLoaded(loadComplete: Boolean, hasBeenPresented: Boolean) {
        if (loadComplete && hasBeenPresented) {
            checkoutBridge.sendMessage(this, CheckoutBridge.SDKOperation.Presented)
        }
    }

    private var initLoadTime: Long = -1

    init {
        webViewClient = CheckoutWebViewClient()
        addJavascriptInterface(checkoutBridge, JAVASCRIPT_INTERFACE_NAME)
        settings.userAgentString = "${settings.userAgentString} ${userAgentSuffix()}"
    }

    fun hasFinishedLoading() = loadComplete

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        checkoutBridge.setEventProcessor(eventProcessor)
    }

    fun notifyPresented() {
        presented = true
    }

    override fun getEventProcessor(): CheckoutWebViewEventProcessor {
        return checkoutBridge.getEventProcessor()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addJavascriptInterface(checkoutBridge, JAVASCRIPT_INTERFACE_NAME)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeJavascriptInterface(JAVASCRIPT_INTERFACE_NAME)
    }

    fun loadCheckout(url: String, isPreload: Boolean) {
        initLoadTime = System.currentTimeMillis()
        this.isPreload = isPreload
        Handler(Looper.getMainLooper()).post {
            val headers = mutableMapOf(
                COLOR_SCHEME_HEADER to ShopifyCheckoutSheetKit.configuration.colorScheme.id
            )
            if (isPreload) {
                headers["Sec-Purpose"] = "prefetch"
            }
            loadUrl(url, headers)
        }
    }

    inner class CheckoutWebViewClient : BaseWebView.BaseWebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            checkoutBridge.getEventProcessor().onCheckoutViewLoadStarted()
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            loadComplete = true
            val timeToLoad = System.currentTimeMillis() - initLoadTime
            checkoutBridge.sendMessage(view, CheckoutBridge.SDKOperation.Instrumentation(
                InstrumentationPayload(
                    name= "checkout_finished_loading",
                    value= timeToLoad,
                    type = histogram,
                    tags = mapOf("preloading" to isPreload.toString()),
                )
            ))
            getEventProcessor().onCheckoutViewLoadComplete()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                if (request.hasExternalAnnotation() || request.url?.isContactLink() == true) {
                    checkoutBridge.getEventProcessor().onCheckoutViewLinkClicked(request.trimmedUri())
                    return true
                }
                if (request.isForMainFrame) {
                    val requestHeaders = request.requestHeaders?.toMutableMap() ?: mutableMapOf()
                    if (requestHeaders[COLOR_SCHEME_HEADER] == null) {
                        requestHeaders[COLOR_SCHEME_HEADER] = ShopifyCheckoutSheetKit.configuration.colorScheme.id
                        loadUrl(request.url.toString(), requestHeaders)
                        return true
                    }
                }
            }

            return false
        }

        private fun WebResourceRequest.hasExternalAnnotation(): Boolean {
            if (!this.url.isWebLink()) {
                return false
            }
            val openExternallyParam = this.url.getQueryParameter(OPEN_EXTERNALLY_PARAM)
            return setOf("true", "1").contains(openExternallyParam?.lowercase()?.trim())
        }

        private fun WebResourceRequest.trimmedUri(): Uri {
            if (!setOf(Scheme.HTTP, Scheme.HTTPS).contains(this.url.scheme)) {
                return this.url
            }

            val trimmedUri = Uri.Builder()
                .scheme(this.url.scheme)
                .authority(this.url.authority)
                .path(this.url.path)

            this.url.queryParameterNames.forEach {
                if (it != OPEN_EXTERNALLY_PARAM) {
                    trimmedUri.appendQueryParameter(it, this.url.getQueryParameter(it))
                }
            }

            return trimmedUri.build()
        }

        private fun Uri?.isWebLink(): Boolean = setOf(Scheme.HTTP, Scheme.HTTPS).contains(this?.scheme)
        private fun Uri?.isMailtoLink(): Boolean = this?.scheme == Scheme.MAILTO
        private fun Uri?.isTelLink(): Boolean = this?.scheme == Scheme.TEL
        private fun Uri?.isContactLink(): Boolean = this.isMailtoLink() || this.isTelLink()
    }

    companion object {
        private object Scheme {
            const val HTTP = "http"
            const val HTTPS = "https"
            const val TEL = "tel"
            const val MAILTO = "mailto"
        }

        private const val OPEN_EXTERNALLY_PARAM = "open_externally"
        private const val JAVASCRIPT_INTERFACE_NAME = "CheckoutSheetProtocolConsumer"
        private const val COLOR_SCHEME_HEADER = "Sec-CH-Prefers-Color-Scheme"

        internal var cacheEntry: CheckoutWebViewCacheEntry? = null
        internal var cacheClock = CheckoutWebViewCacheClock()

        fun markCacheEntryStale() {
            cacheEntry = cacheEntry?.copy(timeout = -1)
        }

        fun clearCache(newEntry: CheckoutWebViewCacheEntry? = null) = cacheEntry?.let {
            Handler(Looper.getMainLooper()).post {
                it.view.destroy()
                cacheEntry = newEntry
            }
        }

        fun cacheableCheckoutView(
            url: String,
            activity: ComponentActivity,
            isPreload: Boolean = false,
        ): CheckoutWebView {
            var view: CheckoutWebView? = null
            val countDownLatch = CountDownLatch(1)

            activity.runOnUiThread {
                view = fetchView(url, activity, isPreload)
                countDownLatch.countDown()
            }

            countDownLatch.await()

            return view!!
        }

        private fun fetchView(
            url: String,
            activity: ComponentActivity,
            isPreload: Boolean,
        ): CheckoutWebView {
            val preloadingEnabled = ShopifyCheckoutSheetKit.configuration.preloading.enabled
            if (!preloadingEnabled || cacheEntry?.isValid(url) != true) {
                val view = CheckoutWebView(activity as Context).apply {
                    loadCheckout(url, isPreload)
                    if (isPreload) {
                        // Pauses processing that can be paused safely (e.g. geolocation, animations), but not JavaScript / network requests
                        // https://developer.android.com/reference/android/webkit/WebView#onPause()
                        onPause()
                    }
                }

                setCacheEntry(
                    CheckoutWebViewCacheEntry(
                        key = url,
                        view = view,
                        clock = cacheClock,
                        timeout = if (isPreload && preloadingEnabled) 5.minutes.inWholeMilliseconds else 0,
                    )
                )

                return view
            }

            return cacheEntry!!.view
        }

        private fun setCacheEntry(cacheEntry: CheckoutWebViewCacheEntry) {
            if (this.cacheEntry == null) {
                this.cacheEntry = cacheEntry
            } else {
                clearCache(cacheEntry)
            }
        }
    }

    internal data class CheckoutWebViewCacheEntry(
        val key: String,
        val view: CheckoutWebView,
        val clock: CheckoutWebViewCacheClock,
        val timeout: Long,
    ) {
        private val timestamp = clock.currentTimeMillis()

        fun isValid(key: String): Boolean {
            return key == cacheEntry!!.key && !cacheEntry!!.isStale
        }

        internal val isStale: Boolean
            get() = abs(timestamp - clock.currentTimeMillis()) >= timeout
    }

    internal class CheckoutWebViewCacheClock {
        fun currentTimeMillis(): Long = System.currentTimeMillis()
    }
}

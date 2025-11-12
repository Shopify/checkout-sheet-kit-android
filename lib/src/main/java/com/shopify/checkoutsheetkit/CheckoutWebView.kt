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
import androidx.core.net.toUri
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

internal class CheckoutWebView(context: Context, attributeSet: AttributeSet? = null) :
    BaseWebView(context, attributeSet) {

    override val recoverErrors = true
    var isPreload = false
    override var checkoutOptions: CheckoutOptions? = null

    private val checkoutBridge = CheckoutBridge(CheckoutWebViewEventProcessor(NoopEventProcessor()))
    private var loadComplete = false
        set(value) {
            log.d(LOG_TAG, "Setting loadComplete to $value.")
            field = value
        }
    private var presented = false
        set(value) {
            log.d(LOG_TAG, "Setting presented to $value.")
            field = value
        }

    init {
        webViewClient = CheckoutWebViewClient()
        addJavascriptInterface(checkoutBridge, JAVASCRIPT_INTERFACE_NAME)
        checkoutBridge.setWebView(this)
    }

    fun hasFinishedLoading() = loadComplete

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        log.d(LOG_TAG, "Setting event processor $eventProcessor.")
        checkoutBridge.setEventProcessor(eventProcessor)
    }

    fun notifyPresented() {
        log.d(LOG_TAG, "Notify presented called.")
        presented = true
    }

    override fun getEventProcessor(): CheckoutWebViewEventProcessor {
        return checkoutBridge.getEventProcessor()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        log.d(LOG_TAG, "Attached to window. Adding JavaScript interface with name $JAVASCRIPT_INTERFACE_NAME.")
        addJavascriptInterface(checkoutBridge, JAVASCRIPT_INTERFACE_NAME)
        checkoutBridge.setWebView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        log.d(LOG_TAG, "Detached from window. Removing JavaScript interface with name $JAVASCRIPT_INTERFACE_NAME.")
        removeJavascriptInterface(JAVASCRIPT_INTERFACE_NAME)
        checkoutBridge.setWebView(null)
    }

    fun loadCheckout(url: String, isPreload: Boolean, options: CheckoutOptions? = null) {
        log.d(LOG_TAG, "Loading checkout with url $url. IsPreload: $isPreload.")
        this.isPreload = isPreload
        this.checkoutOptions = options
        Handler(Looper.getMainLooper()).post {
            val headers = if (isPreload) mutableMapOf("Shopify-Purpose" to "prefetch") else mutableMapOf()
            val includeAuthentication = authenticationTracker.shouldSendToken(checkoutOptions?.authToken)

            loadUrl(
                url.toUri().withEmbedParam(
                    options = checkoutOptions,
                    includeAuthentication = includeAuthentication
                ),
                headers
            )
        }
    }

    inner class CheckoutWebViewClient : BaseWebView.BaseWebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            log.d(LOG_TAG, "onPageStarted called $url.")
            checkoutBridge.getEventProcessor().onCheckoutViewLoadStarted()
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            log.d(LOG_TAG, "onPageFinished called $url.")
            authenticationTracker.confirmTokenSent()
            loadComplete = true
            getEventProcessor().onCheckoutViewLoadComplete()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (
                request?.hasExternalAnnotation() == true ||
                request?.url?.isContactLink() == true ||
                request?.url?.isDeepLink() == true
            ) {
                log.d(LOG_TAG, "Overriding URL loading to invoke onCheckoutLinkClicked for request: $request.")
                checkoutBridge.getEventProcessor().onCheckoutViewLinkClicked(request.trimmedUri())
                return true
            }

            return super.shouldOverrideUrlLoading(view, request)
        }

        private fun WebResourceRequest.hasExternalAnnotation(): Boolean {
            if (!this.url.isWebLink()) {
                return false
            }
            val openExternallyParam = this.url.getQueryParameter(OPEN_EXTERNALLY_PARAM)
            val hasOpenExternallyParam = setOf("true", "1").contains(openExternallyParam?.lowercase()?.trim())
            log.d(LOG_TAG, "open_externally param found on request URL.")
            return hasOpenExternallyParam
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
    }

    companion object {
        private const val LOG_TAG = "CheckoutWebView"
        private const val OPEN_EXTERNALLY_PARAM = "open_externally"
        private const val JAVASCRIPT_INTERFACE_NAME = "EmbeddedCheckoutProtocolConsumer"

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
            options: CheckoutOptions? = null,
        ): CheckoutWebView {
            var view: CheckoutWebView? = null
            val countDownLatch = CountDownLatch(1)

            activity.runOnUiThread {
                view = fetchView(url, activity, isPreload, options)
                countDownLatch.countDown()
            }

            countDownLatch.await()

            return view!!
        }

        private fun fetchView(
            url: String,
            activity: ComponentActivity,
            isPreload: Boolean,
            options: CheckoutOptions?,
        ): CheckoutWebView {
            val preloadingEnabled = ShopifyCheckoutSheetKit.configuration.preloading.enabled
            log.d(
                LOG_TAG,
                "Fetch view called for url $url. Is preload: $isPreload. Preloading enabled: $preloadingEnabled."
            )
            if (!preloadingEnabled || cacheEntry?.isValid(url) != true) {
                log.d(LOG_TAG, "Constructing new CheckoutWebView and calling loadCheckout.")
                val view = CheckoutWebView(activity as Context).apply {
                    loadCheckout(url, isPreload, options)
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

            log.d(LOG_TAG, "Returning previously cached view.")
            return cacheEntry!!.view
        }

        private fun setCacheEntry(cacheEntry: CheckoutWebViewCacheEntry) {
            if (this.cacheEntry == null) {
                log.d(
                    LOG_TAG,
                    "Caching CheckoutWebView with TTL: ${cacheEntry.timeout} (note: a TTL of 0 is equivalent to not caching)."
                )

                this.cacheEntry = cacheEntry
            } else {
                log.d(LOG_TAG, "Clearing WebView cache and destroying cached view.")
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
            val valid = this.key == key && !isStale
            log.d(LOG_TAG, "Checking cache entry validity. Is valid: $valid.")
            return valid
        }

        internal val isStale: Boolean
            get() {
                val staleResult = abs(timestamp - clock.currentTimeMillis()) >= timeout
                log.d(LOG_TAG, "Checking cache entry staleness. Is stale: $staleResult.")
                return staleResult
            }
    }

    internal class CheckoutWebViewCacheClock {
        fun currentTimeMillis(): Long = System.currentTimeMillis()
    }
}

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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.CheckoutBridge.Companion.userAgentSuffix
import com.shopify.checkoutsheetkit.InstrumentationType.histogram
import java.net.HttpURLConnection.HTTP_GONE
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

@SuppressLint("SetJavaScriptEnabled")
internal class CheckoutWebView(context: Context, attributeSet: AttributeSet? = null) :
    WebView(context, attributeSet) {

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
        configureWebView()
    }

    fun hasFinishedLoading() = loadComplete

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        checkoutBridge.setEventProcessor(eventProcessor)
    }

    fun notifyPresented() {
        presented = true
    }

    private fun configureWebView() {
        visibility = VISIBLE
        settings.apply {
            userAgentString = "${settings.userAgentString} ${userAgentSuffix()}"
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                checkoutBridge.getEventProcessor().updateProgressBar(newProgress)
            }
        }
        isHorizontalScrollBarEnabled = false
        webViewClient = CheckoutWebViewClient()
        requestDisallowInterceptTouchEvent(true)
        setBackgroundColor(TRANSPARENT)
        addJavascriptInterface(checkoutBridge, JAVASCRIPT_INTERFACE_NAME)
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        id = View.generateViewId()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
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
        Handler(Looper.getMainLooper()).post {
            val headers = if (isPreload) mapOf("Sec-Purpose" to "prefetch") else emptyMap()
            loadUrl(url, headers)
        }
    }

    inner class CheckoutWebViewClient : WebViewClient() {
        init {
            if (BuildConfig.DEBUG) {
                setWebContentsDebuggingEnabled(true)
            }
        }

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
                    "checkout_finished_loading", timeToLoad, histogram, mapOf()
                )
            ))
            checkoutBridge.getEventProcessor().onCheckoutViewLoadComplete()
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !detail.didCrash()) {
                // Renderer was killed because system ran out of memory.

                // Removing the view from `CheckoutWebViewContainer will trigger a cache clear
                // and call webView.destroy()
                (view.parent as ViewGroup).removeView(view)
                true
            } else {
                false
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.hasExternalAnnotation() == true || request?.url?.isContactLink() == true) {
                checkoutBridge.getEventProcessor().onCheckoutViewLinkClicked(request.trimmedUri())
                return true
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

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (error != null) {
                handleError(
                    request,
                    error.errorCode,
                    error.description.toString()
                )
            }
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (errorResponse != null) {
                handleError(
                    request,
                    errorResponse.statusCode,
                    errorResponse.reasonPhrase
                )
            }
        }

        private fun handleError(
            request: WebResourceRequest?,
            errorCode: Int,
            errorDescription: String
        ) {
            if (request?.isForMainFrame == true) {
                val exception = when (errorCode) {
                    HTTP_NOT_FOUND -> CheckoutLiquidNotMigratedException()
                    HTTP_GONE -> CheckoutExpiredException()
                    HTTP_INTERNAL_ERROR -> CheckoutUnavailableException()
                    else -> CheckoutSdkError(errorDescription)
                }
                checkoutBridge.getEventProcessor().onCheckoutViewFailedWithError(exception)
            }
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
        private const val JAVASCRIPT_INTERFACE_NAME = "android"

        internal var cacheEntry: CheckoutWebViewCacheEntry? = null
        internal var cacheClock = CheckoutWebViewCacheClock()

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
                }

                setCacheEntry(
                    CheckoutWebViewCacheEntry(
                        key = url,
                        view = view,
                        clock = cacheClock,
                        timeout = if (preloadingEnabled) 5.minutes.inWholeMilliseconds else 0,
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

        private val isStale: Boolean
            get() = abs(timestamp - clock.currentTimeMillis()) >= timeout
    }

    internal class CheckoutWebViewCacheClock {
        fun currentTimeMillis(): Long = System.currentTimeMillis()
    }
}

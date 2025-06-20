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
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import java.net.HttpURLConnection.HTTP_GONE
import java.net.HttpURLConnection.HTTP_NOT_FOUND

@SuppressLint("SetJavaScriptEnabled")
internal abstract class BaseWebView(context: Context, attributeSet: AttributeSet? = null) :
    WebView(context, attributeSet) {

    init {
        configureWebView()
    }

    abstract fun getEventProcessor(): CheckoutWebViewEventProcessor
    abstract val recoverErrors: Boolean
    abstract val cspSchema: String

    private fun configureWebView() {
        visibility = VISIBLE
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
            WebSettingsCompat.setPaymentRequestEnabled(settings, true)
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                log.d(LOG_TAG, "On progress change called. New progress $newProgress.")
                getEventProcessor().updateProgressBar(newProgress)
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                log.d(LOG_TAG, "onGeolocationPermissionsShowPrompt called, origin $origin, invoking eventProcessor callback.")
                getEventProcessor().onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onGeolocationPermissionsHidePrompt() {
                log.d(LOG_TAG, "onGeolocationPermissionsHidePrompt called, invoking eventProcessor callback.")
                getEventProcessor().onGeolocationPermissionsHidePrompt()
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                log.d(LOG_TAG, "onPermissionRequest called $request, invoking eventProcessor callback.")
                getEventProcessor().onPermissionRequest(request)
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams,
            ): Boolean {
                log.d(LOG_TAG, "onShowFileChooser called, invoking eventProcessor callback.")
                return getEventProcessor().onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
        }
        isHorizontalScrollBarEnabled = false
        requestDisallowInterceptTouchEvent(true)
        setBackgroundColor(TRANSPARENT)
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        id = View.generateViewId()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            log.d(LOG_TAG, "Back key event detected, navigating back.")
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    internal fun userAgentSuffix(): String {
        val platform = ShopifyCheckoutSheetKit.configuration.platform
        val platformPart = if (platform != null) "(${platform.displayName})" else "(Android)"
        val version = ShopifyCheckoutSheetKit.version.split("-").first()
        val suffix = "CheckoutKit/$version $platformPart CheckoutSheetProtocol/$cspSchema"
        log.d(LOG_TAG, "Setting User-Agent suffix $suffix")
        return suffix
    }

    open inner class BaseWebViewClient : WebViewClient() {
        init {
            if (BuildConfig.DEBUG) {
                log.d(LOG_TAG, "Setting web contents debugging enabled.")
                setWebContentsDebuggingEnabled(true)
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.isForMainFrame == true && request.url?.isWebLink() == true) {
                var headers = request.requestHeaders ?: mutableMapOf()
                var shouldOverride = false

                val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
                val shouldHaveColorSchemeHeader = colorScheme !is ColorScheme.Web && colorScheme !is ColorScheme.Automatic
                if (shouldHaveColorSchemeHeader && !headers.hasColorSchemeHeader()) {
                    headers = headers.withColorScheme()
                    shouldOverride = true
                }
                if (!headers.hasBrandingHeader()) {
                    headers = headers.withBranding()
                    shouldOverride = true
                }

                if (shouldOverride) {
                    view?.loadUrl(request.url.toString(), headers)
                    return true
                }
            }
            return false
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !detail.didCrash()) {
                // Renderer was killed because system ran out of memory.
                log.d(LOG_TAG, "onRenderProcessGone called, calling onCheckoutFailedWithError")
                val eventProcessor = getEventProcessor()
                eventProcessor.onCheckoutViewFailedWithError(
                    CheckoutSheetKitException(
                        errorDescription = "Render process gone.",
                        errorCode = CheckoutSheetKitException.RENDER_PROCESS_GONE,
                        isRecoverable = recoverErrors,
                    )
                )
                true
            } else {
                false
            }
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
                    errorResponse.reasonPhrase.ifBlank { "HTTP ${errorResponse.statusCode} Error" },
                    errorResponse.responseHeaders,
                )
            }
        }

        internal open fun isRecoverable(statusCode: Int): Boolean {
            return when (statusCode) {
                TOO_MANY_REQUESTS -> recoverErrors
                ERROR_BAD_URL -> false
                in CLIENT_ERROR -> false
                else -> recoverErrors
            }
        }

        private fun handleError(
            request: WebResourceRequest?,
            errorCode: Int,
            errorDescription: String,
            responseHeaders: MutableMap<String, String> = mutableMapOf(),
        ) {
            if (request?.isForMainFrame == true) {
                log.d(
                    LOG_TAG,
                    "Handling error for main frame. URL: ${request.url}, errorCode: $errorCode, errorDescription: $errorDescription"
                )
                val processor = getEventProcessor()
                when {
                    errorCode == HTTP_NOT_FOUND && responseHeaders[DEPRECATED_REASON_HEADER]?.lowercase() == LIQUID_NOT_SUPPORTED -> {
                        log.d(LOG_TAG, "Failing with liquid not supported error. Recoverable: false")
                        processor.onCheckoutViewFailedWithError(
                            ConfigurationException(
                                errorDescription = "Storefronts using checkout.liquid are not supported. Please upgrade to Checkout " +
                                    "Extensibility.",
                                errorCode = ConfigurationException.CHECKOUT_LIQUID_NOT_MIGRATED,
                                isRecoverable = false,
                            )
                        )
                    }

                    errorCode == HTTP_GONE -> {
                        log.d(LOG_TAG, "Failing with cart expired. Recoverable: false")
                        processor.onCheckoutViewFailedWithError(
                            CheckoutExpiredException(
                                isRecoverable = false,
                                errorCode = CheckoutExpiredException.CART_EXPIRED
                            ),
                        )
                    }

                    else -> {
                        val recoverable = isRecoverable(errorCode)
                        log.d(LOG_TAG, "Failing with other error. Code: $errorCode. Recoverable $recoverable")
                        processor.onCheckoutViewFailedWithError(
                            HttpException(
                                errorDescription = errorDescription,
                                statusCode = errorCode,
                                isRecoverable = recoverable
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG = "BaseWebView"
        private const val DEPRECATED_REASON_HEADER = "X-Shopify-API-Deprecated-Reason"
        private const val LIQUID_NOT_SUPPORTED = "checkout_liquid_not_supported"

        private const val TOO_MANY_REQUESTS = 429
        private val CLIENT_ERROR = 400..499
    }
}

/**
 * Removes the WebView from its parent if a parent exists
 */
internal fun BaseWebView.removeFromParent() {
    val parent = this.parent
    if (parent is ViewGroup) {
        log.d("BaseWebView", "Existing parent found for WebView, removing.")
        // Ensure view is not destroyed when removing from parent
        CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.YES
        parent.removeView(this)
    }
}

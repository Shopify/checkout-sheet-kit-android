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

import android.net.Uri
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutErrorCode
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutErrorEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent

/**
 * Event processor that can handle events internally, delegate to the CheckoutEventProcessor
 * passed into ShopifyCheckoutSheetKit.present(), or preprocess arguments and then delegate
 *
 * @constructor Internal constructor with UI callbacks for dialog mode.
 * External users should use the public constructor which only requires an EventProcessor.
 */
public class CheckoutWebViewEventProcessor internal constructor(
    private val eventProcessor: CheckoutEventProcessor,
    private val toggleHeader: (Boolean) -> Unit = {},
    private val checkoutErrorInterceptor: (CheckoutException) -> Unit = { CheckoutWebView.clearCacheInternal() },
    private val setProgressBarVisibility: (Int) -> Unit = {},
    private val updateProgressBarPercentage: (Int) -> Unit = {},
) {
    /**
     * Public constructor for external use (e.g., inline checkout mode).
     * Internal UI callbacks will use sensible defaults.
     */
    public constructor(eventProcessor: CheckoutEventProcessor) : this(
        eventProcessor = eventProcessor,
        toggleHeader = {},
        checkoutErrorInterceptor = {
            CheckoutWebView.clearCacheInternal()
            eventProcessor.onFail(it)
        },
        setProgressBarVisibility = {},
        updateProgressBarPercentage = {}
    )

    internal fun onCheckoutViewStart(checkoutStartEvent: CheckoutStartEvent) {
        log.d(LOG_TAG, "Calling onCheckoutStarted $checkoutStartEvent.")
        eventProcessor.onStart(checkoutStartEvent)
    }

    internal fun onCheckoutViewComplete(checkoutCompleteEvent: CheckoutCompleteEvent) {
        log.d(LOG_TAG, "Clearing WebView cache after checkout completion.")
        CheckoutWebView.markCacheEntryStale()

        log.d(LOG_TAG, "Calling onCheckoutCompleted $checkoutCompleteEvent.")
        eventProcessor.onComplete(checkoutCompleteEvent)
    }

    internal fun onCheckoutViewError(checkoutErrorEvent: CheckoutErrorEvent) {
        log.d(LOG_TAG, "Received checkout.error: ${checkoutErrorEvent.code} - ${checkoutErrorEvent.message}")

        val exception: CheckoutException = when (checkoutErrorEvent.code) {
            CheckoutErrorCode.STOREFRONT_PASSWORD_REQUIRED,
            CheckoutErrorCode.CUSTOMER_ACCOUNT_REQUIRED,
            CheckoutErrorCode.INVALID_PAYLOAD,
            CheckoutErrorCode.INVALID_SIGNATURE,
            CheckoutErrorCode.NOT_AUTHORIZED,
            CheckoutErrorCode.PAYLOAD_EXPIRED -> {
                log.e(LOG_TAG, "Configuration error: ${checkoutErrorEvent.message}, code: ${checkoutErrorEvent.code}")
                ConfigurationException(
                    errorDescription = checkoutErrorEvent.message,
                    errorCode = checkoutErrorEvent.code.name,
                    isRecoverable = false
                )
            }

            CheckoutErrorCode.CART_COMPLETED,
            CheckoutErrorCode.INVALID_CART -> {
                log.d(LOG_TAG, "Checkout expired: ${checkoutErrorEvent.message}, code: ${checkoutErrorEvent.code}")
                CheckoutExpiredException(
                    errorDescription = checkoutErrorEvent.message,
                    errorCode = checkoutErrorEvent.code.name,
                    isRecoverable = false
                )
            }

            CheckoutErrorCode.KILLSWITCH_ENABLED,
            CheckoutErrorCode.UNRECOVERABLE_FAILURE,
            CheckoutErrorCode.POLICY_VIOLATION,
            CheckoutErrorCode.VAULTED_PAYMENT_ERROR -> {
                log.e(LOG_TAG, "Checkout unavailable: ${checkoutErrorEvent.message}, code: ${checkoutErrorEvent.code}")
                ClientException(
                    errorDescription = checkoutErrorEvent.message,
                    errorCode = checkoutErrorEvent.code.name,
                    isRecoverable = false
                )
            }
        }

        onCheckoutViewFailedWithError(exception)
    }

    internal fun onCheckoutViewModalToggled(modalVisible: Boolean) {
        onMainThread {
            toggleHeader(modalVisible)
        }
    }

    internal fun onCheckoutViewLinkClicked(uri: Uri) {
        log.d(LOG_TAG, "Calling onCheckoutLinkClicked.")
        eventProcessor.onLinkClick(uri)
    }

    internal fun onCheckoutViewFailedWithError(error: CheckoutException) {
        onMainThread {
            checkoutErrorInterceptor(error)
        }
    }

    internal fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        return eventProcessor.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    internal fun onGeolocationPermissionsHidePrompt() {
        return eventProcessor.onGeolocationPermissionsHidePrompt()
    }

    internal fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams,
    ): Boolean {
        return eventProcessor.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }

    internal fun onPermissionRequest(permissionRequest: PermissionRequest) {
        onMainThread {
            eventProcessor.onPermissionRequest(permissionRequest)
        }
    }

    internal fun onCheckoutViewLoadComplete() {
        onMainThread {
            setProgressBarVisibility(INVISIBLE)
        }
    }

    internal fun updateProgressBar(progress: Int) {
        onMainThread {
            updateProgressBarPercentage(progress)
        }
    }

    internal fun onCheckoutViewLoadStarted() {
        onMainThread {
            setProgressBarVisibility(VISIBLE)
        }
    }

    internal fun onCheckoutViewAddressChangeStart(event: CheckoutAddressChangeStartEvent) {
        onMainThread {
            eventProcessor.onAddressChangeStart(event)
        }
    }

    internal fun onCheckoutViewSubmitStart(event: CheckoutSubmitStartEvent) {
        log.d(LOG_TAG, "Calling onCheckoutSubmitStart.")
        onMainThread {
            eventProcessor.onSubmitStart(event)
        }
    }

    internal fun onCheckoutViewPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStartEvent) {
        onMainThread {
            eventProcessor.onPaymentMethodChangeStart(event)
        }
    }

    internal companion object {
        private const val LOG_TAG = "CheckoutWebViewEventProcessor"
    }
}

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

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutErrorEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartEvent
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import java.lang.ref.WeakReference

internal class CheckoutBridge(
    private var eventProcessor: CheckoutWebViewEventProcessor,
) {

    private var webViewRef: WeakReference<WebView>? = null

    /**
     * TODO:
     * This is architecturally the inverse of what we do for iOS, where the RCTCheckoutWebView.swift holds the list of events active
     * Once the AddressPicker screen is in, as the Android native apps can pass references to the events directly
     * We can move this to the RCTCheckoutWebView.java, and remove the webViewRef as that exists on
     * the events.
     * This doesn't affect behaviour just means they're consistent
     */
    private val pendingEvents = mutableMapOf<String, RPCRequest<*, *>>()

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        this.eventProcessor = eventProcessor
    }

    fun getEventProcessor(): CheckoutWebViewEventProcessor = this.eventProcessor

    fun setWebView(webView: WebView?) {
        this.webViewRef = if (webView != null) WeakReference(webView) else null
    }

    /**
     * Respond to an RPC event with the given response data
     * @param eventId The ID of the event to respond to
     * @param responseData The JSON response data to send back
     */
    fun respondToEvent(eventId: String, responseData: String) {
        val event = pendingEvents[eventId]
        if (event == null) return
        event.respondWith(jsonString = responseData)
        pendingEvents.remove(event.id)
    }

    /**
     * Sets up an RPC request for response handling by:
     * 1. Setting the WebView reference on the request so it can respond directly
     * 2. Storing the event in pendingEvents for potential React Native response
     *
     * @param rpcRequest The RPC request to set up
     */
    private fun setupRequestForResponse(rpcRequest: RPCRequest<*, *>) {
        // Set the WebView reference on the request so it can respond directly
        webViewRef?.get()?.let { webView ->
            rpcRequest.webView = WeakReference(webView)
        }

        // Store the event for potential React Native response
        pendingEvents[rpcRequest.id] = rpcRequest
    }

    // Allows Web to postMessages back to the SDK
    @Suppress("SwallowedException")
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            log.d(LOG_TAG, "Received message from checkout.")

            when (val event = RPCRequestRegistry.decode(message)) {
                is CheckoutAddressChangeStartEvent -> handleRpcRequest(
                    rpcRequest = event.rpcRequest,
                    logMessage = "checkout.addressChangeStart",
                    dispatch = { eventProcessor.onCheckoutViewAddressChangeStart(event) }
                )

                is CheckoutSubmitStartEvent -> handleRpcRequest(
                    rpcRequest = event.rpcRequest,
                    logMessage = "checkout.submitStart",
                    dispatch = { eventProcessor.onCheckoutViewSubmitStart(event) }
                )

                is CheckoutPaymentMethodChangeStartEvent -> handleRpcRequest(
                    rpcRequest = event.rpcRequest,
                    logMessage = "checkout.paymentMethodChangeStart",
                    dispatch = { eventProcessor.onCheckoutViewPaymentMethodChangeStart(event) }
                )

                is CheckoutStartEvent -> handleNotification(
                    logMessage = "checkout.start",
                    dispatch = { eventProcessor.onCheckoutViewStart(event) }
                )

                is CheckoutCompleteEvent -> handleNotification(
                    logMessage = "checkout.complete",
                    dispatch = { eventProcessor.onCheckoutViewComplete(event) }
                )

                is CheckoutErrorEvent -> handleNotification(
                    logMessage = "checkout.error",
                    dispatch = { eventProcessor.onCheckoutViewError(event) }
                )

                null -> log.d(LOG_TAG, "Unsupported message received. Ignoring.")

                else -> handleUnknownEvent(event)
            }
        } catch (e: Exception) {
            handleDecodingError(e)
        }
    }

    private fun handleRpcRequest(
        rpcRequest: RPCRequest<*, *>,
        logMessage: String,
        dispatch: () -> Unit
    ) {
        setupRequestForResponse(rpcRequest)
        log.d(LOG_TAG, "Received $logMessage message with webView ref: ${webViewRef?.get()}")
        onMainThread { dispatch() }
    }

    private fun handleNotification(
        logMessage: String,
        dispatch: () -> Unit
    ) {
        log.d(LOG_TAG, "Received $logMessage message. Dispatching decoded event.")
        onMainThread { dispatch() }
    }

    private fun handleUnknownEvent(event: Any) {
        if (event is RPCRequest<*, *>) {
            setupRequestForResponse(event)
            log.d(LOG_TAG, "Received RPC request of type ${event::class.simpleName}, id: ${event.id}")
        } else {
            log.d(LOG_TAG, "Received unknown notification type: ${event::class.simpleName}. Ignoring.")
        }
    }

    private fun handleDecodingError(e: Exception) {
        log.d(LOG_TAG, "Failed to decode message with error: $e. Calling onCheckoutFailedWithError")
        onMainThread {
            eventProcessor.onCheckoutViewFailedWithError(
                CheckoutSheetKitException(
                    errorDescription = "Error decoding message from checkout.",
                    errorCode = CheckoutSheetKitException.ERROR_RECEIVING_MESSAGE_FROM_CHECKOUT,
                    isRecoverable = true,
                ),
            )
        }
    }

    companion object {
        private const val LOG_TAG = "CheckoutBridge"
        const val SCHEMA_VERSION: String = "2025-10"

        /**
         * Static method to send a JSONRPC response via WebView.evaluateJavascript
         *
         * @param webView The WebView to send the response through
         * @param responseJson The stringified JSONRPC response envelope
         */
        fun sendResponse(webView: WebView, responseJson: String) {
            Handler(Looper.getMainLooper()).post {
                runCatching {
                    webView.evaluateJavascript(
                        """|
                        |(function() {
                        |    try {
                        |        if (window && typeof window.postMessage === 'function') {
                        |            window.postMessage($responseJson, '*');
                        |        } else if (window && window.console && window.console.error) {
                        |            window.console.error('window.postMessage is not available.');
                        |        }
                        |    } catch (error) {
                        |        if (window && window.console && window.console.error) {
                        |            window.console.error('Failed to post message to checkout', error);
                        |        }
                        |    }
                        |})();
                        |""".trimMargin(),
                        null,
                    )
                }
                .onFailure { error ->
                    log.e(
                        LOG_TAG,
                        "Failed to post response to checkout: ${error.message}",
                    )
                }
            }
        }
    }
}

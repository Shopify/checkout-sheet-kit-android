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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

internal class CheckoutBridge(
    private var eventProcessor: CheckoutWebViewEventProcessor,
    private val messageParser: CheckoutMessageParser = CheckoutMessageParser(Json { ignoreUnknownKeys = true }, log),
) {

    private var webViewRef: WeakReference<WebView>? = null
    private val pendingEvents = mutableMapOf<String, CheckoutMessageParser.JSONRPCMessage>()

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
        if (event is CheckoutMessageParser.JSONRPCMessage.AddressChangeRequested) {
            try {
                // Parse the response data as DeliveryAddressChangePayload
                val jsonParser = Json { ignoreUnknownKeys = true }
                val payload = jsonParser.decodeFromString<DeliveryAddressChangePayload>(responseData)
                event.respondWith(payload)
                pendingEvents.remove(eventId)
                log.d(LOG_TAG, "Successfully responded to event $eventId")
            } catch (e: Exception) {
                log.e(LOG_TAG, "Failed to parse response data for event $eventId: ${e.message}")
            }
        } else {
            log.w(LOG_TAG, "No pending event found with ID $eventId")
        }
    }

    // Allows Web to postMessages back to the SDK
    @Suppress("SwallowedException")
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            log.d(LOG_TAG, "Received message from checkout.")
            when (val checkoutMessage = messageParser.parse(message)) {
                is CheckoutMessageParser.JSONRPCMessage.AddressChangeRequested -> {
                    log.d(LOG_TAG, "Received checkout.addressChangeStart message.")
                    // Set the WebView reference on the message so it can respond directly
                    webViewRef?.get()?.let { webView ->
                        checkoutMessage.setWebView(webView)
                    }
                    // Store the event for potential React Native response
                    checkoutMessage.id?.let { id ->
                        pendingEvents[id] = checkoutMessage
                    }
                    onMainThread {
                        eventProcessor.onAddressChangeRequested(checkoutMessage.toEvent())
                    }
                }

                is CheckoutMessageParser.JSONRPCMessage.Completed -> {
                    log.d(LOG_TAG, "Received checkout.complete message. Dispatching decoded event.")
                    onMainThread {
                        eventProcessor.onCheckoutViewComplete(checkoutMessage.event)
                    }
                }

                null -> {
                    log.d(LOG_TAG, "Unsupported message received. Ignoring.")
                }
            }
        } catch (e: Exception) {
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

@Serializable
internal data class WebToSdkEvent(
    val name: String,
    val body: String = ""
)

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
import com.shopify.checkoutsheetkit.CheckoutMessageContract.EVENT_ADDRESS_CHANGE_REQUESTED
import com.shopify.checkoutsheetkit.CheckoutMessageContract.EVENT_COMPLETED
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_SET_DELIVERY_ADDRESS
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEventDecoder
import com.shopify.checkoutsheetkit.lifecycleevents.DeliveryAddressChangeMessage
import com.shopify.checkoutsheetkit.lifecycleevents.DeliveryAddressChangePayload
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class CheckoutBridge(
    private var eventProcessor: CheckoutWebViewEventProcessor,
    private val decoder: Json = Json { ignoreUnknownKeys = true },
    private val checkoutCompletedEventDecoder: CheckoutCompletedEventDecoder = CheckoutCompletedEventDecoder(decoder, log),
) {

    private val messageParser = CheckoutMessageParser(decoder, log)
    private val deliveryAddressMessage = DeliveryAddressChangeMessage(decoder)
    private val addressChangeRequestedDecoder = CheckoutAddressChangeRequestedDecoder(decoder)

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        this.eventProcessor = eventProcessor
    }

    fun getEventProcessor(): CheckoutWebViewEventProcessor = this.eventProcessor

    // Allows Web to postMessages back to the SDK
    @Suppress("SwallowedException")
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            log.d(LOG_TAG, "Received message from checkout.")
            when (val checkoutMessage = messageParser.parse(message)) {
                is CheckoutMessageParser.CheckoutMessage.AddressChangeRequested -> {
                    handleAddressChangeRequested(checkoutMessage.params)
                }

                is CheckoutMessageParser.CheckoutMessage.Completed -> {
                    handleCheckoutCompleted(checkoutMessage.params)
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

    private fun handleCheckoutCompleted(params: JsonObject) {
        log.d(LOG_TAG, "Received completed message. Attempting to decode.")

        val event = checkoutCompletedEventDecoder.decode(
            WebToSdkEvent(
                name = EVENT_COMPLETED,
                body = params.toString(),
            ),
        )

        log.d(LOG_TAG, "Decoded message $event.")
        onMainThread {
            eventProcessor.onCheckoutViewComplete(event)
        }
    }

    private fun handleAddressChangeRequested(params: JsonObject) {
        log.d(LOG_TAG, "Handling address change requested message.")

        val onResponse = { payload: DeliveryAddressChangePayload ->
            log.d(LOG_TAG, "Address change response received: $payload")
            sendAddressResponse(payload)
        }

        val onCancel = {
            log.d(LOG_TAG, "Address change cancelled")
        }

        val legacyMessage = WebToSdkEvent(
            name = EVENT_ADDRESS_CHANGE_REQUESTED,
            body = params.toString(),
        )

        addressChangeRequestedDecoder.decode(legacyMessage, onResponse, onCancel)?.let { event ->
            onMainThread {
                eventProcessor.onAddressChangeRequested(event)
            }
        }
    }

    private fun sendAddressResponse(payload: DeliveryAddressChangePayload) {
        CheckoutWebView.currentEntry()?.let { cacheEntry ->
            Handler(Looper.getMainLooper()).post {
                val messagePayload = deliveryAddressMessage.encodeSetDeliveryAddress(payload)
                val script = postMessageDispatchTemplate(messagePayload)

                try {
                    cacheEntry.view.evaluateJavascript(script, null)
                } catch (e: Exception) {
                    log.d(LOG_TAG, "Failed to send address message to checkout, invoking onCheckoutViewFailedWithError")
                    eventProcessor.onCheckoutViewFailedWithError(
                        CheckoutSheetKitException(
                            errorDescription = "Failed to send '$METHOD_SET_DELIVERY_ADDRESS' message to checkout.",
                            errorCode = CheckoutSheetKitException.ERROR_SENDING_MESSAGE_TO_CHECKOUT,
                            isRecoverable = true,
                        ),
                    )
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG = "CheckoutBridge"
        const val SCHEMA_VERSION: String = "2025-10"

        private fun postMessageDispatchTemplate(body: String) = """|
        |(function() {
        |    try {
        |        if (window && window.postMessage) {
        |            window.postMessage($body, '*');
        |        }
        |    } catch (error) {
        |        if (window && window.console && window.console.error) {
        |            window.console.error('Failed to dispatch message to checkout', error);
        |        }
        |    }
        |})();
        |""".trimMargin()
    }
}

@Serializable
internal data class WebToSdkEvent(
    val name: String,
    val body: String = ""
)

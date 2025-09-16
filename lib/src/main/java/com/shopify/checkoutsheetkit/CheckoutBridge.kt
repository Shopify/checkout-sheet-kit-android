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
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.ADDRESS_CHANGE_INTENT
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.COMPLETED
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.ERROR
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.MODAL
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.WEB_PIXELS
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.errorevents.CheckoutErrorDecoder
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEventDecoder
import com.shopify.checkoutsheetkit.pixelevents.PixelEventDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class CheckoutBridge(
    private var eventProcessor: CheckoutWebViewEventProcessor,
    private val decoder: Json = Json { ignoreUnknownKeys = true },
    private val pixelEventDecoder: PixelEventDecoder = PixelEventDecoder(decoder, log),
    private val checkoutCompletedEventDecoder: CheckoutCompletedEventDecoder = CheckoutCompletedEventDecoder(decoder, log),
    private val checkoutErrorDecoder: CheckoutErrorDecoder = CheckoutErrorDecoder(decoder, log),
) {

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        this.eventProcessor = eventProcessor
    }

    fun getEventProcessor(): CheckoutWebViewEventProcessor = this.eventProcessor

    enum class CheckoutWebOperation(val key: String) {
        ADDRESS_CHANGE_INTENT("addressChangeIntent"),
        COMPLETED("completed"),
        MODAL("checkoutBlockingEvent"),
        WEB_PIXELS("webPixels"),
        ERROR("error");

        companion object {
            fun fromKey(key: String): CheckoutWebOperation? {
                return entries.find { it.key == key }
            }
        }
    }

    sealed class SDKOperation(val key: String) {
        data object Presented : SDKOperation("presented")
        data class DeliveryAddressChange(val payload: DeliveryAddressChangePayload): SDKOperation("deliveryAddressChange")
        class Instrumentation(val payload: InstrumentationPayload) : SDKOperation("instrumentation")
    }

    // Allows Web to postMessages back to the SDK
    @Suppress("SwallowedException")
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            log.d(LOG_TAG, "Received message from checkout.")
            val decodedMsg = decoder.decodeFromString<WebToSdkEvent>(message)

            when (CheckoutWebOperation.fromKey(decodedMsg.name)) {
                ADDRESS_CHANGE_INTENT -> {
                    log.d(LOG_TAG, "ADDRESS CHANGE INTENT $message")
                    
                    val onResponse = { payload: DeliveryAddressChangePayload ->
                        log.d(LOG_TAG, "Address change response received: $payload")
                        // Send the response back to the WebView
                        sendAddressResponse(payload)
                        // Navigate back to checkout
                        eventProcessor.onAddressResponseComplete()
                    }
                    
                    val onCancel = {
                        log.d(LOG_TAG, "Address change cancelled")
                        // Send cancellation response back to the WebView
                        sendAddressCancellation()
                        // Navigate back to checkout
                        eventProcessor.onAddressCancelled()
                    }
                    
                    CheckoutAddressChangeIntentDecoder().decode(decodedMsg, onResponse, onCancel)?.let { event ->
                        eventProcessor.onAddressChangeIntent(event)
                    }
                }

                COMPLETED -> {
                    log.d(LOG_TAG, "Received Completed message.  Attempting to decode.")
                    checkoutCompletedEventDecoder.decode(decodedMsg).let { event ->
                        log.d(LOG_TAG, "Decoded message $event.")
                        eventProcessor.onCheckoutViewComplete(event)
                    }
                }

                MODAL -> {
                    log.d(LOG_TAG, "Received Modal message.")
                    val modalVisible = decodedMsg.body.toBooleanStrictOrNull()
                    modalVisible?.let {
                        log.d(LOG_TAG, "Modal visible $it")
                        eventProcessor.onCheckoutViewModalToggled(modalVisible)
                    }
                }

                WEB_PIXELS -> {
                    log.d(LOG_TAG, "Received WebPixel message. Attempting to decode.")
                    pixelEventDecoder.decode(decodedMsg)?.let { event ->
                        log.d(LOG_TAG, "Decoded message $event.")
                        eventProcessor.onWebPixelEvent(event)
                    }
                }

                ERROR -> {
                    log.d(LOG_TAG, "Received Error message. Attempting to decode.")
                    checkoutErrorDecoder.decode(decodedMsg)?.let { exception ->
                        log.d(LOG_TAG, "Decoded message $exception.")
                        eventProcessor.onCheckoutViewFailedWithError(exception)
                    }
                }

                else -> {}
            }
        } catch (e: Exception) {
            log.d(LOG_TAG, "Failed to decode message with error: $e. Calling onCheckoutFailedWithError")
            eventProcessor.onCheckoutViewFailedWithError(
                CheckoutSheetKitException(
                    errorDescription = "Error decoding message from checkout.",
                    errorCode = CheckoutSheetKitException.ERROR_RECEIVING_MESSAGE_FROM_CHECKOUT,
                    isRecoverable = true,
                ),
            )
        }
    }

    // Send messages from SDK to Web
    @Suppress("SwallowedException")
    fun sendMessage(view: WebView, operation: SDKOperation) {
        val script = when (operation) {
            is SDKOperation.Presented -> {
                log.d(LOG_TAG, "Sending presented message to checkout, informing it that the sheet is now visible.")
                dispatchMessageTemplate("'${operation.key}'")
            }

            is SDKOperation.Instrumentation -> {
                log.d(LOG_TAG, "Sending instrumentation message to checkout.")
                val body = Json.encodeToString(SdkToWebEvent(operation.payload))
                dispatchMessageTemplate("'${operation.key}', $body")
            }

            is SDKOperation.DeliveryAddressChange -> {
                val body = Json.encodeToString(SdkToWebEvent(operation.payload))
                dispatchMessageTemplate("'${operation.key}', $body")
            }
        }
        try {
            view.evaluateJavascript(script, null)
        } catch (e: Exception) {
            log.d(LOG_TAG, "Failed to send message to checkout, invoking onCheckoutViewFailedWithError")
            eventProcessor.onCheckoutViewFailedWithError(
                CheckoutSheetKitException(
                    errorDescription = "Failed to send '${operation.key}' message to checkout, some features may not work.",
                    errorCode = CheckoutSheetKitException.ERROR_SENDING_MESSAGE_TO_CHECKOUT,
                    isRecoverable = true,
                )
            )
        }
    }

    private fun sendAddressResponse(payload: DeliveryAddressChangePayload) {        
        CheckoutWebView.currentEntry()?.let { cacheEntry ->
            Handler(Looper.getMainLooper()).post {
                sendMessage(cacheEntry.view, SDKOperation.DeliveryAddressChange(payload))
            }
        }
    }

    private fun sendAddressCancellation() {
        log.d(LOG_TAG, "Address change was cancelled - not sending response to WebView")
        // For cancellation, we typically don't send anything back to the WebView
        // The checkout will remain in its current state
    }


    companion object {
        private const val LOG_TAG = "CheckoutBridge"
        const val SCHEMA_VERSION_NUMBER: String = "8.1"

        private fun dispatchMessageTemplate(body: String) = """|
        |if (window.MobileCheckoutSdk && window.MobileCheckoutSdk.dispatchMessage) {
        |    window.MobileCheckoutSdk.dispatchMessage($body);
        |} else {
        |    window.addEventListener('mobileCheckoutBridgeReady', function () {
        |        window.MobileCheckoutSdk.dispatchMessage($body);
        |    }, {passive: true, once: true});
        |}
        |""".trimMargin()
    }
}

@Serializable
internal data class SdkToWebEvent<T>(
    val detail: T
)

@Serializable
internal data class InstrumentationPayload(
    val name: String,
    val value: Long,
    val type: InstrumentationType,
    val tags: Map<String, String>
)

@Serializable
public data class DeliveryAddressChangePayload(
    val delivery: CartDelivery,
)

@Serializable
public class CartDelivery(
    public val addresses: List<CartSelectableAddressInput>,
)

@Serializable
public data class CartSelectableAddressInput(
    public val address: CartDeliveryAddressInput,
)

@Serializable
public data class CartDeliveryAddressInput(
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val address1: String? = null,
    public val address2: String? = null,
    public val city: String? = null,
    public val countryCode: String? = null,
    public val phone: String? = null,
    public val provinceCode: String? = null,
    public val zip: String? = null,
)

@Suppress("EnumEntryName", "EnumNaming")
@Serializable
internal enum class InstrumentationType {
    histogram
}

@Serializable
internal data class WebToSdkEvent(
    val name: String,
    val body: String = ""
)

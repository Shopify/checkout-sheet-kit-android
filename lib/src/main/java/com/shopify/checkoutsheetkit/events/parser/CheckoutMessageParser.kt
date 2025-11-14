package com.shopify.checkoutsheetkit.events.parser

import android.webkit.WebView
import com.shopify.checkoutsheetkit.CheckoutBridge
import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.events.CartDeliveryAddress
import com.shopify.checkoutsheetkit.events.CheckoutAddressChangeRequestedEvent
import com.shopify.checkoutsheetkit.events.CheckoutAddressChangeRequestedEventData
import com.shopify.checkoutsheetkit.events.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.events.CheckoutStartEvent
import com.shopify.checkoutsheetkit.events.DeliveryAddressChangePayload
import com.shopify.checkoutsheetkit.events.DeliveryAddressChangeResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.lang.ref.WeakReference

internal class CheckoutMessageParser(
    internal val json: Json,
    private val log: LogWrapper = ShopifyCheckoutSheetKit.log,
) {

    fun parse(rawMessage: String): JSONRPCMessage? {
        val envelope = json.runCatching { decodeFromString<JsonRpcEnvelope>(rawMessage) }.getOrNull()
            ?: return null

        val id = envelope.id?.jsonPrimitive?.contentOrNull

        return when (envelope.method) {
            CheckoutMessageContract.METHOD_ADDRESS_CHANGE_REQUESTED -> envelope.params
                .decodeOrNull<CheckoutAddressChangeRequestedEventData> {
                    log.d(LOG_TAG, "Failed to decode address change requested params: ${it.message}")
                }
                ?.let { JSONRPCMessage.AddressChangeRequested(id, it) }

            CheckoutMessageContract.METHOD_START -> envelope.params
                .decodeOrNull<CheckoutStartEvent> {
                    log.d(LOG_TAG, "Failed to decode checkout start params: ${it.message}")
                }
                ?.let { JSONRPCMessage.Started(it) }

            CheckoutMessageContract.METHOD_COMPLETE -> envelope.params
                .decodeOrNull<CheckoutCompleteEvent> {
                    log.d(LOG_TAG, "Failed to decode checkout completed params: ${it.message}")
                }
                ?.let { JSONRPCMessage.Completed(it) }

            else -> {
                log.d(LOG_TAG, "Received unsupported message method: ${envelope.method}")
                null
            }
        }
    }

    sealed class JSONRPCMessage {
        /**
         * Base class for JSONRPC notifications (one-way messages with no ID and no response expected)
         */
        sealed class JSONRPCNotification : JSONRPCMessage()

        /**
         * Base class for JSONRPC requests (messages with ID that expect a response)
         */
        sealed class JSONRPCRequest : JSONRPCMessage() {
            abstract val id: String?
            internal var webViewRef: WeakReference<WebView>? = null

            internal fun setWebView(webView: WebView) {
                webViewRef = WeakReference(webView)
            }
        }

        data class AddressChangeRequested internal constructor(
            override val id: String?,
            internal val params: CheckoutAddressChangeRequestedEventData,
        ) : JSONRPCRequest() {
            private var hasResponded = false
            internal val addressType: String get() = params.addressType
            internal val selectedAddress: CartDeliveryAddress? get() = params.selectedAddress

            /**
             * Send a response to this JSONRPC request
             * @param payload The payload to send back to the WebView
             */
            internal fun respondWith(payload: DeliveryAddressChangePayload) {
                if (hasResponded) return
                hasResponded = true

                webViewRef?.get()?.let { webView ->
                    val response = DeliveryAddressChangeResponse()
                    val responseJson = response.encodeSetDeliveryAddress(payload, id)
                    CheckoutBridge.Companion.sendResponse(webView, responseJson)
                }
            }

            /**
             * Convert to public-facing event
             */
            internal fun toEvent(): CheckoutAddressChangeRequestedEvent {
                return CheckoutAddressChangeRequestedEvent(this)
            }
        }

        data class Started(
            val event: CheckoutStartEvent,
        ) : JSONRPCNotification()

        data class Completed(
            val event: CheckoutCompleteEvent,
        ) : JSONRPCNotification()
    }

    @Serializable
    private data class JsonRpcEnvelope(
        @SerialName(CheckoutMessageContract.VERSION_FIELD)
        val version: String? = null,
        val method: String? = null,
        val params: JsonElement? = null,
        val id: JsonElement? = null,
    )

    companion object {
        private const val LOG_TAG = "CheckoutMessageParser"
    }

    private inline fun <reified T> JsonElement?.decodeOrNull(onFailure: (Throwable) -> Unit): T? {
        return this?.let {
            runCatching { json.decodeFromJsonElement<T>(it) }
                .onFailure(onFailure)
                .getOrNull()
        }
    }
}

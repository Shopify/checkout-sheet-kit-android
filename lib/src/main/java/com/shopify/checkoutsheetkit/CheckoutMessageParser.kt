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

import android.webkit.WebView
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_ADDRESS_CHANGE_REQUESTED
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_COMPLETE
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
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
            METHOD_ADDRESS_CHANGE_REQUESTED ->
                envelope.params
                    .decodeOrNull<CheckoutAddressChangeRequestedEventData> {
                        log.d(LOG_TAG, "Failed to decode address change requested params: ${it.message}")
                    }
                    ?.let { JSONRPCMessage.AddressChangeRequested(id, it) }

            METHOD_COMPLETE ->
                envelope.params
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

            internal fun setWebView(webView: android.webkit.WebView) {
                webViewRef = WeakReference(webView)
            }
        }

        data class AddressChangeRequested internal constructor(
            override val id: String?,
            internal val params: CheckoutAddressChangeRequestedEventData,
        ) : JSONRPCRequest() {
            private var hasResponded = false
            internal val addressType: String get() = params.addressType
            internal val selectedAddress: CartDeliveryAddressInput? get() = params.selectedAddress

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
                    CheckoutBridge.sendResponse(webView, responseJson)
                }
            }

            /**
             * Convert to public-facing event
             */
            internal fun toEvent(): CheckoutAddressChangeRequestedEvent {
                return CheckoutAddressChangeRequestedEvent(this)
            }
        }

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

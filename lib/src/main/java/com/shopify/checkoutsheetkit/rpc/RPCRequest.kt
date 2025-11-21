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
package com.shopify.checkoutsheetkit.rpc

import android.webkit.WebView
import com.shopify.checkoutsheetkit.CheckoutBridge
import com.shopify.checkoutsheetkit.RespondableEvent
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
import java.lang.ref.WeakReference

/**
 * Abstract base class for all RPC request implementations.
 * Mirrors the Swift RPCRequest protocol with type parameters for request params and response payload.
 *
 * @param P The type of parameters for this request
 * @param R The type of response payload this request expects
 */
public abstract class RPCRequest<P : Any, R : Any>(
    /**
     * An identifier established by the client. If null, this is a notification.
     */
    public override val id: String?,
    /**
     * The parameters from the JSON-RPC request.
     */
    public open val params: P,
    /**
     * The serializer for the response type.
     */
    private val responseSerializer: KSerializer<R>
) : RespondableEvent {

    /**
     * The JSON-RPC version. Must be exactly "2.0".
     */
    public open val jsonrpc: String = "2.0"

    /**
     * Weak reference to the WebView for sending responses.
     */
    public var webView: WeakReference<WebView>? = null

    /**
     * Whether this is a notification (no response expected).
     */
    public val isNotification: Boolean get() = id == null

    private var hasResponded = false

    @OptIn(ExperimentalSerializationApi::class)
    public val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false // Exclude null fields from JSON output
    }

    /**
     * The RPC method name for this request type.
     * Subclasses must override this to provide their method name.
     */
    public abstract val method: String

    /**
     * Respond to this request with the specified payload.
     *
     * @param payload The response payload
     */
    @OptIn(InternalSerializationApi::class)
    public fun respondWith(payload: R) {
        ShopifyCheckoutSheetKit.log.d(
            "RPCRequest",
            "respondWith called for method '$method' with id '$id'. " +
                "webView: ${webView?.get()}, hasResponded: $hasResponded, isNotification: $isNotification"
        )

        when {
            hasResponded -> logMultipleResponseAttempt()
            isNotification -> logNotificationResponseAttempt()
            else -> handleValidResponse(payload)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun handleValidResponse(payload: R) {
        try {
            validate(payload)
            hasResponded = true
            sendSuccessResponse(payload)
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e(
                "RPCRequest",
                "Validation failed for RPC request '$method' with id '$id': ${e.message}"
            )
            respondWithError("Validation failed: ${e.message}")
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun sendSuccessResponse(payload: R) {
        webView?.get()?.let { webView ->
            try {
                val payloadJson: JsonElement = json.encodeToJsonElement(
                    responseSerializer,
                    payload
                )
                val responseJsonObject = buildJsonObject {
                    put("jsonrpc", JsonPrimitive(jsonrpc))
                    id?.let { put("id", JsonPrimitive(it)) }
                    put("result", payloadJson)
                }
                val responseJson = json.encodeToString(responseJsonObject)
                ShopifyCheckoutSheetKit.log.d(
                    "RPCRequest",
                    "About to call sendResponse for method '$method' with encoded response"
                )
                CheckoutBridge.sendResponse(webView, responseJson)
            } catch (e: Exception) {
                ShopifyCheckoutSheetKit.log.e(
                    "RPCRequest",
                    "Failed to encode response for RPC request '$method' with id '$id': ${e.message}"
                )
            }
        } ?: logWebViewLost()
    }

    private fun logMultipleResponseAttempt() {
        ShopifyCheckoutSheetKit.log.w(
            "RPCRequest",
            "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring."
        )
    }

    private fun logNotificationResponseAttempt() {
        ShopifyCheckoutSheetKit.log.w(
            "RPCRequest",
            "Attempted to respond to RPC notification '$method'. Notifications do not expect responses."
        )
    }

    private fun logWebViewLost() {
        ShopifyCheckoutSheetKit.log.w(
            "RPCRequest",
            "WebView reference lost for RPC request '$method' with id '$id'. webView: $webView"
        )
    }

    /**
     * Respond to this request with a JSON string.
     * Useful for language bindings (e.g., React Native).
     *
     * @param jsonString A JSON string representing the response payload
     */
    public fun respondWith(jsonString: String) {
        // Since we can't use inline reified, we'll need to handle this differently
        // For React Native bridge, the JSON should be parseable as JsonElement first
        try {
            val jsonElement = json.parseToJsonElement(jsonString)
            // Try to decode it as the response type using the responseSerializer
            // This approach requires the concrete class to provide proper deserialization
            respondWithJsonElement(jsonElement)
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e(
                "RPCRequest",
                "respondWith(json) failed to decode method: $method, id: $id, error: ${e.message}"
            )
            respondWithError(jsonString)
        }
    }

    /**
     * Internal method to respond with a JsonElement.
     * Uses the responseSerializer to deserialize the JSON element to the response type.
     */
    protected open fun respondWithJsonElement(jsonElement: JsonElement) {
        try {
            val payload = json.decodeFromJsonElement(responseSerializer, jsonElement)
            respondWith(payload)
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e(
                "RPCRequest",
                "Failed to decode JSON response for method: $method, error: ${e.message}"
            )
            respondWithError("Failed to decode JSON response: ${e.message}")
        }
    }

    /**
     * Respond to this request with an error.
     *
     * @param error The error message
     */
    public fun respondWithError(error: String) {
        when {
            hasResponded -> {
                ShopifyCheckoutSheetKit.log.w(
                    "RPCRequest",
                    "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring."
                )
            }
            isNotification -> {
                ShopifyCheckoutSheetKit.log.w(
                    "RPCRequest",
                    "Attempted to respond to RPC notification '$method'. Notifications do not expect responses."
                )
            }
            else -> {
                hasResponded = true

                webView?.get()?.let { webView ->
                    try {
                        // Build error response JSON manually using JsonObject
                        val responseJsonObject = buildJsonObject {
                            put("jsonrpc", JsonPrimitive(jsonrpc))
                            id?.let { put("id", JsonPrimitive(it)) }
                            put("error", JsonPrimitive(error))
                        }

                        // Convert JsonObject to string
                        val responseJson = json.encodeToString(responseJsonObject)
                        CheckoutBridge.sendResponse(webView, responseJson)
                    } catch (e: Exception) {
                        ShopifyCheckoutSheetKit.log.e(
                            "RPCRequest",
                            "Failed to encode error response for RPC request '$method' with id '$id': ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Validate the response payload before sending.
     * Default implementation does nothing - subclasses can override.
     *
     * @param payload The payload to validate
     * @throws EventResponseError.ValidationFailed if validation fails
     */
    public open fun validate(payload: R) {
        // Default implementation does nothing
        // Subclasses can override to add validation
    }

    public companion object {
        /**
         * Generic RPC envelope for decoding requests
         */
        @Serializable
        public data class RPCEnvelope<P>(
            @SerialName("jsonrpc")
            val jsonrpc: String,
            @SerialName("id")
            val id: String? = null,
            @SerialName("method")
            val method: String,
            @SerialName("params")
            val params: P
        )

        /**
         * Helper function for decoding RPC requests with type information
         */
        public inline fun <reified P : Any> decodeRequest(
            jsonString: String,
            factory: (id: String?, params: P) -> RPCRequest<*, *>
        ): RPCRequest<*, *> {
            val json = Json { ignoreUnknownKeys = true }
            val envelope = json.decodeFromString<RPCEnvelope<P>>(jsonString)
            return factory(envelope.id, envelope.params)
        }
    }
}
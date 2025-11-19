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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    public open val params: P
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
    protected val json: Json = Json {
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
     * The serializer for the response type R.
     * Subclasses must override this to provide the correct serializer for their response type.
     */
    public abstract val responseSerializer: KSerializer<R>

    /**
     * Respond to this request with the specified payload.
     *
     * @param payload The response payload
     */
    public fun respondWith(payload: R) {
        ShopifyCheckoutSheetKit.log.d("RPCRequest", "respondWith called for method '$method' with id '$id'. webView: ${webView?.get()}, hasResponded: $hasResponded, isNotification: $isNotification")
        
        if (hasResponded) {
            ShopifyCheckoutSheetKit.log.w("RPCRequest", "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring.")
            return
        }
        if (isNotification) {
            ShopifyCheckoutSheetKit.log.w("RPCRequest", "Attempted to respond to RPC notification '$method'. Notifications do not expect responses.")
            return
        }

        try {
            validate(payload)
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e("RPCRequest", "Validation failed for RPC request '$method' with id '$id': ${e.message}")
            respondWithError("Validation failed: ${e.message}")
            return
        }

        hasResponded = true

        webView?.get()?.let { webView ->
            val response = RPCResponse(
                jsonrpc = jsonrpc,
                id = id,
                result = payload
            )

            try {
                // Use the response serializer to encode with proper type information
                @OptIn(ExperimentalSerializationApi::class)
                val responseJson = json.encodeToString(
                    RPCResponse.serializer(responseSerializer),
                    response
                )
                ShopifyCheckoutSheetKit.log.d("RPCRequest", "About to call sendResponse for method '$method' with encoded response")
                CheckoutBridge.sendResponse(webView, responseJson)
            } catch (e: Exception) {
                ShopifyCheckoutSheetKit.log.e("RPCRequest", "Failed to encode response for RPC request '$method' with id '$id': ${e.message}")
            }
        } ?: run {
            ShopifyCheckoutSheetKit.log.w("RPCRequest", "WebView reference lost for RPC request '$method' with id '$id'. webView: $webView")
        }
    }

    /**
     * Respond to this request with a JSON string.
     * Useful for language bindings (e.g., React Native).
     *
     * @param json A JSON string representing the response payload
     */
    public fun respondWith(json: String) {
        // This will be overridden by concrete classes if they need custom deserialization
        // For now, we can't deserialize without knowing the concrete type R
        ShopifyCheckoutSheetKit.log.e("RPCRequest", "respondWith(json) called but not implemented for '$method'")
        respondWithError("JSON response not supported for this request type")
    }

    /**
     * Respond to this request with an error.
     *
     * @param error The error message
     */
    public fun respondWithError(error: String) {
        if (hasResponded) {
            ShopifyCheckoutSheetKit.log.w("RPCRequest", "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring.")
            return
        }
        if (isNotification) {
            ShopifyCheckoutSheetKit.log.w("RPCRequest", "Attempted to respond to RPC notification '$method'. Notifications do not expect responses.")
            return
        }

        hasResponded = true

        webView?.get()?.let { webView ->
            val response = RPCResponse<R>(
                jsonrpc = jsonrpc,
                id = id,
                error = error
            )

            try {
                @OptIn(ExperimentalSerializationApi::class)
                val responseJson = json.encodeToString(
                    RPCResponse.serializer(responseSerializer),
                    response
                )
                CheckoutBridge.sendResponse(webView, responseJson)
            } catch (e: Exception) {
                ShopifyCheckoutSheetKit.log.e("RPCRequest", "Failed to encode error response for RPC request '$method' with id '$id': ${e.message}")
            }
        }
    }

    /**
     * Validate the response payload before sending.
     * Default implementation does nothing - subclasses can override.
     *
     * @param payload The payload to validate
     * @throws Exception if validation fails
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
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

/**
 * Base class for all RPC request implementations.
 * Provides common functionality for response handling and WebView communication.
 *
 * @param P The type of parameters for this request
 * @param R The type of response payload this request expects
 */
public abstract class BaseRPCRequest<P : Any, R : Any>(
    override val id: String?,
    override val params: P
) : RPCRequest<P, R>, RespondableEvent {

    override val jsonrpc: String = "2.0"
    override var webView: WeakReference<WebView>? = null
    override val isNotification: Boolean get() = id == null

    private var hasResponded = false

    protected val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * The RPC method name for this request type.
     * Subclasses must override this to provide their method name.
     */
    abstract override val method: String

    override fun respondWith(payload: R) {
        if (hasResponded) {
            ShopifyCheckoutSheetKit.log.w("BaseRPCRequest", "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring.")
            return
        }
        if (isNotification) {
            ShopifyCheckoutSheetKit.log.w("BaseRPCRequest", "Attempted to respond to RPC notification '$method'. Notifications do not expect responses.")
            return
        }

        try {
            validate(payload)
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e("BaseRPCRequest", "Validation failed for RPC request '$method' with id '$id': ${e.message}")
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
                // Use inline reified function to encode with proper type information
                val responseJson = json.encodeToString(response)
                CheckoutBridge.sendResponse(webView, responseJson)
            } catch (e: Exception) {
                ShopifyCheckoutSheetKit.log.e("BaseRPCRequest", "Failed to encode response for RPC request '$method' with id '$id': ${e.message}")
            }
        } ?: run {
            ShopifyCheckoutSheetKit.log.w("BaseRPCRequest", "WebView reference lost for RPC request '$method' with id '$id'")
        }
    }

    override fun respondWith(json: String) {
        // This will be overridden by concrete classes if they need custom deserialization
        // For now, we can't deserialize without knowing the concrete type R
        ShopifyCheckoutSheetKit.log.e("BaseRPCRequest", "respondWith(json) called but not implemented for '$method'")
        respondWithError("JSON response not supported for this request type")
    }

    override fun respondWithError(error: String) {
        if (hasResponded) {
            ShopifyCheckoutSheetKit.log.w("BaseRPCRequest", "Attempted to respond to RPC request '$method' with id '$id' multiple times. Ignoring.")
            return
        }
        if (isNotification) {
            ShopifyCheckoutSheetKit.log.w("BaseRPCRequest", "Attempted to respond to RPC notification '$method'. Notifications do not expect responses.")
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
                val responseJson = json.encodeToString(response)
                CheckoutBridge.sendResponse(webView, responseJson)
            } catch (e: Exception) {
                ShopifyCheckoutSheetKit.log.e("BaseRPCRequest", "Failed to encode error response for RPC request '$method' with id '$id': ${e.message}")
            }
        }
    }

    override fun validate(payload: R) {
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
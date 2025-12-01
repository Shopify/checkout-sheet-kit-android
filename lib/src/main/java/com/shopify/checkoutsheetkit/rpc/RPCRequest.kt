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
import com.shopify.checkoutsheetkit.CheckoutRequest
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutEventResponseException
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
 * Internal RPC request implementation.
 * Mirrors the Swift RPCRequest protocol with type parameters for request params and response payload.
 *
 * @param P The type of parameters for this request
 * @param R The type of response payload this request expects
 */
internal class RPCRequest<P : Any, R : Any>(
    /**
     * An identifier established by the client, used to correlate responses.
     */
    override val id: String,
    /**
     * The parameters from the JSON-RPC request.
     * Contains event-specific data like cart and checkout information.
     */
    val params: P,
    /**
     * The serializer for the response type.
     */
    private val responseSerializer: KSerializer<R>,
    /**
     * The RPC method name for this request type.
     */
    override val method: String
) : CheckoutRequest<R> {

    /**
     * The JSON-RPC version. Must be exactly "2.0".
     * Internal implementation detail.
     */
    internal val jsonrpc: String = "2.0"

    /**
     * Weak reference to the WebView for sending responses.
     * Internal - used by the SDK to send responses back through the bridge.
     */
    internal var webView: WeakReference<WebView>? = null

    private var hasResponded = false

    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false // Exclude null fields from JSON output
    }


    /**
     * Respond to this request with the specified payload.
     *
     * @param payload The response payload
     */
    @OptIn(InternalSerializationApi::class)
    override fun respondWith(payload: R) {
        ShopifyCheckoutSheetKit.log.d(
            "RPCRequest",
            "respondWith called for method '$method' with id '$id'. " +
                "webView: ${webView?.get()}, hasResponded: $hasResponded"
        )

        if (hasResponded) {
            logMultipleResponseAttempt()
        } else {
            handleValidResponse(payload)
        }
    }

    private fun handleValidResponse(payload: R) {
        hasResponded = true
        sendSuccessResponse(payload)
    }

    private fun sendSuccessResponse(payload: R) {
        webView?.get()?.let { webView ->
            try {
                val payloadJson: JsonElement = json.encodeToJsonElement(
                    responseSerializer,
                    payload
                )
                val responseJsonObject = buildJsonObject {
                    put("jsonrpc", JsonPrimitive(jsonrpc))
                    put("id", JsonPrimitive(id))
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
     * @throws CheckoutEventResponseException.DecodingFailed if JSON parsing or deserialization fails
     */
    override fun respondWith(jsonString: String) {
        val jsonElement = try {
            json.parseToJsonElement(jsonString)
        } catch (e: Exception) {
            throw CheckoutEventResponseException.DecodingFailed("Failed to parse JSON: ${e.message}", e)
        }
        respondWithJsonElement(jsonElement)
    }

    /**
     * Internal method to respond with a JsonElement.
     * Uses the responseSerializer to deserialize the JSON element to the response type.
     *
     * @throws CheckoutEventResponseException.DecodingFailed if deserialization fails
     */
    internal fun respondWithJsonElement(jsonElement: JsonElement) {
        val payload = try {
            json.decodeFromJsonElement(responseSerializer, jsonElement)
        } catch (e: Exception) {
            throw CheckoutEventResponseException.DecodingFailed("Failed to decode response: ${e.message}", e)
        }
        respondWith(payload)
    }
}

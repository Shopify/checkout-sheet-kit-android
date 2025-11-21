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

import com.shopify.checkoutsheetkit.CheckoutMessageContract
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequested
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Registry for RPC request types, handling type-erased decoding.
 * This allows us to decode incoming JSON-RPC messages into the appropriate
 * request type based on the method name.
 */
public object RPCRequestRegistry {
    /**
     * List of all supported RPC request decoder types.
     * Add new request types here as they are implemented.
     *
     * Note: Each decoder must implement TypeErasedRPCDecodable and provide
     * a companion object that implements it.
     */
    public val requestTypes: List<TypeErasedRPCDecodable> = listOf(
        AddressChangeRequested.Companion,
        CheckoutStart.Companion,
        CheckoutComplete.Companion
    )

    private val registry: Map<String, TypeErasedRPCDecodable> by lazy {
        buildRegistry()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Build the registry from the static list of request types.
     * Each request type must provide its method name for registration.
     */
    private fun buildRegistry(): Map<String, TypeErasedRPCDecodable> {
        val map = mutableMapOf<String, TypeErasedRPCDecodable>()
        for (decoder in requestTypes) {
            // Each decoder should provide the method it handles
            map[decoder.method] = decoder
        }
        return map
    }

    /**
     * Decode a JSON-RPC message into the appropriate request type.
     *
     * @param jsonString The JSON string to decode
     * @return The decoded RPC request, or null if the method is not registered
     */
    public fun decode(jsonString: String): RPCRequest<*, *>? {
        return try {
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val method = jsonObject[CheckoutMessageContract.METHOD_FIELD]?.jsonPrimitive?.content

            when {
                // Validate JSON-RPC version
                jsonObject[CheckoutMessageContract.VERSION_FIELD]?.jsonPrimitive?.content != CheckoutMessageContract.VERSION -> {
                    val version = jsonObject[CheckoutMessageContract.VERSION_FIELD]?.jsonPrimitive?.content
                    ShopifyCheckoutSheetKit.log.e("RPCRequestRegistry", "Invalid JSON-RPC version: $version")
                    null
                }
                // Check for method field
                method == null -> {
                    ShopifyCheckoutSheetKit.log.e("RPCRequestRegistry", "Missing method field in JSON-RPC message")
                    null
                }
                // Decode if decoder exists
                else -> {
                    val decoder = registry[method]
                    if (decoder == null) {
                        ShopifyCheckoutSheetKit.log.d("RPCRequestRegistry", "No decoder registered for method: $method")
                        null
                    } else {
                        decoder.decodeErased(jsonString)
                    }
                }
            }
        } catch (e: Exception) {
            ShopifyCheckoutSheetKit.log.e("RPCRequestRegistry", "Failed to decode JSON-RPC message: ${e.message}")
            null
        }
    }

    /**
     * Check if a decoder is registered for a specific method.
     *
     * @param method The RPC method name
     * @return True if a decoder is registered for this method
     */
    public fun isRegistered(method: String): Boolean {
        return registry.containsKey(method)
    }

    /**
     * Get all registered method names.
     *
     * @return Set of registered method names
     */
    public fun getRegisteredMethods(): Set<String> {
        return registry.keys.toSet()
    }
}
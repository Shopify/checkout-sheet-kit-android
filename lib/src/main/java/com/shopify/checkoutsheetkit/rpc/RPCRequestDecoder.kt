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

import com.shopify.checkoutsheetkit.CheckoutNotification
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * RPC decoder for request events (bidirectional, response expected).
 * Decodes the RPC envelope and wraps params in an RPCRequest instance.
 * This eliminates the need to implement decodeErased in every RPC request.
 */
internal class RPCRequestDecoder<P : Any, R : Any>(
    override val method: String,
    private val paramsSerializer: KSerializer<P>,
    private val responseSerializer: KSerializer<R>,
    private val factory: (id: String, params: P, responseSerializer: KSerializer<R>) -> CheckoutNotification
) : TypeErasedRPCDecodable {

    private val json = Json { ignoreUnknownKeys = true }

    override fun decodeErased(jsonString: String): CheckoutNotification {
        val envelope = json.decodeFromString(
            RPCEnvelope.serializer(paramsSerializer),
            jsonString
        )
        val id = envelope.id
        if (id == null) {
            ShopifyCheckoutSheetKit.log.e(
                "RPCRequestDecoder",
                "Request event (method '$method') is missing required 'id' field"
            )
            throw IllegalArgumentException("Request events (method '$method') must have an 'id' field")
        }
        return factory(id, envelope.params, responseSerializer)
    }

    companion object {
        /**
         * Create a decoder using an inline reified function to capture the serializers automatically.
         */
        inline fun <reified P : Any, reified R : Any> create(
            method: String,
            noinline factory: (id: String, params: P, responseSerializer: KSerializer<R>) -> CheckoutNotification
        ): RPCRequestDecoder<P, R> {
            return RPCRequestDecoder(
                method = method,
                paramsSerializer = serializer(),
                responseSerializer = serializer(),
                factory = factory
            )
        }
    }
}

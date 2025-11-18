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
package com.shopify.checkoutsheetkit.rpcevents

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Simple decoder implementation that can be used as a companion object delegate.
 * This eliminates the need to implement decodeErased in every RPC request.
 */
public class SimpleRPCDecoder<P : Any, R : Any>(
    private val paramsSerializer: KSerializer<P>,
    private val factory: (id: String?, params: P) -> BaseRPCRequest<P, R>
) : TypeErasedRPCDecodable {

    private val json = Json { ignoreUnknownKeys = true }

    private val cachedMethod: String by lazy {
        // Create a dummy instance with null id and minimal params
        // We need to handle the case where params have required fields
        val dummyJson = when (paramsSerializer.descriptor.serialName) {
            "com.shopify.checkoutsheetkit.rpcevents.AddressChangeRequestedParams" -> """{"addressType":""}"""
            else -> "{}"
        }
        val dummyParams = json.decodeFromString(paramsSerializer, dummyJson)
        factory(null, dummyParams).method
    }

    override fun getMethod(): String = cachedMethod

    override fun decodeErased(jsonString: String): RPCRequest<*, *> {
        val envelope = json.decodeFromString(
            BaseRPCRequest.Companion.RPCEnvelope.serializer(paramsSerializer),
            jsonString
        )
        return factory(envelope.id, envelope.params)
    }

    public companion object {
        /**
         * Create a decoder using an inline reified function to capture the serializer automatically.
         * The method is extracted from an instance, eliminating duplication.
         */
        public inline fun <reified P : Any, R : Any> create(
            noinline factory: (id: String?, params: P) -> BaseRPCRequest<P, R>
        ): SimpleRPCDecoder<P, R> {
            return SimpleRPCDecoder(
                paramsSerializer = kotlinx.serialization.serializer(),
                factory = factory
            )
        }

        /**
         * Create a decoder with an explicit method name (for backwards compatibility).
         */
        public inline fun <reified P : Any> create(
            method: String,
            noinline factory: (id: String?, params: P) -> RPCRequest<*, *>
        ): SimpleRPCDecoder<P, Any> {
            return SimpleRPCDecoder(
                paramsSerializer = kotlinx.serialization.serializer(),
                factory = factory as (id: String?, params: P) -> BaseRPCRequest<P, Any>
            )
        }
    }
}
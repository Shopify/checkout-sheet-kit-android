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
package com.shopify.checkoutsheetkit.lifecycleevents

import com.shopify.checkoutsheetkit.CheckoutRequest
import com.shopify.checkoutsheetkit.rpc.RPCRequestDecoder
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.TypeErasedRPCDecodable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val ADDRESS_CHANGE_START_METHOD = "checkout.addressChangeStart"

/**
 * RPC request for address change start events from checkout.
 */
public class CheckoutAddressChangeStartEvent internal constructor(
    private val request: RPCRequest<CheckoutAddressChangeStartParams, CheckoutAddressChangeStartResponsePayload>
) : CheckoutRequest<CheckoutAddressChangeStartResponsePayload> by request {

    internal constructor(
        id: String,
        params: CheckoutAddressChangeStartParams,
        responseSerializer: KSerializer<CheckoutAddressChangeStartResponsePayload>
    ) : this(RPCRequest(id, params, responseSerializer, ADDRESS_CHANGE_START_METHOD))

    /**
     * The current cart state at the time of address change.
     */
    public val cart: Cart
        get() = request.params.cart

    /**
     * The type of address being changed (e.g., "shipping", "billing").
     */
    public val addressType: String
        get() = request.params.addressType

    internal val rpcRequest: RPCRequest<*, *> get() = request

    internal companion object : TypeErasedRPCDecodable by RPCRequestDecoder.create<
        CheckoutAddressChangeStartParams,
        CheckoutAddressChangeStartResponsePayload
        >(
        method = ADDRESS_CHANGE_START_METHOD,
        factory = ::CheckoutAddressChangeStartEvent
    )

    override fun toString(): String {
        return "CheckoutAddressChangeStartEvent(id='$id', method='$method', addressType='$addressType', cart=$cart)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckoutAddressChangeStartEvent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Serializable
internal data class CheckoutAddressChangeStartParams(
    val addressType: String,
    val cart: Cart
)

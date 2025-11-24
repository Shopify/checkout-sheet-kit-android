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
package com.shopify.checkoutsheetkit.rpc.events

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.rpc.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.RPCDecoder
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.TypeErasedRPCDecodable
import kotlinx.serialization.Serializable

private const val ADDRESS_CHANGE_START_METHOD = "checkout.addressChangeStart"

/**
 * RPC request for address change start events from checkout.
 */
public class CheckoutAddressChangeStart(
    id: String?,
    params: CheckoutAddressChangeStartEvent,
    responseSerializer: kotlinx.serialization.KSerializer<CheckoutAddressChangeStartResponsePayload>
) : RPCRequest<CheckoutAddressChangeStartEvent, CheckoutAddressChangeStartResponsePayload>(id, params, responseSerializer) {

    override val method: String = ADDRESS_CHANGE_START_METHOD

    override fun validate(payload: CheckoutAddressChangeStartResponsePayload) {
        val cart = payload.cart ?: return

        val addresses = cart.delivery?.addresses
        if (addresses == null || addresses.isEmpty()) {
            throw CheckoutEventResponseException.ValidationFailed(
                "At least one address is required in cart.delivery.addresses"
            )
        }

        addresses.forEachIndexed { index, selectableAddress ->
            val countryCode = selectableAddress.address.countryCode
            when {
                countryCode.isNullOrEmpty() -> throw CheckoutEventResponseException.ValidationFailed(
                    "Country code is required at index $index"
                )
                countryCode.length != 2 -> throw CheckoutEventResponseException.ValidationFailed(
                    "Country code must be exactly 2 characters (ISO 3166-1 alpha-2) at index $index, got: '$countryCode'"
                )
            }
        }
    }

    public companion object : TypeErasedRPCDecodable by RPCDecoder.create(
        method = ADDRESS_CHANGE_START_METHOD,
        factory = ::CheckoutAddressChangeStart
    )
}

/**
 * Parameters for the address change start RPC event.
 */
@Serializable
public data class CheckoutAddressChangeStartEvent(
    /**
     * The type of address being changed (e.g., "shipping", "billing")
     */
    public val addressType: String,

    /**
     * The current cart state
     */
    public val cart: Cart
)

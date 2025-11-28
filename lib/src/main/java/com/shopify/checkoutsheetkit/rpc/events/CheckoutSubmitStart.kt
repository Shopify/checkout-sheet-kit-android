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

import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.lifecycleevents.Checkout
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartResponsePayload
import com.shopify.checkoutsheetkit.rpc.RPCDecoder
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.TypeErasedRPCDecodable
import kotlinx.serialization.Serializable

private const val SUBMIT_START_METHOD = "checkout.submitStart"

/**
 * RPC request for submit start events from checkout.
 * This event is emitted when the buyer attempts to submit the checkout.
 */
public class CheckoutSubmitStart(
    id: String?,
    params: CheckoutSubmitStartEvent,
    responseSerializer: kotlinx.serialization.KSerializer<CheckoutSubmitStartResponsePayload>
) : RPCRequest<CheckoutSubmitStartEvent, CheckoutSubmitStartResponsePayload>(id, params, responseSerializer) {

    override val method: String = SUBMIT_START_METHOD

    public companion object : TypeErasedRPCDecodable by RPCDecoder.create(
        method = SUBMIT_START_METHOD,
        factory = ::CheckoutSubmitStart
    )
}

/**
 * Parameters for the submit start RPC event.
 */
@Serializable
public data class CheckoutSubmitStartEvent(
    public val cart: Cart,
    public val checkout: Checkout
)

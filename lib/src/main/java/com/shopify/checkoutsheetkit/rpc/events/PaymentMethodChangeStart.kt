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

import com.shopify.checkoutsheetkit.PaymentMethodChangePayload
import com.shopify.checkoutsheetkit.PaymentMethodChangeStartParams
import com.shopify.checkoutsheetkit.rpc.RPCDecoder
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.TypeErasedRPCDecodable

private const val PAYMENT_METHOD_CHANGE_START_METHOD = "checkout.paymentMethodChangeStart"

/**
 * RPC request for payment method change requests from checkout.
 * This replaces the deprecated CheckoutCardChangeRequested event.
 */
public class PaymentMethodChangeStart(
    id: String?,
    params: PaymentMethodChangeStartParams,
    responseSerializer: kotlinx.serialization.KSerializer<PaymentMethodChangePayload>
) : RPCRequest<PaymentMethodChangeStartParams, PaymentMethodChangePayload>(id, params, responseSerializer) {

    override val method: String = PAYMENT_METHOD_CHANGE_START_METHOD

    public companion object : TypeErasedRPCDecodable by RPCDecoder.create(
        method = PAYMENT_METHOD_CHANGE_START_METHOD,
        factory = ::PaymentMethodChangeStart
    )
}
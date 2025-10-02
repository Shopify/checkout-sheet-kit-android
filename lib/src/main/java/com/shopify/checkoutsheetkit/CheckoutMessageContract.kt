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

internal object CheckoutMessageContract {
    const val BODY_KEY = "body"
    const val VERSION_FIELD = "jsonrpc"
    const val METHOD_FIELD = "method"
    const val PARAMS_FIELD = "params"
    const val ID_FIELD = "id"

    const val VERSION = "2.0"

    const val METHOD_ADDRESS_CHANGE_REQUESTED = "checkout.addressChangeRequested"
    const val METHOD_SET_DELIVERY_ADDRESS = "checkout.setDeliveryAddress"
    const val METHOD_COMPLETED = "checkout.completed"

    const val EVENT_ADDRESS_CHANGE_REQUESTED = "addressChangeRequested"
    const val EVENT_COMPLETED = "completed"
}

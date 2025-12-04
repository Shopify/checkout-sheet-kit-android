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

/**
 * Interface to enable type-erased decoding of RPC messages.
 * Mirrors the Swift TypeErasedRPCDecodable protocol.
 *
 * This interface allows us to decode both RPC requests and notifications without knowing
 * their specific type parameters at compile time, working around Kotlin's type erasure limitations.
 *
 * Implement this in the companion object of your RPC request/notification classes.
 */
internal interface TypeErasedRPCDecodable {
    /**
     * The RPC method name that this decoder handles.
     * (e.g., "checkout.addressChangeStart", "checkout.start")
     */
    val method: String

    /**
     * Decode an RPC message from a JSON string without type parameters.
     *
     * @param jsonString The JSON string to decode
     * @return The decoded event (CheckoutNotification for notifications, CheckoutRequest for requests)
     */
    fun decodeErased(jsonString: String): CheckoutNotification
}
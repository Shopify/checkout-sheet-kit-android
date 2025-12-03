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

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutEventResponseException

/**
 * Base interface for all checkout events (notifications and requests).
 */
public interface CheckoutNotification {
    /**
     * The method name that identifies this event type.
     * e.g., "checkout.start", "checkout.addressChangeStart"
     */
    public val method: String
}

/**
 * Interface for checkout events that expect a response (bidirectional).
 *
 * @param R The type of response payload this request expects
 */
public interface CheckoutRequest<R> : CheckoutNotification {
    /**
     * Unique identifier for this request, used to correlate responses.
     */
    public val id: String

    /**
     * Respond with a strongly-typed payload.
     *
     * @param payload The response payload
     */
    public fun respondWith(payload: R)

    /**
     * Respond with a JSON string payload.
     * Useful for language bindings (e.g., React Native).
     *
     * @param jsonString A JSON string representing the response payload
     * @throws CheckoutEventResponseException.DecodingFailed if JSON parsing or deserialization fails
     */
    public fun respondWith(jsonString: String)
}

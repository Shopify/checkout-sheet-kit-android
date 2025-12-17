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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class CheckoutErrorCode {
    @SerialName("INVALID_PAYLOAD")
    INVALID_PAYLOAD,

    @SerialName("INVALID_SIGNATURE")
    INVALID_SIGNATURE,

    @SerialName("NOT_AUTHORIZED")
    NOT_AUTHORIZED,

    @SerialName("PAYLOAD_EXPIRED")
    PAYLOAD_EXPIRED,

    @SerialName("CUSTOMER_ACCOUNT_REQUIRED")
    CUSTOMER_ACCOUNT_REQUIRED,

    @SerialName("STOREFRONT_PASSWORD_REQUIRED")
    STOREFRONT_PASSWORD_REQUIRED,

    @SerialName("CART_COMPLETED")
    CART_COMPLETED,

    @SerialName("INVALID_CART")
    INVALID_CART,

    @SerialName("KILLSWITCH_ENABLED")
    KILLSWITCH_ENABLED,

    @SerialName("UNRECOVERABLE_FAILURE")
    UNRECOVERABLE_FAILURE,

    @SerialName("POLICY_VIOLATION")
    POLICY_VIOLATION,

    @SerialName("VAULTED_PAYMENT_ERROR")
    VAULTED_PAYMENT_ERROR
}

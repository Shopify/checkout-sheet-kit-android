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

import kotlinx.serialization.Serializable

/**
 * Types used for address change requests and responses.
 * These represent the data structures for communication with the checkout WebView.
 */

@Serializable
public data class DeliveryAddressChangePayload(
    val delivery: CartDelivery,
)

@Serializable
public class CartDelivery(
    public val addresses: List<CartSelectableAddressInput>,
)

@Serializable
public data class CartSelectableAddressInput(
    public val address: CartDeliveryAddressInput,
)

@Serializable
public data class CartDeliveryAddressInput(
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val company: String? = null,
    public val address1: String? = null,
    public val address2: String? = null,
    public val city: String? = null,
    public val countryCode: String? = null,
    public val phone: String? = null,
    public val provinceCode: String? = null,
    public val zip: String? = null,
)
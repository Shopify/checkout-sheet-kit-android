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
 * Types used for payment method change requests and responses.
 * These represent the data structures for communication with the checkout WebView.
 */

/**
 * Represents the current card information sent from the WebView
 */
@Serializable
public data class CurrentCard(
    val last4: String,
    val brand: String,
)

/**
 * Parameters for the payment method change request
 */
@Serializable
public data class PaymentMethodChangeStartParams(
    val currentCard: CurrentCard? = null,
)

/**
 * Response payload for payment method change
 */
@Serializable
public data class PaymentMethodChangePayload(
    val card: PaymentCard,
    val billing: BillingInfo,
)

/**
 * Payment card information for the response
 */
@Serializable
public data class PaymentCard(
    val last4: String,
    val brand: String,
) {
    init {
        require(last4.length == 4) { "last4 must be exactly 4 characters" }
        require(brand.isNotEmpty()) { "brand must not be empty" }
    }
}

/**
 * Billing information for the payment method
 */
@Serializable
public data class BillingInfo(
    val useDeliveryAddress: Boolean,
    val address: CartDeliveryAddressInput? = null,
) {
    init {
        if (!useDeliveryAddress) {
            requireNotNull(address) { "address is required when useDeliveryAddress is false" }
            address.countryCode?.let {
                require(it.isNotEmpty()) { "countryCode cannot be empty when provided" }
            }
        }
    }
}
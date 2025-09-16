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
package com.shopify.checkout_sdk_mobile_buy_integration_sample

import com.shopify.checkoutsheetkit.CartDelivery
import com.shopify.checkoutsheetkit.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.DeliveryAddressChangePayload

/**
 * Sample app's address representation.
 * This demonstrates how clients can have their own address format.
 */
data class ClientDefinedAddress(
    val firstName: String,
    val lastName: String,
    val address1: String,
    val address2: String? = null,
    val city: String,
    val province: String,
    val country: String,
    val zip: String,
    val phone: String? = null
)

/**
 * Extension function to convert the sample app's address format
 * to the library's expected DeliveryAddressChangePayload format.
 */
fun ClientDefinedAddress.toDeliveryAddressChangePayload(): DeliveryAddressChangePayload {
    return DeliveryAddressChangePayload(
        delivery = CartDelivery(
            addresses = listOf(
                CartSelectableAddressInput(
                    address = CartDeliveryAddressInput(
                        firstName = this.firstName,
                        lastName = this.lastName,
                        address1 = this.address1,
                        address2 = this.address2,
                        city = this.city,
                        provinceCode = this.province,
                        countryCode = this.country,
                        zip = this.zip,
                        phone = this.phone
                    )
                )
            )
        )
    )
}

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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data

import com.shopify.buy3.Storefront

/**
 * Example data that will be passed to cartCreate/buyerIdentityUpdate to prefill checkout
 * if the 'Create Cart With Buyer Identity' setting is enabled
 *
 * Note checkout will only use this delivery information if the store delivers to that address
 */
object DemoBuyerIdentity {
    internal val value = Storefront.CartBuyerIdentityInput()
        .setEmail("john.smith@example.com")
        .setCountryCode(Storefront.CountryCode.US)
        .setPhone("+12125551234")
        .setDeliveryAddressPreferences(
            listOf(
                Storefront.DeliveryAddressInput().setDeliveryAddress(
                    Storefront.MailingAddressInput()
                        .setAddress1("150 5th Avenue")
                        .setCity("New York")
                        .setCountry("US")
                        .setFirstName("John")
                        .setLastName("Smith")
                        .setProvince("NY")
                        .setPhone("+12125551234")
                        .setZip("10011")
                ),
                Storefront.DeliveryAddressInput().setDeliveryAddress(
                    Storefront.MailingAddressInput()
                        .setAddress1("89 Haight Street")
                        .setAddress2("Apt 2B")
                        .setCity("San Francisco")
                        .setCountry("US")
                        .setFirstName("John")
                        .setLastName("Smith")
                        .setProvince("CA")
                        .setPhone("+12125551234")
                        .setZip("94117")
                )
            )
        )
}

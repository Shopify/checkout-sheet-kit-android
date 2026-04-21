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

import com.apollographql.apollo.api.Optional
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartAddressInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartBuyerIdentityInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartDeliveryAddressInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartSelectableAddressInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CountryCode

object DemoBuyerIdentity {
    internal val value = CartBuyerIdentityInput(
        email = Optional.present("example.customer@shopify.com"),
        countryCode = Optional.present(CountryCode.CA),
        phone = Optional.present("+441792123456"),
    )

    internal val deliveryAddresses = listOf(
        CartSelectableAddressInput(
            address = CartAddressInput(
                deliveryAddress = Optional.present(
                    CartDeliveryAddressInput(
                        address1 = Optional.present("The Cloak & Dagger"),
                        address2 = Optional.present("1st Street Southeast"),
                        city = Optional.present("Calgary"),
                        countryCode = Optional.present(CountryCode.CA),
                        firstName = Optional.present("Ada"),
                        lastName = Optional.present("Lovelace"),
                        provinceCode = Optional.present("AB"),
                        phone = Optional.present("+441792123456"),
                        zip = Optional.present("T1X 0L3"),
                    )
                )
            )
        ),
        CartSelectableAddressInput(
            address = CartAddressInput(
                deliveryAddress = Optional.present(
                    CartDeliveryAddressInput(
                        address1 = Optional.present("8 Lon Heddwch"),
                        address2 = Optional.present("Llansamlet"),
                        city = Optional.present("Swansea"),
                        countryCode = Optional.present(CountryCode.GB),
                        firstName = Optional.present("Ada"),
                        lastName = Optional.present("Lovelace"),
                        phone = Optional.present("+441792123456"),
                        zip = Optional.present("SA7 9UY"),
                    )
                )
            )
        ),
    )
}

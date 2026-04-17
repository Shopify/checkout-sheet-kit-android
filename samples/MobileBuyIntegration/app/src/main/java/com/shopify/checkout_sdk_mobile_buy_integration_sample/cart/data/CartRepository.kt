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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartBuyerIdentityInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartDeliveryInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartLineUpdateInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CountryCode
import timber.log.Timber

class CartRepository(
    private val storefrontApiClient: StorefrontApiClient,
) {

    suspend fun createCart(
        variantId: ID,
        quantity: Int,
        demoBuyerIdentityEnabled: Boolean,
        customerAccessToken: String?,
    ): CartState.Cart {
        val input = CartInput(
            lines = Optional.present(
                listOf(
                    CartLineInput(
                        merchandiseId = variantId.id,
                        quantity = Optional.present(quantity),
                    )
                )
            ),
            buyerIdentity = Optional.present(buyerIdentity(demoBuyerIdentityEnabled, customerAccessToken)),
            delivery = if (demoBuyerIdentityEnabled && customerAccessToken == null) {
                Optional.present(CartDeliveryInput(addresses = Optional.present(DemoBuyerIdentity.deliveryAddresses)))
            } else {
                Optional.Absent
            },
        )

        val data = storefrontApiClient.createCart(input)
        val cartCreate = data.cartCreate
        val cart = cartCreate?.cart

        if (cart == null) {
            val errors = cartCreate?.userErrors?.joinToString { "${it.field} - ${it.message}" }
            throw RuntimeException("Failed to create cart, $errors")
        }

        Timber.i("cart ${cart.cartFragment.checkoutUrl}")
        return cart.cartFragment.toLocal()
    }

    suspend fun addCartLine(cartId: ID, variantId: ID, quantity: Int): CartState.Cart {
        val line = CartLineInput(
            merchandiseId = variantId.id,
            quantity = Optional.present(quantity),
        )

        val data = storefrontApiClient.cartLinesAdd(cartId = cartId.id, lines = listOf(line))
        val cart = data.cartLinesAdd?.cart
            ?: throw RuntimeException("Failed to add cart line")

        return cart.cartFragment.toLocal()
    }

    suspend fun modifyCartLine(cartId: ID, lineItemId: ID, quantity: Int?): CartState.Cart {
        if (quantity != null) {
            val line = CartLineUpdateInput(
                id = lineItemId.id,
                quantity = Optional.present(quantity),
            )
            val data = storefrontApiClient.cartLinesUpdate(cartId = cartId.id, lines = listOf(line))
            val cart = data.cartLinesUpdate?.cart
                ?: throw RuntimeException("Failed to modify cart")
            return cart.cartFragment.toLocal()
        } else {
            val data = storefrontApiClient.cartLinesRemove(cartId = cartId.id, lineIds = listOf(lineItemId.id))
            val cart = data.cartLinesRemove?.cart
                ?: throw RuntimeException("Failed to modify cart")
            return cart.cartFragment.toLocal()
        }
    }

    private fun buyerIdentity(demoBuyerIdentityEnabled: Boolean, customerAccessToken: String?): CartBuyerIdentityInput {
        if (customerAccessToken != null) {
            Timber.i("Setting a customer access token in buyer identity")
            return CartBuyerIdentityInput(customerAccessToken = Optional.present(customerAccessToken))
        }

        return if (demoBuyerIdentityEnabled) {
            Timber.i("Using demo buyer identity data to prefill checkout")
            DemoBuyerIdentity.value
        } else {
            CartBuyerIdentityInput(countryCode = Optional.present(CountryCode.CA))
        }
    }
}

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
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.source.network.CartStorefrontApiClient
import com.shopify.graphql.support.ID
import kotlin.coroutines.suspendCoroutine

class CartRepository(
    private val cartStorefrontApiClient: CartStorefrontApiClient,
) {

    suspend fun createCart(variantId: String, quantity: Int, demoBuyerIdentityEnabled: Boolean): CartState.Cart {
        return suspendCoroutine { continuation ->
            val buyerIdentity = if (demoBuyerIdentityEnabled) {
                DemoBuyerIdentity.value
            } else {
                Storefront.CartBuyerIdentityInput().setCountryCode(Storefront.CountryCode.CA)
            }

            cartStorefrontApiClient.createCart(
                variant = Storefront.ProductVariant(ID(variantId)),
                buyerIdentity = buyerIdentity,
                quantity = quantity,
                successCallback = { response ->
                    val cartCreateResponse = response.data?.cartCreate
                    if (cartCreateResponse == null) {
                        continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart")))
                    } else {
                        continuation.resumeWith(Result.success(cartCreateResponse.cart.toLocal()))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart", exception)))
                }
            )
        }
    }

    suspend fun addCartLine(cartId: String, variantId: String, quantity: Int): CartState.Cart {
        val line = CartLineInput(ID(variantId)).setQuantity(quantity)
        return suspendCoroutine { continuation ->
            cartStorefrontApiClient.cartLinesAdd(
                lines = listOf(line),
                cartId = ID(cartId),
                successCallback = { response ->
                    val cartLinesAddResponse = response.data?.cartLinesAdd
                    if (cartLinesAddResponse == null) {
                        continuation.resumeWith(Result.failure(RuntimeException("Failed to add cart line")))
                    } else {
                        continuation.resumeWith(Result.success(cartLinesAddResponse.cart.toLocal()))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to add cart line", exception)))
                })
        }
    }

    suspend fun modifyCartLine(cartId: String, lineItemId: String, quantity: Int?): CartState.Cart {
        return suspendCoroutine { continuation ->
            cartStorefrontApiClient.cartLinesModify(
                cartId = ID(cartId),
                lineItemId = ID(lineItemId),
                quantity = quantity,
                successCallback = { response ->
                    val cartResult =
                        if (quantity != null) response.data?.cartLinesUpdate?.cart
                        else response.data?.cartLinesRemove?.cart

                    if (cartResult == null) {
                        continuation.resumeWith(Result.failure(RuntimeException("Failed to modify cart")))
                    } else {
                        continuation.resumeWith(Result.success(cartResult.toLocal()))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to modify cart", exception)))
                })
        }
    }
}

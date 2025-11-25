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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.toGraphQLId
import timber.log.Timber
import kotlin.coroutines.suspendCoroutine

class CartRepository(
    private val cartStorefrontApiClient: CartStorefrontApiClient,
) {

    suspend fun createCart(
        variantId: ID,
        quantity: Int,
        demoBuyerIdentityEnabled: Boolean,
        customerAccessToken: String?,
    ): CartState.Cart {
        return suspendCoroutine { continuation ->
            cartStorefrontApiClient.createCart(
                variant = Storefront.ProductVariant(variantId.toGraphQLId()),
                buyerIdentity = buyerIdentity(demoBuyerIdentityEnabled, customerAccessToken),
                quantity = quantity,
                successCallback = { response ->
                    val cartCreateResponse = response.data?.cartCreate
                    if (cartCreateResponse?.cart == null) {
                        val errors = cartCreateResponse?.userErrors?.joinToString { "${it.field} - ${it.message}" }
                        continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart, $errors")))
                    } else {
                        Timber.i("cart ${cartCreateResponse.cart.checkoutUrl}")
                        val cart = cartCreateResponse.cart

                        // If demo buyer identity is enabled and no customer access token, add delivery addresses
                        if (demoBuyerIdentityEnabled && customerAccessToken == null) {
                            addDeliveryAddresses(cart, continuation)
                        } else {
                            continuation.resumeWith(Result.success(cart.toLocal()))
                        }
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart", exception)))
                }
            )
        }
    }

    private fun addDeliveryAddresses(
        cart: Storefront.Cart,
        continuation: kotlin.coroutines.Continuation<CartState.Cart>
    ) {
        cartStorefrontApiClient.cartDeliveryAddressesAdd(
            cartId = cart.id,
            deliveryAddresses = DemoBuyerIdentity.deliveryAddresses,
            successCallback = { response ->
                val cartDeliveryAddressesAddResponse = response.data?.cartDeliveryAddressesAdd
                if (cartDeliveryAddressesAddResponse?.cart == null) {
                    val errors = cartDeliveryAddressesAddResponse?.userErrors?.joinToString { "${it.field} - ${it.message}" }
                    Timber.w("Failed to add delivery addresses, $errors")
                    // Don't fail the entire cart creation if delivery addresses fail to add
                    continuation.resumeWith(Result.success(cart.toLocal()))
                } else {
                    Timber.i("Added delivery addresses to cart")
                    continuation.resumeWith(Result.success(cartDeliveryAddressesAddResponse.cart.toLocal()))
                }
            },
            failureCallback = { exception ->
                Timber.w("Failed to add delivery addresses", exception)
                // Don't fail the entire cart creation if delivery addresses fail to add
                continuation.resumeWith(Result.success(cart.toLocal()))
            }
        )
    }

    suspend fun addCartLine(cartId: ID, variantId: ID, quantity: Int): CartState.Cart {
        val line = CartLineInput(variantId.toGraphQLId()).setQuantity(quantity)
        return suspendCoroutine { continuation ->
            cartStorefrontApiClient.cartLinesAdd(
                lines = listOf(line),
                cartId = cartId.toGraphQLId(),
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

    suspend fun modifyCartLine(cartId: ID, lineItemId: ID, quantity: Int?): CartState.Cart {
        return suspendCoroutine { continuation ->
            cartStorefrontApiClient.cartLinesModify(
                cartId = cartId.toGraphQLId(),
                lineItemId = lineItemId.toGraphQLId(),
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

    /**
     * Build up CartBuyerIdentityInput to pass into cart mutations, with value depending on
     * whether a customer access token is available, and the demo buyer identity setting is enabled
     */
    private fun buyerIdentity(demoBuyerIdentityEnabled: Boolean, customerAccessToken: String?): Storefront.CartBuyerIdentityInput {
        // Give precedence to customer access token
        if (customerAccessToken != null) {
            // Attach token to authenticate
            Timber.i("Setting a customer access token in buyer identity")
            return Storefront.CartBuyerIdentityInput().setCustomerAccessToken(customerAccessToken)
        }

        return if (demoBuyerIdentityEnabled) {
            // No token available, instead of authenticating, prefill checkout with known customer data
            Timber.i("Using demo buyer identity data to prefill checkout")
            DemoBuyerIdentity.buyerIdentity
        } else {
            // We could e.g. set a country code in the absence of other customer information
            Storefront.CartBuyerIdentityInput().setCountryCode(Storefront.CountryCode.CA)
        }
    }
}

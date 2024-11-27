package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data

import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.source.network.CartStorefrontApiClient
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.suspendCoroutine

class CartRepository(
    private val cartStorefrontApiClient: CartStorefrontApiClient,
) {

    suspend fun createCart(variantId: String, quantity: Int, demoBuyerIdentityEnabled: Boolean): Flow<CartState.Cart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartCreateResponse.cart.toLocal())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart", exception)))
                }
            )
        }
    }

    suspend fun addCartLine(cartId: String, variantId: String, quantity: Int): Flow<CartState.Cart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartLinesAddResponse.cart.toLocal())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to add cart line", exception)))
                })
        }
    }

    suspend fun modifyCartLine(cartId: String, lineItemId: String, quantity: Int?): Flow<CartState.Cart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartResult.toLocal())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to modify cart", exception)))
                })
        }
    }
}

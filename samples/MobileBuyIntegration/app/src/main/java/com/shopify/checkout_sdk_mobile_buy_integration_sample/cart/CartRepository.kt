package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart

import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.suspendCoroutine

class CartRepository(
    private val cartStorefrontApiClient: CartStorefrontApiClient,
) {

    suspend fun createCart(variantId: String, quantity: Int, demoBuyerIdentityEnabled: Boolean): Flow<CartState.UICart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartCreateResponse.cart.toUICart())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to create cart", exception)))
                }
            )
        }
    }

    suspend fun addCartLine(cartId: String, variantId: String, quantity: Int): Flow<CartState.UICart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartLinesAddResponse.cart.toUICart())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to add cart line", exception)))
                })
        }
    }

    suspend fun modifyCartLine(cartId: String, lineItemId: String, quantity: Int?): Flow<CartState.UICart> {
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
                        continuation.resumeWith(Result.success(flowOf(cartResult.toUICart())))
                    }
                },
                failureCallback = { exception ->
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to modify cart", exception)))
                })
        }
    }

    private fun Storefront.Cart.toUICart(): CartState.UICart {
        return CartState.UICart(
            cartID = id.toString(),
            cartLines = this.lines.nodes.mapNotNull { cartLine -> cartLine.toUICartLine() },
            cartTotals = CartTotals(
                totalAmount = Amount(
                    currency = cost.totalAmount.currencyCode.name,
                    price = cost.totalAmount.amount.toDouble(),
                ),
                totalAmountEstimated = cost.totalAmountEstimated,
                totalQuantity = totalQuantity
            ),
            checkoutUrl = checkoutUrl,
        )
    }

    private fun Storefront.BaseCartLine.toUICartLine(): CartLine? {
        return (this.merchandise as? Storefront.ProductVariant)?.let {
            CartLine(
                id = this.id.toString(),
                imageURL = it.product.featuredImage.url,
                imageAltText = it.product.featuredImage.altText ?: "",
                title = it.product.title,
                vendor = it.product.vendor,
                quantity = this.quantity,
                pricePerQuantity = this.cost.amountPerQuantity.amount.toDouble(),
                currencyPerQuantity = this.cost.amountPerQuantity.currencyCode.name,
                totalPrice = this.cost.totalAmount.amount.toDouble(),
                totalCurrency = this.cost.totalAmount.currencyCode.name,
            )
        }
    }
}

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
package com.shopify.checkout_kit_mobile_buy_integration_sample.cart.data.source.network

import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CartBuyerIdentityInput
import com.shopify.buy3.Storefront.CartInput
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.buy3.Storefront.CartLineUpdateInput
import com.shopify.buy3.Storefront.CartQuery
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor
import com.shopify.graphql.support.ID
import com.shopify.graphql.support.Input

class CartStorefrontApiClient(
    private val executor: StorefrontApiRequestExecutor,
) {

    fun cartLinesModify(
        cartId: ID,
        lineItemId: ID,
        quantity: Int?,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {
        val mutation = if (quantity != null) {
            Storefront.mutation { mutation ->
                mutation.cartLinesUpdate(cartId, listOf(CartLineUpdateInput(lineItemId).setQuantity(quantity))) { cartLinesUpdate ->
                    cartLinesUpdate.cart { cartQuery ->
                        cartQueryFragment(cartQuery)
                    }
                }
            }
        } else {
            Storefront.mutation { mutation ->
                mutation.cartLinesRemove(cartId, listOf(lineItemId)) { cartLinesRemove ->
                    cartLinesRemove.cart { cartQuery ->
                        cartQueryFragment(cartQuery)
                    }.userErrors { errors ->
                        errors.message()
                        errors.code()
                        errors.field()
                    }
                }
            }
        }

        executor.executeMutation(mutation, successCallback, failureCallback)
    }

    fun createCart(
        variant: Storefront.ProductVariant,
        buyerIdentity: CartBuyerIdentityInput?,
        quantity: Int,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val mutation = Storefront.mutation { mutation ->
            mutation.cartCreate({ cartCreate ->
                cartCreate.input(
                    CartInput().setLinesInput(
                        Input.value(
                            listOf(CartLineInput(variant.id).setQuantity(quantity))
                        )
                    ).setBuyerIdentityInput(
                        Input.value(buyerIdentity)
                    )
                )
            }) { createCart ->
                createCart.cart { cartQuery ->
                    cartQueryFragment(cartQuery)
                }.userErrors { errors ->
                    errors.message()
                    errors.code()
                    errors.field()
                }
            }
        }

        executor.executeMutation(mutation, successCallback, failureCallback)
    }

    fun cartLinesAdd(
        lines: List<CartLineInput>,
        cartId: ID,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val mutation = Storefront.mutation { mutation ->
            mutation.cartLinesAdd(cartId, lines) { cartLinesAddPayload ->
                cartLinesAddPayload.cart { cartQuery ->
                    cartQueryFragment(cartQuery)
                }.userErrors { errors ->
                    errors.message()
                    errors.code()
                    errors.field()
                }
            }
        }

        executor.executeMutation(mutation, successCallback, failureCallback)
    }

    private fun cartQueryFragment(cartQuery: CartQuery): CartQuery {
        return cartQuery
            .checkoutUrl()
            .totalQuantity()
            .cost { costQuery ->
                costQuery.totalAmount { totalAmount ->
                    totalAmount.amount()
                    totalAmount.currencyCode()
                }
                costQuery.totalAmountEstimated()
            }
            .lines({ it.first(250) }) { lineQuery ->
                lineQuery.nodes { line ->
                    line.id()
                    line.quantity()
                    line.cost { cost ->
                        cost.totalAmount { total ->
                            total.amount()
                            total.currencyCode()
                        }
                        cost.amountPerQuantity {
                            it.amount()
                            it.currencyCode()
                        }
                    }
                    line.merchandise { merchandise ->
                        merchandise.onProductVariant { variant ->
                            variant.price { price ->
                                price.amount()
                                price.currencyCode()
                            }
                            variant.title()
                            variant.product { product ->
                                product.title()
                                product.vendor()
                                product.featuredImage { image ->
                                    image.url()
                                    image.altText()
                                }
                            }
                            variant.selectedOptions { option ->
                                option.name()
                                option.value()
                            }
                        }
                    }
                }
            }
    }
}

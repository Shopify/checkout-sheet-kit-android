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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client

import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphClient
import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CartBuyerIdentityInput
import com.shopify.buy3.Storefront.CartInput
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.buy3.Storefront.CartLineUpdateInput
import com.shopify.buy3.Storefront.CartQuery
import com.shopify.buy3.Storefront.MutationQuery
import com.shopify.buy3.Storefront.ProductVariantQuery
import com.shopify.buy3.Storefront.QueryRootQuery
import com.shopify.graphql.support.ID
import com.shopify.graphql.support.Input

class StorefrontClient(private val client: GraphClient) {

    fun fetchHomePageData(
        numCollections: Int,
        numProducts: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.collections({ it.first(numCollections) }) { collectionConnection ->
                collectionConnection.nodes { collection ->
                    collection.handle()
                    collection.title()
                    collection.description()
                    collection.image { image ->
                        image.url()
                        image.altText()
                        image.width()
                        image.height()
                    }
                    collection.products({ it.first(numProducts) }) { productsConnection ->
                        productsConnection.nodes { product ->
                            product.title()
                            product.priceRange { priceRange ->
                                priceRange.maxVariantPrice { variantPrice ->
                                    variantPrice.amount()
                                    variantPrice.currencyCode()
                                }
                                priceRange.minVariantPrice { variantPrice ->
                                    variantPrice.amount()
                                    variantPrice.currencyCode()
                                }
                            }
                            product.featuredImage {
                                it.url()
                                it.height()
                                it.width()
                                it.altText()
                            }
                        }
                    }
                }
            }
        }

        executeQuery(query, successCallback, failureCallback)
    }

    fun fetchProduct(
        productId: ID,
        numVariants: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.product({ it.id(productId) }) { product ->
                product
                    .description()
                    .title()
                    .vendor()
                    .featuredImage { image ->
                        image.url()
                        image.width()
                        image.height()
                        image.altText()
                    }
                    .variants({ it.first(numVariants) }) { productVariant ->
                        productVariant.nodes { productVariantNode: ProductVariantQuery ->
                            productVariantNode.price { price ->
                                price
                                    .amount()
                                    .currencyCode()
                            }
                        }
                    }
            }
        }
        executeQuery(query, successCallback, failureCallback)
    }

    fun cartLinesUpdate(
        cartId: ID,
        lineItemID: ID,
        quantity: Int,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val lineUpdateInput = CartLineUpdateInput(lineItemID).setQuantity(quantity)

        val mutation = Storefront.mutation { mutation ->
            mutation.cartLinesUpdate(
                cartId,
                listOf(lineUpdateInput)
            ) { cartLinesUpdate ->
                cartLinesUpdate.cart { cartQuery ->
                    cartQueryFragment(cartQuery)
                }
            }
        }

        executeMutation(mutation, successCallback, failureCallback)
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
                }
            }
        }

        executeMutation(mutation, successCallback, failureCallback)
    }

    fun cartLinesAdd(
        lines: List<CartLineInput>,
        cartId: ID,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val mutation = Storefront.mutation { mutation ->
            mutation.cartLinesAdd(lines, cartId) { cartLinesAddPayload ->
                cartLinesAddPayload.cart { cartQuery ->
                    cartQueryFragment(cartQuery)
                }
            }
        }

        executeMutation(mutation, successCallback, failureCallback)
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
            }
            .lines({ it.first(250) }) { lineQuery ->
                lineQuery.nodes { line ->
                    line.id()
                    line.quantity()
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
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun executeQuery(
        query: QueryRootQuery,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {
        client.queryGraph(query).enqueue { it: GraphCallResult<Storefront.QueryRoot> ->
            when (it) {
                is GraphCallResult.Success -> {
                    successCallback(it.response)
                }

                is GraphCallResult.Failure -> {
                    failureCallback?.invoke(it.error)
                }
            }
        }
    }

    private fun executeMutation(
        mutation: MutationQuery,
        successCallback: (GraphResponse<Storefront.Mutation>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {
        client.mutateGraph(mutation).enqueue { result: GraphCallResult<Storefront.Mutation> ->
            when (result) {
                is GraphCallResult.Success -> {
                    successCallback(result.response)
                }

                is GraphCallResult.Failure -> {
                    failureCallback?.invoke(result.error)
                }
            }
        }
    }
}

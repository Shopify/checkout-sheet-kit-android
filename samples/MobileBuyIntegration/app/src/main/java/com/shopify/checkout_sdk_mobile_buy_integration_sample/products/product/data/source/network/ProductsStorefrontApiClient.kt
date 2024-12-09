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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.source.network

import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.ProductQuery
import com.shopify.buy3.Storefront.ProductVariantQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor
import com.shopify.graphql.support.ID

class ProductsStorefrontApiClient(
    private val executor: StorefrontApiRequestExecutor,
) {

    fun fetchProduct(
        productId: ID,
        numVariants: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.product({ it.id(productId) }) { product ->
                productFragment(product, numVariants)
            }
        }
        executor.executeQuery(query, successCallback, failureCallback)
    }

    fun fetchProducts(
        numProducts: Int,
        numVariants: Int,
        cursor: String? = null,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.products({ it.first(numProducts).after(cursor) }) { productConnection ->
                productConnection.edges { edges ->
                    edges.node { product ->
                        productFragment(product, numVariants)
                    }
                }
                productConnection.pageInfo { pageInfo ->
                    pageInfo.startCursor()
                    pageInfo.endCursor()
                }
            }
        }
        executor.executeQuery(query, successCallback, failureCallback)
    }


    private fun productFragment(productQuery: ProductQuery, numVariants: Int): ProductQuery {
        return productQuery
            .description()
            .title()
            .vendor()
            .featuredImage { image ->
                image.url()
                image.width()
                image.height()
                image.altText()
            }
            .priceRange { priceRange ->
                priceRange.minVariantPrice { minPrice ->
                    minPrice.amount()
                    minPrice.currencyCode()
                }
                priceRange.maxVariantPrice { maxPrice ->
                    maxPrice.amount()
                    maxPrice.currencyCode()
                }
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

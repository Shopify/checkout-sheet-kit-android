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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network

import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CollectionQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor

class ProductCollectionsStorefrontApiClient(
    private val executor: StorefrontApiRequestExecutor,
) {
    fun fetchCollection(
        handle: String,
        numProducts: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {
        val query = Storefront.query { query ->
            query.collection({ it.handle(handle) }) { collection ->
                collectionFragment(collection, numProducts)
            }
        }

        executor.executeQuery(query, successCallback, failureCallback)
    }

    fun fetchCollections(
        numCollections: Int,
        numProducts: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.collections({ it.first(numCollections) }) { collectionConnection ->
                collectionConnection.nodes { collection ->
                    collectionFragment(collection, numProducts)
                }
            }
        }

        executor.executeQuery(query, successCallback, failureCallback)
    }

    private fun collectionFragment(collectionQuery: CollectionQuery, numProducts: Int): CollectionQuery {
        return collectionQuery.handle()
            .title()
            .description()
            .image { image ->
                image.url()
                image.altText()
                image.width()
                image.height()
            }
            .products({ it.first(numProducts) }) { productsConnection ->
                productsConnection.edges { edges ->
                    edges.node { product ->
                        product.title()
                        product.description()
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

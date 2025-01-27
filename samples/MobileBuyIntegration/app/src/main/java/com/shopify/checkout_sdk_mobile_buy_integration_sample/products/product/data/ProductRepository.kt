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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.source.network.ProductsStorefrontApiClient
import com.shopify.graphql.support.ID
import kotlin.coroutines.suspendCoroutine

class ProductRepository(
    private val client: ProductsStorefrontApiClient,
) {

    suspend fun getProduct(productId: ID): Product {
        return suspendCoroutine { continuation ->
            client.fetchProduct(productId = productId, numVariants = 20, { result ->
                val product = result.data?.product
                if (product == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch product")))
                } else {
                    continuation.resumeWith(Result.success(product.toLocal()))
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }

    suspend fun getProducts(numProducts: Int, numVariants: Int, cursor: String?): Products {
        return suspendCoroutine { continuation ->
            client.fetchProducts(numProducts = numProducts, numVariants = numVariants, cursor = cursor, { response ->
                val products = response.data?.products
                if (products == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch products")))
                } else {
                    val result = Products(
                        products = products.edges.map { it.node.toLocal() },
                        pageInfo = PageInfo(
                            startCursor = products.pageInfo.startCursor,
                            endCursor = products.pageInfo.endCursor,
                        )
                    )
                    continuation.resumeWith(Result.success(result))
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }
}

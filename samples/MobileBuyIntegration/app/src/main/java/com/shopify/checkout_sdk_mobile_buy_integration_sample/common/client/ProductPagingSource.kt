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

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.Product
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProduct
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProductImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProductVariant
import timber.log.Timber
import kotlin.coroutines.suspendCoroutine

class ProductPagingSource(
    private val backend: StorefrontClient,
) : PagingSource<String, UIProduct>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, UIProduct> {
        try {
            val cursor = params.key
            Timber.i("Fetching page of ${params.loadSize} products with cursor $cursor")
            val response = getProducts(params.loadSize, 10, cursor)
            Timber.i("Fetched products $response")
            return response
        } catch (e: Exception) {
            Timber.e("Error when paging through data $e")
            return LoadResult.Error(e)
        }
    }

    private suspend fun getProducts(numProducts: Int, numVariants: Int, cursor: String?): LoadResult<String, UIProduct> {
        return suspendCoroutine { continuation ->
            backend.fetchProducts(numProducts = numProducts, numVariants = numVariants, cursor = cursor, successCallback = { response ->
                val products = response.data?.products
                if (products != null) {
                    continuation.resumeWith(
                        Result.success(
                            LoadResult.Page(
                                data = products.edges.map { it.node.toUIProduct() },
                                prevKey = null,
                                nextKey = products.pageInfo.endCursor,
                            )
                        )
                    )
                } else {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch data")))
                }
            }, failureCallback = {
                continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch data")))
            })
        }
    }

    private fun Product.toUIProduct(): UIProduct {
        val variants = this.variants as Storefront.ProductVariantConnection
        val firstVariant = variants.nodes.first()
        val uiProduct = UIProduct(
            id = id,
            title = title,
            vendor = vendor,
            description = description,
            image = if (featuredImage == null) UIProductImage() else UIProductImage(
                width = featuredImage.width,
                height = featuredImage.height,
                url = featuredImage.url,
                altText = featuredImage.altText ?: "Product image",
            ),
            variants = mutableListOf(
                UIProductVariant(
                    id = firstVariant.id,
                    price = firstVariant.price.amount,
                    currencyName = firstVariant.price.currencyCode.name,
                )
            )
        )
        return uiProduct
    }


    override fun getRefreshKey(state: PagingState<String, UIProduct>): String? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
        }
    }
}

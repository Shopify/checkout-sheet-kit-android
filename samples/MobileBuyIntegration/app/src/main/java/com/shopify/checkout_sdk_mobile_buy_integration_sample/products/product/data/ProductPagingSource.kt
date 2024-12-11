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

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarEvent
import timber.log.Timber

class ProductPagingSource(
    private val repository: ProductRepository,
) : PagingSource<String, Product>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Product> {
        try {
            val cursor = params.key
            Timber.i("Fetching page of ${params.loadSize} products with cursor $cursor")
            val products = repository.getProducts(params.loadSize, 10, cursor)
            return LoadResult.Page(
                data = products.products,
                prevKey = null,
                nextKey = products.pageInfo.endCursor
            )
        } catch (e: Exception) {
            Timber.e("Error when paging through data $e")
            SnackbarController.sendEvent(SnackbarEvent(R.string.products_failed_to_load))
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Product>): String? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
        }
    }
}

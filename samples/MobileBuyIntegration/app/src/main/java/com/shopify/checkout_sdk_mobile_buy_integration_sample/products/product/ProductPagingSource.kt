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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import timber.log.Timber

class ProductPagingSource(
    private val repository: ProductRepository,
) : PagingSource<String, UIProduct>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, UIProduct> {
        try {
            val cursor = params.key
            Timber.i("Fetching page of ${params.loadSize} products with cursor $cursor")
            return repository.getProducts(params.loadSize, 10, cursor).map { res ->
                LoadResult.Page(
                    data = res.products,
                    prevKey = null,
                    nextKey = res.pageInfo.endCursor
                )
            }.single()
        } catch (e: Exception) {
            Timber.e("Error when paging through data $e")
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, UIProduct>): String? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
        }
    }
}

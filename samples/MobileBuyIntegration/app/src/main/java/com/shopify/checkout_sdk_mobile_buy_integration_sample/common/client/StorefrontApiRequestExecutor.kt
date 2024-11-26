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

import android.util.LruCache
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphClient
import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.MutationQuery
import com.shopify.buy3.Storefront.QueryRootQuery
import timber.log.Timber
import java.security.MessageDigest

class StorefrontApiRequestExecutor(
    private val client: GraphClient,
    private val lruCache: LruCache<String, GraphCallResult.Success<Storefront.QueryRoot>>,
) {
    internal fun executeQuery(
        query: QueryRootQuery,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {

        val cachedResponse = lruCache.get(query.cacheKey())
        if (cachedResponse != null) {
            Timber.i("Returning cached response")
            successCallback(cachedResponse.response)
        } else {
            Timber.i("No cached response, sending new request")
            client.queryGraph(query).enqueue { it: GraphCallResult<Storefront.QueryRoot> ->
                when (it) {
                    is GraphCallResult.Success -> {
                        lruCache.put(query.cacheKey(), it)
                        successCallback(it.response)
                    }

                    is GraphCallResult.Failure -> {
                        failureCallback?.invoke(it.error)
                    }
                }
            }
        }
    }

    internal fun executeMutation(
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun QueryRootQuery.cacheKey(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toString().toByteArray())
        return digest.toHexString()
    }
}

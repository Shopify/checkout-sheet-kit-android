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

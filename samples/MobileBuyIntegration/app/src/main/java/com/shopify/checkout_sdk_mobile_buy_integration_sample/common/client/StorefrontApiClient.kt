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

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.CartCreateMutation
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.CartLinesAddMutation
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.CartLinesRemoveMutation
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.CartLinesUpdateMutation
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchCollectionQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchCollectionsQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchProductQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchProductsQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.type.CartLineUpdateInput

class StorefrontApiClient(
    private val apollo: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://${BuildConfig.storefrontDomain}/api/${BuildConfig.storefrontApiVersion}/graphql.json")
        .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
        .addHttpHeader("X-Shopify-Storefront-Access-Token", BuildConfig.storefrontAccessToken)
        .build()
) {
    suspend fun fetchProducts(numProducts: Int, numVariants: Int, cursor: String? = null): FetchProductsQuery.Data {
        val response = apollo.query(
            FetchProductsQuery(
                numProducts = numProducts,
                numVariants = numVariants,
                cursor = Optional.presentIfNotNull(cursor),
            )
        ).execute()
        return response.dataOrThrow()
    }

    suspend fun fetchProduct(productId: String, numVariants: Int): FetchProductQuery.Data {
        val response = apollo.query(
            FetchProductQuery(productId = productId, numVariants = numVariants)
        ).execute()
        return response.dataOrThrow()
    }

    suspend fun fetchCollections(numCollections: Int, numProducts: Int): FetchCollectionsQuery.Data {
        val response = apollo.query(
            FetchCollectionsQuery(numCollections = numCollections, numProducts = numProducts)
        ).execute()
        return response.dataOrThrow()
    }

    suspend fun fetchCollection(handle: String, numProducts: Int): FetchCollectionQuery.Data {
        val response = apollo.query(
            FetchCollectionQuery(handle = handle, numProducts = numProducts)
        ).execute()
        return response.dataOrThrow()
    }

    suspend fun createCart(input: CartInput): CartCreateMutation.Data {
        val response = apollo.mutation(CartCreateMutation(input = input)).execute()
        return response.dataOrThrow()
    }

    suspend fun cartLinesAdd(cartId: String, lines: List<CartLineInput>): CartLinesAddMutation.Data {
        val response = apollo.mutation(CartLinesAddMutation(cartId = cartId, lines = lines)).execute()
        return response.dataOrThrow()
    }

    suspend fun cartLinesUpdate(cartId: String, lines: List<CartLineUpdateInput>): CartLinesUpdateMutation.Data {
        val response = apollo.mutation(CartLinesUpdateMutation(cartId = cartId, lines = lines)).execute()
        return response.dataOrThrow()
    }

    suspend fun cartLinesRemove(cartId: String, lineIds: List<String>): CartLinesRemoveMutation.Data {
        val response = apollo.mutation(CartLinesRemoveMutation(cartId = cartId, lineIds = lineIds)).execute()
        return response.dataOrThrow()
    }
}

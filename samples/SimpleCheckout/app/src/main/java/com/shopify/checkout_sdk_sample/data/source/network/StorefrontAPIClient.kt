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
package com.shopify.checkout_sdk_sample.data.source.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomScalarType
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.shopify.checkout_sdk_sample.BuildConfig
import com.shopify.checkout_sdk_sample.CartCreateMutation
import com.shopify.checkout_sdk_sample.FetchProductsQuery
import com.shopify.checkout_sdk_sample.data.Cart
import com.shopify.checkout_sdk_sample.data.Product
import com.shopify.checkout_sdk_sample.data.source.network.adapters.URLAdapter
import com.shopify.checkout_sdk_sample.data.toLocal

class StorefrontAPIClient(
    private val apollo: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://${BuildConfig.storefrontDomain}/api/2024-10/graphql.json")
        .addCustomScalarAdapter(
            CustomScalarType("URL", "java.net.URL"),
            URLAdapter(),
        )
        .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
        .fetchPolicy(FetchPolicy.CacheFirst)
        .addHttpHeader("X-Shopify-Storefront-Access-Token", BuildConfig.storefrontAccessToken)
        .build()
) {

    suspend fun fetchProducts(numProducts: Int, numVariants: Int): List<Product> {
        val response = apollo
            .query(FetchProductsQuery(numProducts = numProducts, numVariants = numVariants))
            .execute()

        return response.data?.products?.nodes?.map { product -> product.toLocal() } ?: emptyList()
    }

    suspend fun createCart(variantId: String): Cart? {
        val response = apollo
            .mutation(CartCreateMutation(variantId))
            .execute()

        return response.data?.cartCreate?.cart?.toLocal()
    }
}

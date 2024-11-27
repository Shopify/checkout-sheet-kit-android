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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network.CollectionsStorefrontApiClient
import kotlin.coroutines.suspendCoroutine

class CollectionRepository(
    private val client: CollectionsStorefrontApiClient,
) {
    suspend fun getCollections(numberOfCollections: Int, numberOfProductsPerCollection: Int): List<Collection> {
        return suspendCoroutine { continuation ->
            client.fetchCollections(numberOfCollections, numberOfProductsPerCollection, {
                val collections = it.data?.collections
                if (collections == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch collections")))
                } else {
                    continuation.resumeWith(Result.success(collections.nodes.map { it.toLocal() }))
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }

    suspend fun getCollection(collectionHandle: String, numberOfProducts: Int): Collection {
        return suspendCoroutine { continuation ->
            client.fetchCollection(collectionHandle, numberOfProducts, {
                val collection = it.data?.collection
                if (collection == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch collection")))
                } else {
                    continuation.resumeWith(Result.success(collection.toLocal()))
                }
            }, { error ->
                continuation.resumeWith(Result.failure(error))
            })
        }
    }
}

package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection

import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProduct
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.toUIProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.suspendCoroutine

class CollectionRepository(
    private val client: CollectionsStorefrontApiClient,
) {
    suspend fun getCollections(numberOfCollections: Int, numberOfProductsPerCollection: Int): Flow<List<UICollection>> {
        return suspendCoroutine { continuation ->
            client.fetchCollections(numberOfCollections, numberOfProductsPerCollection, {
                val collections = it.data?.collections
                if (collections == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch collections")))
                } else {
                    continuation.resumeWith(Result.success(flowOf(collections.nodes.map { it.toUICollection() })))
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }

    suspend fun getCollection(collectionHandle: String, numberOfProducts: Int): Flow<UICollection> {
        return suspendCoroutine { continuation ->
            client.fetchCollection(collectionHandle, numberOfProducts, {
                val collection = it.data?.collection
                if (collection == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch collection")))
                } else {
                    continuation.resumeWith(Result.success(flowOf(collection.toUICollection())))
                }
            }, { error ->
                continuation.resumeWith(Result.failure(error))
            })
        }
    }
}

fun Storefront.Collection.toUICollection(): UICollection {
    return UICollection(
        id = this.id.toString(),
        handle = this.handle,
        title = this.title,
        description = this.description,
        image = CollectionImage(
            this.image.url,
            this.image.altText
        ),
        products = this.products.edges.map { it.node.toUIProduct() }
    )
}

data class UICollection(
    val id: String = "",
    val handle: String = "",
    val title: String = "",
    val description: String = "",
    val image: CollectionImage = CollectionImage(),
    val products: List<UIProduct> = mutableListOf()
)

data class CollectionImage(
    val url: String? = null,
    val altText: String? = null,
)

package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network

import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CollectionQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor

class CollectionsStorefrontApiClient(
    private val executor: StorefrontApiRequestExecutor,
) {
    fun fetchCollection(
        handle: String,
        numProducts: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)?,
    ) {
        val query = Storefront.query { query ->
            query.collection({ it.handle(handle) }) { collection ->
                collectionFragment(collection, numProducts)
            }
        }

        executor.executeQuery(query, successCallback, failureCallback)
    }

    fun fetchCollections(
        numCollections: Int,
        numProducts: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.collections({ it.first(numCollections) }) { collectionConnection ->
                collectionConnection.nodes { collection ->
                    collectionFragment(collection, numProducts)
                }
            }
        }

        executor.executeQuery(query, successCallback, failureCallback)
    }

    private fun collectionFragment(collectionQuery: CollectionQuery, numProducts: Int): CollectionQuery {
        return collectionQuery.handle()
            .title()
            .description()
            .image { image ->
                image.url()
                image.altText()
                image.width()
                image.height()
            }
            .products({ it.first(numProducts) }) { productsConnection ->
                productsConnection.edges { edges ->
                    edges.node { product ->
                        product.title()
                        product.priceRange { priceRange ->
                            priceRange.maxVariantPrice { variantPrice ->
                                variantPrice.amount()
                                variantPrice.currencyCode()
                            }
                            priceRange.minVariantPrice { variantPrice ->
                                variantPrice.amount()
                                variantPrice.currencyCode()
                            }
                        }
                        product.featuredImage {
                            it.url()
                            it.height()
                            it.width()
                            it.altText()
                        }
                    }
                }
            }
    }

}

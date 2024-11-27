package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.source.network

import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.ProductQuery
import com.shopify.buy3.Storefront.ProductVariantQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor
import com.shopify.graphql.support.ID

class ProductsStorefrontApiClient(
    private val executor: StorefrontApiRequestExecutor,
) {

    fun fetchProduct(
        productId: ID,
        numVariants: Int,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.product({ it.id(productId) }) { product ->
                productFragment(product, numVariants)
            }
        }
        executor.executeQuery(query, successCallback, failureCallback)
    }

    fun fetchProducts(
        numProducts: Int,
        numVariants: Int,
        cursor: String? = null,
        successCallback: (GraphResponse<Storefront.QueryRoot>) -> Unit,
        failureCallback: ((GraphError) -> Unit)? = {},
    ) {
        val query = Storefront.query { query ->
            query.products({ it.first(numProducts).after(cursor) }) { productConnection ->
                productConnection.edges { edges ->
                    edges.node { product ->
                        productFragment(product, numVariants)
                    }
                }
                productConnection.pageInfo { pageInfo ->
                    pageInfo.startCursor()
                    pageInfo.endCursor()
                }
            }
        }
        executor.executeQuery(query, successCallback, failureCallback)
    }


    private fun productFragment(productQuery: ProductQuery, numVariants: Int): ProductQuery {
        return productQuery
            .description()
            .title()
            .vendor()
            .featuredImage { image ->
                image.url()
                image.width()
                image.height()
                image.altText()
            }
            .priceRange { priceRange ->
                priceRange.minVariantPrice { minPrice ->
                    minPrice.amount()
                    minPrice.currencyCode()
                }
                priceRange.maxVariantPrice { maxPrice ->
                    maxPrice.amount()
                    maxPrice.currencyCode()
                }
            }
            .variants({ it.first(numVariants) }) { productVariant ->
                productVariant.nodes { productVariantNode: ProductVariantQuery ->
                    productVariantNode.price { price ->
                        price
                            .amount()
                            .currencyCode()
                    }
                }
            }
    }
}

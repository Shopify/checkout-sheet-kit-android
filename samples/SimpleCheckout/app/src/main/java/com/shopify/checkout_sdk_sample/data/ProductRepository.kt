package com.shopify.checkout_sdk_sample.data

import com.shopify.checkout_sdk_sample.data.source.network.StorefrontAPIClient

class ProductRepository(private val storefrontAPIClient: StorefrontAPIClient = StorefrontAPIClient()) {

    suspend fun find(count: Int, variantCount: Int): List<Product> {
        return storefrontAPIClient.fetchProducts(count, variantCount)
    }
}

package com.shopify.checkout_sdk_sample.data

import com.shopify.checkout_sdk_sample.data.source.network.StorefrontAPIClient

class CartRepository(private val storefrontAPIClient: StorefrontAPIClient = StorefrontAPIClient()) {

    suspend fun create(variantId: String): Cart? {
        return storefrontAPIClient.createCart(variantId)
    }
}

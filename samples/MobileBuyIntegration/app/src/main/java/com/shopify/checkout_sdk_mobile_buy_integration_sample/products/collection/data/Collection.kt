package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product

data class Collection(
    val id: String = "",
    val handle: String = "",
    val title: String = "",
    val description: String = "",
    val image: CollectionImage = CollectionImage(),
    val products: List<Product> = mutableListOf()
)

data class CollectionImage(
    val url: String? = null,
    val altText: String? = null,
)

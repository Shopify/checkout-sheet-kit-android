package com.shopify.checkout_sdk_sample.data

import java.net.URL

/**
 * The application's internal data model
 */
data class Product(
    val title: String,
    val vendor: String?,
    val description: String?,
    val image: ProductImage?,
    val variants: List<ProductVariant>,
    val selectedVariant: Int = 0,
)

data class ProductVariant(
    val price: String,
    val currencyName: String,
    val id: String,
)

data class ProductImage(
    val width: Int?,
    val height: Int?,
    val altText: String?,
    val url: String,
)

data class Cart(
    val checkoutUrl: URL,
)

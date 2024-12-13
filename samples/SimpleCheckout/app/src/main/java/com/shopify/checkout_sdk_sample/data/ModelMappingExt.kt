package com.shopify.checkout_sdk_sample.data

import com.shopify.checkout_sdk_sample.CartCreateMutation
import com.shopify.checkout_sdk_sample.FetchProductsQuery

/**
 * Mappers from Storefront API types to local application model types
 */

fun FetchProductsQuery.Node.toLocal() = Product(
    title = this.title,
    description = this.description,
    vendor = this.vendor,
    variants = this.variants.nodes.map { variant ->
        ProductVariant(
            price = variant.price.amount.toString(),
            currencyName = variant.price.currencyCode.name,
            id = variant.id,
        )
    }.toMutableList(),
    image = ProductImage(
        altText = this.featuredImage?.altText ?: "",
        url = this.featuredImage?.url.toString(),
        width = this.featuredImage?.width ?: 0,
        height = this.featuredImage?.height ?: 0,
    )
)

fun CartCreateMutation.Cart.toLocal() = Cart(
    checkoutUrl = this.checkoutUrl
)

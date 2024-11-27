package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data

import com.shopify.buy3.Storefront

fun Storefront.Product.toLocal(): Product {
    val variants = this.variants
    val firstVariant = variants?.nodes?.firstOrNull()
    val uiProduct = Product(
        id = id.toString(),
        title = title,
        description = description,
        image = if (featuredImage == null) ProductImage() else ProductImage(
            width = featuredImage.width,
            height = featuredImage.height,
            url = featuredImage.url,
            altText = featuredImage.altText ?: "Product image",
        ),
        priceRange = ProductPriceRange(
            minVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.minVariantPrice.currencyCode.name,
                amount = priceRange.minVariantPrice.amount.toDouble()
            ),
            maxVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.maxVariantPrice.currencyCode.name,
                amount = priceRange.maxVariantPrice.amount.toDouble()
            )
        ),
        variants =
        if (firstVariant != null) {
            mutableListOf(
                ProductVariant(
                    id = firstVariant.id.toString(),
                    price = firstVariant.price.amount,
                    currencyName = firstVariant.price.currencyCode.name,
                )
            )
        } else {
            mutableListOf()
        }
    )
    return uiProduct
}

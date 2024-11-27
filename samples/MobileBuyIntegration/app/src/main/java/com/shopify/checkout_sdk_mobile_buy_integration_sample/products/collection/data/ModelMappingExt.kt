package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data

import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.toLocal

internal fun Storefront.Collection.toLocal(): Collection {
    return Collection(
        id = id.toString(),
        handle = handle,
        title = title,
        description = description,
        image = CollectionImage(
            image.url,
            image.altText
        ),
        products = products.edges.map { it.node.toLocal() }
    )
}

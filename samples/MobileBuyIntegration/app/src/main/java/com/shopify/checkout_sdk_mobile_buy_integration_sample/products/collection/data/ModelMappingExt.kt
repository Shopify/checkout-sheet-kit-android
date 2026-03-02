/*
 * MIT License
 *
 * Copyright 2023-present, Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchCollectionQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.FetchCollectionsQuery
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductPriceAmount
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductPriceRange

internal fun FetchCollectionsQuery.Node.toLocal(): ProductCollection {
    return ProductCollection(
        id = ID(id),
        handle = handle,
        title = title,
        description = description,
        image = image?.let { img ->
            ProductCollectionImage(img.url, img.altText)
        },
        products = products.edges.map { it.node.toLocal() },
    )
}

internal fun FetchCollectionsQuery.Node1.toLocal(): Product {
    return Product(
        id = ID(id),
        title = title,
        description = description,
        image = featuredImage?.let { image ->
            ProductImage(
                width = image.width ?: 0,
                height = image.height ?: 0,
                url = image.url,
                altText = image.altText ?: "Product image",
            )
        },
        priceRange = ProductPriceRange(
            minVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.minVariantPrice.currencyCode.rawValue,
                amount = priceRange.minVariantPrice.amount.toDouble(),
            ),
            maxVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.maxVariantPrice.currencyCode.rawValue,
                amount = priceRange.maxVariantPrice.amount.toDouble(),
            ),
        ),
    )
}

internal fun FetchCollectionQuery.Collection.toLocal(): ProductCollection {
    return ProductCollection(
        id = ID(id),
        handle = handle,
        title = title,
        description = description,
        image = image?.let { img ->
            ProductCollectionImage(img.url, img.altText)
        },
        products = products.edges.map { it.node.toLocal() },
    )
}

internal fun FetchCollectionQuery.Node.toLocal(): Product {
    return Product(
        id = ID(id),
        title = title,
        description = description,
        image = featuredImage?.let { image ->
            ProductImage(
                width = image.width ?: 0,
                height = image.height ?: 0,
                url = image.url,
                altText = image.altText ?: "Product image",
            )
        },
        priceRange = ProductPriceRange(
            minVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.minVariantPrice.currencyCode.rawValue,
                amount = priceRange.minVariantPrice.amount.toDouble(),
            ),
            maxVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.maxVariantPrice.currencyCode.rawValue,
                amount = priceRange.maxVariantPrice.amount.toDouble(),
            ),
        ),
    )
}

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

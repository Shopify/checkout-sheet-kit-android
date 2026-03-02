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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.fragment.ProductFragment
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.fragment.ProductVariantFragment

fun ProductFragment.toLocal(variants: List<ProductVariant> = emptyList()): Product {
    return Product(
        id = ID(id),
        title = title,
        description = description,
        image = featuredImage?.let { image ->
            ProductImage(
                width = image.width ?: 0,
                height = image.height ?: 0,
                url = image.url.toString(),
                altText = image.altText ?: "Product image",
            )
        },
        priceRange = ProductPriceRange(
            minVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.minVariantPrice.currencyCode.rawValue,
                amount = priceRange.minVariantPrice.amount.toString().toDouble(),
            ),
            maxVariantPrice = ProductPriceAmount(
                currencyCode = priceRange.maxVariantPrice.currencyCode.rawValue,
                amount = priceRange.maxVariantPrice.amount.toString().toDouble(),
            ),
        ),
        variants = variants,
    )
}

fun ProductVariantFragment.toLocal(): ProductVariant {
    return ProductVariant(
        id = ID(id),
        price = ProductPriceAmount(
            amount = price.amount.toString().toDouble(),
            currencyCode = price.currencyCode.rawValue,
        ),
        title = title,
        availableForSale = availableForSale,
        selectedOptions = selectedOptions.map { option ->
            ProductVariantSelectedOption(option.name, option.value)
        },
    )
}

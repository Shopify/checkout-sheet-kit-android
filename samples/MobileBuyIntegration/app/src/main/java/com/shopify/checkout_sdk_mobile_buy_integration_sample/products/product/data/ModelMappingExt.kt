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

import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.toLocal

fun Storefront.Product.toLocal(): Product {
    val uiProduct = Product(
        id = id.toLocal(),
        title = title,
        description = description,
        image = if (featuredImage != null) ProductImage(
            width = featuredImage.width,
            height = featuredImage.height,
            url = featuredImage.url,
            altText = featuredImage.altText ?: "Product image",
        ) else null,
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
        variants = variants?.nodes?.map { variant ->
            ProductVariant(
                id = variant.id.toLocal(),
                price = ProductPriceAmount(
                    amount = variant.price.amount.toDouble(),
                    currencyCode = variant.price.currencyCode.name,
                ),
                title = variant.title,
                availableForSale = variant.availableForSale,
                selectedOptions = variant.selectedOptions.map { option ->
                    ProductVariantSelectedOption(
                        option.name,
                        option.value
                    )
                }
            )
        } ?: mutableListOf()
    )
    return uiProduct
}

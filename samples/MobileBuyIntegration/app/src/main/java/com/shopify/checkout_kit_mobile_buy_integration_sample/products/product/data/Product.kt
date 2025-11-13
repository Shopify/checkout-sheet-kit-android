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
package com.shopify.checkout_kit_mobile_buy_integration_sample.products.product.data

import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ID

data class Products(
    val products: List<Product> = mutableListOf(),
    val pageInfo: PageInfo = PageInfo(),
)

data class PageInfo(
    val startCursor: String? = null,
    val endCursor: String? = null,
)

data class Product(
    val id: ID,
    val title: String,
    val description: String,
    val image: ProductImage?,
    val priceRange: ProductPriceRange,
    val variants: List<ProductVariant> = mutableListOf(),
)

data class ProductVariant(
    val id: ID,
    val price: ProductPriceAmount,
    val availableForSale: Boolean,
    val title: String,
    val selectedOptions: List<ProductVariantSelectedOption>,
)

data class ProductVariantSelectedOption(
    val name: String,
    val value: String,
)

data class ProductVariantOptionDetails(
    val name: String,
    val availableForSale: Boolean,
)

data class ProductPriceRange(
    val maxVariantPrice: ProductPriceAmount,
    val minVariantPrice: ProductPriceAmount,
)

data class ProductPriceAmount(
    val currencyCode: String = "",
    val amount: Double = 0.0,
)

data class ProductImage(
    val width: Int = 0,
    val height: Int = 0,
    val altText: String? = null,
    val url: String,
)

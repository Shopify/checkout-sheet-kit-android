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
package com.shopify.checkout_sdk_sample

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLResponse<Root>(
    val data: Root,
)

@Serializable
data class QueryRoot(
    val products: Connection<Product>,
)

@Serializable
data class MutationRoot(
    val cartCreate: CartCreatePayload,
)

@Serializable
data class Connection<Node>(
    val nodes: List<Node>,
)

@Serializable
data class Product(
    val title: String,
    val description: String,
    val vendor: String,
    val variants: Connection<ProductVariant>,
    val featuredImage: Image?,
)

@Serializable
data class ProductVariant(
    val id: String,
    val title: String,
    val price: Money,
)

@Serializable
data class Image(
    val url: String,
    val width: Int,
    val height: Int,
    val altText: String?,
)

@Serializable
data class Money(
    val amount: String,
    val currencyCode: String,
)

@Serializable
data class CartCreatePayload(
    val cart: Cart,
)

@Serializable
data class Cart(
    val checkoutUrl: String,
)

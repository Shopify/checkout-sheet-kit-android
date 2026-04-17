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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.graphql.fragment.CartFragment

internal fun CartFragment.toLocal(): CartState.Cart {
    return CartState.Cart(
        cartID = ID(id),
        cartLines = lines.nodes.mapNotNull { node -> node.toLocal() },
        cartTotals = CartTotals(
            totalAmount = CartAmount(
                currency = cost.totalAmount.currencyCode.rawValue,
                price = cost.totalAmount.amount.toString().toDouble(),
            ),
            totalAmountEstimated = cost.totalAmountEstimated,
            totalQuantity = totalQuantity,
        ),
        checkoutUrl = checkoutUrl.toString(),
    )
}

internal fun CartFragment.Node.toLocal(): CartLine? {
    return merchandise.onProductVariant?.let { variant ->
        CartLine(
            id = ID(id),
            image = variant.product.featuredImage?.let { image ->
                CartLineImage(
                    url = image.url.toString(),
                    altText = image.altText,
                )
            },
            title = variant.product.title,
            vendor = variant.product.vendor,
            quantity = quantity,
            pricePerQuantity = cost.amountPerQuantity.amount.toString().toDouble(),
            currencyPerQuantity = cost.amountPerQuantity.currencyCode.rawValue,
            totalPrice = cost.totalAmount.amount.toString().toDouble(),
            totalCurrency = cost.totalAmount.currencyCode.rawValue,
            variantDescription = variant.selectedOptions.toDescription(),
        )
    }
}

fun List<CartFragment.SelectedOption>.toDescription(): String {
    val optionsWithoutTitle = this.filter { option -> option.name != "Title" }
    return optionsWithoutTitle.joinToString(separator = " / ") { option -> option.value }
}

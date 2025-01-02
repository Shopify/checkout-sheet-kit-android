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

import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.toLocal

internal fun Storefront.Cart.toLocal(): CartState.Cart {
    return CartState.Cart(
        cartID = id.toLocal(),
        cartLines = this.lines.nodes.mapNotNull { cartLine -> cartLine.toLocal() },
        cartTotals = CartTotals(
            totalAmount = CartAmount(
                currency = cost.totalAmount.currencyCode.name,
                price = cost.totalAmount.amount.toDouble(),
            ),
            totalAmountEstimated = cost.totalAmountEstimated,
            totalQuantity = totalQuantity
        ),
        checkoutUrl = checkoutUrl,
    )
}

internal fun Storefront.BaseCartLine.toLocal(): CartLine? {
    return (this.merchandise as? Storefront.ProductVariant)?.let {
        CartLine(
            id = this.id.toLocal(),
            image = if (it.product.featuredImage?.url != null) CartLineImage(
                url = it.product.featuredImage.url,
                altText = it.product.featuredImage.altText,
            ) else null,
            title = it.product.title,
            vendor = it.product.vendor,
            quantity = this.quantity,
            pricePerQuantity = this.cost.amountPerQuantity.amount.toDouble(),
            currencyPerQuantity = this.cost.amountPerQuantity.currencyCode.name,
            totalPrice = this.cost.totalAmount.amount.toDouble(),
            totalCurrency = this.cost.totalAmount.currencyCode.name,
            variantDescription = it.selectedOptions.toDescription()
        )
    }
}

fun List<Storefront.SelectedOption>.toDescription(): String {
    val optionsWithoutTitle = this.filter { option -> option.name != "Title" }
    return optionsWithoutTitle.joinToString(separator = " / ") { option -> option.value }
}

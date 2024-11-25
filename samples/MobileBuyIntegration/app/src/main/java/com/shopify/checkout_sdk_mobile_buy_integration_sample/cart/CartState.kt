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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart

import androidx.compose.runtime.Stable
import com.shopify.graphql.support.ID

sealed class CartState {
    data object Empty : CartState()

    @Stable
    data class Populated(
        val cartID: ID,
        val cartLines: List<CartLine>,
        val cartTotals: CartTotals,
        val checkoutUrl: String,
    ) : CartState()

}

data class CartLine(
    val id: ID,
    val title: String,
    val vendor: String,
    val quantity: Int,
    val imageURL: String,
    val imageAltText: String,
    val pricePerQuantity: Double,
    val currencyPerQuantity: String,
    val totalPrice: Double,
    val totalCurrency: String,
)

data class CartTotals(val totalQuantity: Int, val totalAmount: Amount, val totalAmountEstimated: Boolean)
data class Amount(val currency: String, val price: Double)

val CartState.totalQuantity
    get() = when (this) {
        is CartState.Empty -> 0
        is CartState.Populated -> cartTotals.totalQuantity
    }

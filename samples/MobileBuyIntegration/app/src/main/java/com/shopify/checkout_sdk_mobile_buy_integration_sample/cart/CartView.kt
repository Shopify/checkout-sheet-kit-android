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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopify.checkout.CheckoutEventProcessor
import com.shopify.checkout_sdk_mobile_buy_integration_sample.AppBarState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.toDisplayText

@Composable
fun CartView(
    cartViewModel: CartViewModel,
    setAppBarState: (AppBarState) -> Unit,
    checkoutEventProcessor: CheckoutEventProcessor,
) {
    val state = cartViewModel.cartState.collectAsState().value

    LaunchedEffect(state) {
        setAppBarState(
            AppBarState(
                title = "Cart",
                actions = {
                    if (state.totalQuantity > 0) {
                        IconButton(onClick = { cartViewModel.clearCart() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Empty cart",
                            )
                        }
                    }
                }
            )
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val activity = LocalContext.current as ComponentActivity
        when (state) {
            is CartState.Empty -> {
                EmptyCartMessage(Modifier.fillMaxSize())
            }

            is CartState.Populated -> {
                CartLines(
                    lines = state.cartLines,
                    modifier = Modifier.weight(1f, false)
                )
                CheckoutButton(
                    totalAmount = state.cartTotals.totalAmount,
                    onClick = {
                        cartViewModel.presentCheckout(
                            state.checkoutUrl,
                            activity,
                            checkoutEventProcessor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CartLines(lines: List<CartLine>, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(lines) { item ->
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                CartItem(
                    title = item.title,
                    vendor = item.vendor,
                    quantity = item.quantity,
                    modifier = Modifier
                        .fillMaxWidth(.9f)
                        .align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun CheckoutButton(
    totalAmount: Amount,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(.7f),
            onClick = onClick,
        ) {
            Column {
                Text(
                    "Checkout",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    text = toDisplayText(
                        totalAmount.currency,
                        totalAmount.price
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyCartMessage(
    modifier: Modifier,
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.7f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your cart is empty",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Add products while you shop, so they'll be ready for checkout later.",
                color = MaterialTheme.colors.primaryVariant,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
            )
        }
    }
}

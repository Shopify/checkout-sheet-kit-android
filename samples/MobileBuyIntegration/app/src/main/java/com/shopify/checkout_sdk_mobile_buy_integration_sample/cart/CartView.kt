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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.graphql.support.ID

@Composable
fun <T : DefaultCheckoutEventProcessor> CartView(
    navController: NavController,
    cartViewModel: CartViewModel,
    checkoutEventProcessor: T,
) {
    val state = cartViewModel.cartState.collectAsState().value
    val loading = cartViewModel.loadingState.collectAsState().value

    val activity = LocalContext.current as ComponentActivity
    var mutableQuantity by remember { mutableStateOf<Map<String, Int>>(mutableMapOf()) }

    LaunchedEffect(state) {
        if (state is CartState.Populated) {
            ShopifyCheckoutSheetKit.preload(state.checkoutUrl, activity)
        }
    }

    if (loading) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (state) {
            is CartState.Empty -> {
                EmptyCartMessage(Modifier.fillMaxSize())
            }

            is CartState.Populated -> {
                mutableQuantity = state.cartLines.associate {
                    it.title to it.quantity
                }

                CartLines(
                    lines = state.cartLines,
                    loading = loading,
                    setQuantity = cartViewModel::updateCartQuantity,
                    continueShopping = { navController.navigate(Screen.Products.route) },
                    checkout = {
                        cartViewModel.presentCheckout(
                            state.checkoutUrl,
                            activity,
                            checkoutEventProcessor
                        )
                    },
                    totalAmount = state.cartTotals.totalAmount,
                    modifier = Modifier.weight(1f, false),
                )
            }
        }
    }
}

@Composable
private fun CartLines(
    lines: List<CartLine>,
    loading: Boolean,
    totalAmount: Amount,
    setQuantity: (ID, Int) -> Unit,
    continueShopping: () -> Unit,
    checkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Header2(
                    text = stringResource(id = R.string.cart_header)
                )

                BodySmall(
                    text = stringResource(id = R.string.cart_continue_shopping),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            continueShopping()
                        }
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.cart_product_header),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = stringResource(id = R.string.cart_total_header),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                HorizontalDivider()
            }
        }
        items(lines) { item ->
            CartItem(
                cartLine = item,
                setQuantity = { quantity -> setQuantity(item.id, quantity) },
                loading = loading,
            )
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                HorizontalDivider()

                Text(
                    text = stringResource(id = R.string.cart_estimated_total),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(id = R.string.cart_taxes_included),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth()
                )

                CheckoutButton(
                    onClick = { checkout() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CheckoutButton(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            shape = RectangleShape,
            onClick = onClick,
        ) {
            Column {
                Text(
                    stringResource(id = R.string.cart_checkout),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
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
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
            )
        }
    }
}

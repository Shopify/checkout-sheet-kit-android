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
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.MainActivity
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartAmount
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartLine
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.Analytics
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.toAnalyticsEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyText
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.horizontalPadding
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.verticalPadding
import com.shopify.checkoutsheetkit.compose.CheckoutView
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Cart view that displays cart items and handles checkout using the CheckoutView composable.
 */
@Composable
fun CartView(
    navController: NavController,
    cartViewModel: CartViewModel,
) {
    val state = cartViewModel.cartState.collectAsState().value
    val loading = cartViewModel.loadingState.collectAsState().value
    val activity = LocalActivity.current as ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    var showCheckout by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        cartViewModel.preloadCheckout(activity)
    }

    if (loading) {
        ProgressIndicator()
    }

    CheckoutView(
        url = if (showCheckout && state is CartState.Cart) state.checkoutUrl else "",
        isVisible = showCheckout,
        onComplete = { checkoutCompletedEvent ->
            Timber.d("Checkout completed: ${checkoutCompletedEvent.orderDetails.id}")
            showCheckout = false
            cartViewModel.clearCart()
            coroutineScope.launch {
                navController.popBackStack(Screen.Product.route, false)
                SnackbarController.sendEvent(
                    SnackbarEvent(R.string.cart_checkout_completed)
                )
            }
        },
        onCancel = {
            showCheckout = false
        },
        onFail = { error ->
            Timber.e("Checkout failed: ${error.message}")
            showCheckout = false

            if (!error.isRecoverable) {
                cartViewModel.clearCart()
                coroutineScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(R.string.checkout_error)
                    )
                }
            }

            coroutineScope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(R.string.cart_checkout_failed)
                )
            }
        },
        onPixelEvent = { event ->
            Timber.d("Web pixel event: $event")

            // Handle pixel events (transform, augment, and process)
            val analyticsEvent = when (event) {
                is StandardPixelEvent -> event.toAnalyticsEvent()
                is CustomPixelEvent -> event.toAnalyticsEvent()
            }

            analyticsEvent?.let {
                Analytics.record(analyticsEvent)
            }
        },
        onGeolocationPermissionRequest = { origin, callback ->
            (activity as MainActivity).onGeolocationPermissionsShowPrompt(origin, callback)
        },
        onShowFileChooser = { webView, filePathCallback, fileChooserParams ->
            (activity as MainActivity).onShowFileChooser(filePathCallback, fileChooserParams)
        }
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (state) {
            is CartState.Empty -> {
                EmptyCartMessage(Modifier.fillMaxSize())
            }

            is CartState.Cart -> {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    CartLines(
                        lines = state.cartLines,
                        loading = loading,
                        modifyLineItem = cartViewModel::modifyLineItem,
                        continueShopping = { cartViewModel.continueShopping(navController) },
                        checkout = {
                            showCheckout = true
                        },
                        totalAmount = state.cartTotals.totalAmount,
                        totalAmountEstimated = state.cartTotals.totalAmountEstimated,
                        modifier = Modifier.weight(1f, false),
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLines(
    lines: List<CartLine>,
    loading: Boolean,
    totalAmount: CartAmount,
    totalAmountEstimated: Boolean,
    continueShopping: () -> Unit,
    modifyLineItem: (ID, Int?) -> Unit,
    checkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding)
            ) {
                Header2(
                    text = stringResource(id = R.string.cart_header),
                    fontSize = 30.sp,
                )

                BodySmall(
                    text = stringResource(id = R.string.cart_continue_shopping),
                    textDecoration = TextDecoration.Underline,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable {
                            continueShopping()
                        }
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = verticalPadding)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.cart_product_header),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = stringResource(id = R.string.cart_total_header),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                HorizontalDivider()
            }
        }
        items(lines) { item ->
            CartItem(
                cartLine = item,
                modifyLineItem = modifyLineItem,
                loading = loading,
            )
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(verticalPadding),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding)
            ) {
                HorizontalDivider()

                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val resourceId = if (totalAmountEstimated) R.string.cart_estimated_total else R.string.cart_total
                    Text(
                        text = stringResource(id = resourceId),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        modifier = modifier.wrapContentWidth()
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    MoneyText(
                        currency = totalAmount.currency,
                        price = totalAmount.price,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .wrapContentSize()
                    )
                }

                Text(
                    text = stringResource(id = R.string.cart_taxes_included),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header2(
                text = stringResource(id = R.string.cart_empty),
            )
            BodyMedium(
                stringResource(id = R.string.cart_emtpy_description),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

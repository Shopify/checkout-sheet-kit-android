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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.checkout

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.MobileBuyEventProcessor
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkoutsheetkit.CheckoutWebView
import com.shopify.checkoutsheetkit.CheckoutWebViewEventProcessor
import org.koin.compose.koinInject

@Composable
fun InlineCheckoutScreen(
    checkoutUrl: String,
    navController: NavController,
    cartViewModel: CartViewModel,
    logger: Logger = koinInject<Logger>(),
) {
    val activity = LocalActivity.current as ComponentActivity

    if (checkoutUrl.isEmpty()) {
        ProgressIndicator()
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                CheckoutWebView(context).apply {
                    val eventProcessor = CheckoutWebViewEventProcessor(
                        MobileBuyEventProcessor(
                            cartViewModel,
                            navController,
                            logger,
                            activity,
                        )
                    )
                    setEventProcessor(eventProcessor)
                    loadCheckout(checkoutUrl, isPreload = false)
                }
            }
        )
    }
}

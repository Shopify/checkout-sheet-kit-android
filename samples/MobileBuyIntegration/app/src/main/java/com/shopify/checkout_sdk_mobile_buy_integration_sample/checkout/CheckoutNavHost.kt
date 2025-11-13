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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkoutsheetkit.CheckoutWebView
import com.shopify.checkoutsheetkit.CheckoutWebViewEventProcessor

sealed class CheckoutScreen(val route: String) {
    data object Checkout : CheckoutScreen("checkout")
    data object Address : CheckoutScreen("address/{eventId}") {
        fun route(eventId: String): String {
            return "address/$eventId"
        }

        fun getEventId(backStackEntry: NavBackStackEntry): String {
            return backStackEntry.arguments?.getString("eventId") ?: ""
        }
    }
}

/**
 * Checkout container with navigation-tracked overlay.
 * NavHost tracks navigation state, but WebView lives outside destinations to stay alive.
 */
@Composable
fun CheckoutNavHost(
    checkoutUrl: String,
    mainNavController: NavController,
    cartViewModel: CartViewModel,
    logger: Logger,
) {
    val activity = LocalActivity.current as ComponentActivity
    val checkoutNavController = rememberNavController()

    CheckoutEventProvider {
        val eventStore = LocalCheckoutEventStore.current

        // Remember CheckoutWebView
        val checkoutWebView = remember(checkoutUrl) {
            CheckoutWebView(activity).apply {
                val eventProcessor = CheckoutWebViewEventProcessor(
                    CheckoutNavEventProcessor(
                        mainNavController = mainNavController,
                        checkoutNavController = checkoutNavController,
                        logger = logger,
                        context = activity,
                        eventStore = eventStore,
                    )
                )
                setEventProcessor(eventProcessor)
                loadCheckout(checkoutUrl, isPreload = false)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Keep CheckoutWebView in composition tree to survive navigation
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { checkoutWebView }
            )

            // NavHost for checkout flow navigation
            NavHost(
                navController = checkoutNavController,
                startDestination = CheckoutScreen.Checkout.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(CheckoutScreen.Checkout.route) {
                    // Empty - WebView already rendered above
                }

                composable(CheckoutScreen.Address.route) { backStackEntry ->
                    AddressSelectionScreen(
                        eventId = CheckoutScreen.Address.getEventId(backStackEntry),
                        onDismiss = { checkoutNavController.popBackStack() }
                    )
                }
            }
        }
    }
}

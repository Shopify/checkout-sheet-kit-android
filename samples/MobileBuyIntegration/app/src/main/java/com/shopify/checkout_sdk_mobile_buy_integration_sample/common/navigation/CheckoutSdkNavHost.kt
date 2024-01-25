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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.AppBarState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.Analytics
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.toAnalyticsEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.product.ProductView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsViewModel
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Product : Screen("product")
    object Cart : Screen("cart")
    object Settings : Screen("settings")

    companion object {
        fun fromRoute(route: String): Screen {
            return when (route) {
                Product.route -> Product
                Cart.route -> Cart
                Settings.route -> Settings
                else -> throw RuntimeException("Unknown route")
            }
        }
    }
}

@Composable
fun CheckoutSdkNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    cartViewModel: CartViewModel,
    settingsViewModel: SettingsViewModel,
    setAppBarState: (AppBarState) -> Unit,
) {

    fun showToast(context: Context, text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable(Screen.Product.route) {
            ProductView(cartViewModel, setAppBarState)
        }

        composable(Screen.Cart.route) {
            val activity = LocalContext.current
            CartView(
                cartViewModel = cartViewModel,
                setAppBarState = setAppBarState,
                checkoutEventProcessor = object : DefaultCheckoutEventProcessor(activity) {
                    override fun onCheckoutCompleted() {
                        showToast(activity, "Checkout completed")
                        cartViewModel.clearCart()
                        GlobalScope.launch(Dispatchers.Main) {
                            navController.popBackStack(Screen.Product.route, false)
                        }
                    }

                    override fun onCheckoutFailed(error: CheckoutException) {
                        showToast(activity, "Checkout failed with error ${error.message}")
                    }

                    override fun onCheckoutCanceled() {
                        // optionally respond to checkout being canceled/closed
                        showToast(activity, "Checkout canceled")
                    }

                    override fun onWebPixelEvent(event: PixelEvent) {
                        // handle pixel events (e.g. transform, augment, and process), e.g.
                        val analyticsEvent = when (event) {
                            is StandardPixelEvent -> event.toAnalyticsEvent()
                            is CustomPixelEvent -> event.toAnalyticsEvent()
                        }

                        analyticsEvent?.let {
                            Analytics.record(analyticsEvent)
                        }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsView(
                settingsViewModel = settingsViewModel,
                setAppBarState = setAppBarState,
            )
        }
    }
}

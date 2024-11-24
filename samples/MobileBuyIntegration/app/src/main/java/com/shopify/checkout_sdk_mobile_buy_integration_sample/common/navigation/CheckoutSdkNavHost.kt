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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.MobileBuyEventProcessor
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.home.HomeView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.LogsView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.LogsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.ProductsView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.CollectionView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.ProductView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsViewModel
import org.koin.compose.koinInject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Product : Screen("product/{productId}") {
        fun productIdRouteVariable(backStackEntry: NavBackStackEntry): String {
            return backStackEntry.arguments?.getString("productId") ?: ""
        }

        fun route(productId: String): String {
            return route.replace("{productId}", URLEncoder.encode(productId, StandardCharsets.UTF_8.name()))
        }
    }

    data object Products : Screen("product")
    data object Collection : Screen("collection/{collectionHandle}") {
        fun collectionHandleRouteVariable(backStackEntry: NavBackStackEntry): String {
            return backStackEntry.arguments?.getString("collectionHandle") ?: ""
        }

        fun route(collectionHandle: String): String {
            return route.replace("{collectionHandle}", URLEncoder.encode(collectionHandle, StandardCharsets.UTF_8.name()))
        }
    }

    data object Cart : Screen("cart")
    data object Settings : Screen("settings")
    data object Logs : Screen("logs")

    companion object {
        fun fromRoute(route: String): Screen {
            return when (route) {
                Home.route -> Home
                Collection.route -> Collection
                Product.route -> Product
                Products.route -> Products
                Cart.route -> Cart
                Settings.route -> Settings
                Logs.route -> Logs
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
    logsViewModel: LogsViewModel,
    logger: Logger = koinInject<Logger>(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable(Screen.Home.route) {
            HomeView(navController)
        }

        composable(Screen.Products.route) {
            ProductsView(navController)
        }

        composable(Screen.Product.route) { backStackEntry ->
            ProductView(Screen.Product.productIdRouteVariable(backStackEntry))
        }

        composable(Screen.Collection.route) { backStackEntry ->
            CollectionView(navController, Screen.Collection.collectionHandleRouteVariable(backStackEntry))
        }

        composable(Screen.Cart.route) {
            val activity = LocalContext.current
            CartView(
                cartViewModel = cartViewModel,
                navController = navController,
                checkoutEventProcessor = MobileBuyEventProcessor(
                    cartViewModel,
                    navController,
                    logger,
                    activity,
                )
            )
        }

        composable(Screen.Settings.route) {
            SettingsView(
                settingsViewModel = settingsViewModel,
                navController = navController
            )
        }

        composable(Screen.Logs.route) {
            LogsView(
                logsViewModel = logsViewModel,
            )
        }
    }
}

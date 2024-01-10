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
package com.shopify.checkout_sdk_mobile_buy_integration_sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.rememberNavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.BottomAppBarWithNavigation
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.CheckoutSdkNavHost
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.CheckoutSdkSampleTheme
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsUiState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsViewModel
import com.shopify.checkoutsheetkit.ColorScheme
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun CheckoutSdkApp() {
    KoinAndroidContext {
        val settingsViewModel = koinViewModel<SettingsViewModel>()
        val cartViewModel = koinViewModel<CartViewModel>()

        CheckoutSdkAppRoot(settingsViewModel, cartViewModel)
    }
}

@Composable
fun CheckoutSdkAppRoot(
    settingsViewModel: SettingsViewModel,
    cartViewModel: CartViewModel
) {
    val useDarkTheme = settingsViewModel.uiState.collectAsState().value
        .isDarkTheme(isSystemInDarkTheme())

    CheckoutSdkSampleTheme(darkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val navController = rememberNavController()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Product) }
            var appBarState by remember { mutableStateOf(AppBarState()) }

            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    backStackEntry.destination.route?.let {
                        currentScreen = Screen.fromRoute(it)
                    }
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        backgroundColor = MaterialTheme.colors.background,
                        title = {
                            Text(appBarState.title, fontWeight = FontWeight.SemiBold)
                        },
                        actions = {
                            appBarState.actions.invoke(this)
                        },
                    )
                },
                bottomBar = {
                    BottomAppBarWithNavigation(
                        navController,
                        currentScreen,
                        cartViewModel,
                    )
                }
            ) {
                Column(Modifier.padding(paddingValues = it)) {
                    CheckoutSdkNavHost(
                        navController = navController,
                        startDestination = Screen.Product.route,
                        cartViewModel = cartViewModel,
                        settingsViewModel = settingsViewModel,
                        setAppBarState = { state -> appBarState = state },
                    )
                }
            }
        }
    }
}

data class AppBarState(
    val title: String = "",
    val actions: @Composable RowScope.() -> Unit = {},
)

private fun SettingsUiState.isDarkTheme(isSystemInDarkTheme: Boolean) = when (this) {
    is SettingsUiState.Loading -> isSystemInDarkTheme
    is SettingsUiState.Populated -> {
        when (settings.colorScheme) {
            is ColorScheme.Dark -> true
            is ColorScheme.Light, is ColorScheme.Web -> false
            is ColorScheme.Automatic -> isSystemInDarkTheme
        }
    }
}

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
package com.shopify.checkout_kit_mobile_buy_integration_sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.shopify.checkout_kit_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_kit_mobile_buy_integration_sample.cart.data.totalQuantity
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ObserveAsEvents
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.navigation.BottomAppBarWithNavigation
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.navigation.CheckoutKitNavHost
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ui.NativeSheetsOrchestrator
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ui.theme.CheckoutKitSampleTheme
import com.shopify.checkout_kit_mobile_buy_integration_sample.logs.LogsViewModel
import com.shopify.checkout_kit_mobile_buy_integration_sample.settings.SettingsUiState
import com.shopify.checkout_kit_mobile_buy_integration_sample.settings.SettingsViewModel
import com.shopify.checkoutsheetkit.ColorScheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CheckoutKitApp() {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val cartViewModel = koinViewModel<CartViewModel>()
    val logsViewModel = koinViewModel<LogsViewModel>()

    CheckoutKitAppRoot(settingsViewModel, cartViewModel, logsViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutKitAppRoot(
    settingsViewModel: SettingsViewModel,
    cartViewModel: CartViewModel,
    logsViewModel: LogsViewModel,
) {
    val useDarkTheme = settingsViewModel.uiState.collectAsState().value
        .isDarkTheme(isSystemInDarkTheme())

    val cartState = cartViewModel.cartState.collectAsState()
    val totalQuantity = cartState.value.totalQuantity
    val resources = LocalResources.current
    CheckoutKitSampleTheme(darkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val navController = rememberNavController()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Product) }
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            ObserveAsEvents(flow = SnackbarController.events) { event ->
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = resources.getText(event.resourceId).toString())
                }
            }

            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    backStackEntry.destination.route?.let {
                        currentScreen = Screen.fromRoute(it)
                    }
                }
            }

            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
                topBar = {
                    CenterAlignedTopAppBar(
                        modifier = Modifier,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        title = {
                            Image(
                                modifier = Modifier.height(38.dp),
                                contentScale = ContentScale.FillHeight,
                                painter = painterResource(id = R.drawable.logo_vector),
                                contentDescription = stringResource(id = R.string.logo_content_description)
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate(Screen.Cart.route)
                            }) {
                                BadgedBox(badge = {
                                    if (totalQuantity > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.offset(
                                                x = -(7.5.dp), y = 20.dp
                                            )
                                        ) {
                                            Text("$totalQuantity")
                                        }
                                    }
                                }) {
                                    Icon(
                                        modifier = Modifier.height(48.dp),
                                        painter = painterResource(id = R.drawable.cart),
                                        contentDescription = stringResource(id = R.string.cart_icon_content_description),
                                    )
                                }
                            }
                        },
                    )
                },
                bottomBar = {
                    BottomAppBarWithNavigation(
                        navController,
                        currentScreen,
                    )
                }
            ) {
                Column(Modifier.padding(paddingValues = it)) {
                    CheckoutKitNavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        cartViewModel = cartViewModel,
                        settingsViewModel = settingsViewModel,
                        logsViewModel = logsViewModel,
                    )
                }
            }

            NativeSheetsOrchestrator()
        }
    }
}

private fun SettingsUiState.isDarkTheme(isSystemInDarkTheme: Boolean) = when (this) {
    is SettingsUiState.Loading -> isSystemInDarkTheme
    is SettingsUiState.Loaded -> {
        when (settings.colorScheme) {
            is ColorScheme.Dark -> true
            is ColorScheme.Light, is ColorScheme.Web -> false
            is ColorScheme.Automatic -> isSystemInDarkTheme
        }
    }
}

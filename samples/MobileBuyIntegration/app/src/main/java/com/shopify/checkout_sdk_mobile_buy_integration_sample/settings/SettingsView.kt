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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.AppBarState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen

@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController,
    setAppBarState: (AppBarState) -> Unit,
) {

    val uiState = settingsViewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = true) {
        setAppBarState(
            AppBarState(
                title = "Settings",
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Logs.route) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.List,
                            contentDescription = "View Logs",
                        )
                    }
                }
            )
        )
    }

    when (uiState) {
        is SettingsUiState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        is SettingsUiState.Populated -> {
            Column {
                PreloadingSwitch(
                    checked = uiState.settings.preloading.enabled,
                    onCheckedChange = settingsViewModel::setPreloadingEnabled,
                    modifier = Modifier
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                )

                BuyerIdentityDemoSwitch(
                    checked = uiState.settings.buyerIdentityDemoEnabled,
                    onCheckedChange = settingsViewModel::setBuyerIdentityDemoEnabled,
                    modifier = Modifier
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(
                    Modifier.height(20.dp)
                )

                ColorSchemeSection(
                    selected = uiState.settings.colorScheme,
                    setSelected = settingsViewModel::setColorScheme
                )

                Spacer(
                    Modifier.height(20.dp)
                )

                Version(
                    title = "SDK Version",
                    version = uiState.sdkVersion,
                    modifier = Modifier
                        .background(color = MaterialTheme.colors.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth()
                )

                Version(
                    title = "Sample App Version",
                    version = uiState.sampleAppVersion,
                    modifier = Modifier
                        .background(color = MaterialTheme.colors.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.AppBarState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.horizontalPadding
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.verticalPadding

@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController,
    setAppBarState: (AppBarState) -> Unit,
) {

    when (val uiState = settingsViewModel.uiState.collectAsState().value) {
        is SettingsUiState.Loading -> {
            ProgressIndicator()
        }

        is SettingsUiState.Loaded -> {
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Header2(text = stringResource(id = R.string.settings))

                Column {
                    PreloadingSwitch(
                        checked = uiState.settings.preloading.enabled,
                        onCheckedChange = settingsViewModel::setPreloadingEnabled,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    )

                    BuyerIdentityDemoSwitch(
                        checked = uiState.settings.buyerIdentityDemoEnabled,
                        onCheckedChange = settingsViewModel::setBuyerIdentityDemoEnabled,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    )
                }

                ColorSchemeSection(
                    selected = uiState.settings.colorScheme,
                    setSelected = settingsViewModel::setColorScheme
                )

                Version(
                    title = stringResource(id = R.string.sdk_version),
                    version = uiState.sdkVersion,
                    modifier = Modifier.fillMaxWidth()
                )

                Version(
                    title = stringResource(id = R.string.sample_app_version),
                    version = uiState.sampleAppVersion,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { navController.navigate(Screen.Logs.route) },
                    shape = RectangleShape,
                ) {
                    BodyMedium(
                        text = stringResource(id = R.string.view_logs),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

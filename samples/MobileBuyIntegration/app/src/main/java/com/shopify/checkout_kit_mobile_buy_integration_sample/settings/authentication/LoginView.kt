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
package com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_kit_mobile_buy_integration_sample.R
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginView(
    navController: NavController,
    loginViewModel: LoginViewModel = koinViewModel(),
) {

    val uiState = loginViewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = true) {
        // Check if the buyer is already logged in
        loginViewModel.checkLoginState(Locale.current)
    }

    Column {
        when (uiState.status) {
            is Status.Loading -> {
                ProgressIndicator()
            }

            is Status.LoggedOut -> {
                // Show the login WebView if not yet logged in
                LoginWebView(
                    url = uiState.status.loginUrl,
                    customerAccountApiRedirectUri = uiState.status.redirectUri,
                    onCodeParamIntercepted = { code: String ->
                        loginViewModel.codeParamIntercepted(code)
                    }
                )
            }

            is Status.LoggedIn -> {
                // Navigate back to settings when login is complete
                navController.navigate(Screen.Settings.route)
            }

            is Status.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                ) {
                    // A retry mechanism should be added for this case
                    BodyMedium(text = stringResource(id = R.string.login_error))
                }
            }
        }
    }
}

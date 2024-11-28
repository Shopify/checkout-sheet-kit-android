package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun AuthenticationView(
    action: Screen.Authentication.Action,
    navController: NavController,
    loginViewModel: LoginViewModel = koinViewModel(),
) {

    val uiState = loginViewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = true) {
        if (action == Screen.Authentication.Action.LOGIN) {
            loginViewModel.checkLoginState(Locale.current)
        } else if (action == Screen.Authentication.Action.LOGOUT) {
            loginViewModel.logout()
        }
    }

    Column {
        when (uiState.status) {
            is Status.Loading -> {
                ProgressIndicator()
            }

            is Status.LoggedOut -> {
                AuthenticationWebView(
                    url = uiState.status.loginUrl,
                    onCodeParamIntercepted = { code: String ->
                        loginViewModel.codeParamIntercepted(code)
                    }
                )
            }

            is Status.LoggedIn -> {
                navController.navigate(Screen.Settings.route)
            }

            is Status.LoggingOut -> {
                ProgressIndicator()
                BodyMedium(text = "Logging out...")
                AuthenticationWebView(
                    url = uiState.status.logoutUrl,
                    modifier = Modifier.alpha(0.0f),
                    onPageComplete = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            is Status.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                ) {
                    BodyMedium(text = stringResource(id = R.string.login_error))
                    Button(onClick = {
                        // TODO
                        Timber.i("Retry clicked")
                    }) {
                        BodyMedium(
                            text = stringResource(id = R.string.login_retry),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.utils.CustomerAuthenticationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val customerAuthenticationHelper: CustomerAuthenticationHelper,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUIState(
            status = Status.Loading,
            codeVerifier = customerAuthenticationHelper.createCodeVerifier()
        )
    )
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    /**
     * Updates state (e.g. from Loading) to LoggedOut if the customer has not yet authenticated
     * or LoggedIn if already authenticated
     */
    fun checkLoginState(locale: Locale) = viewModelScope.launch {
        Timber.i("Checking logged in state")
        val token = customerRepository.getCustomerAccessToken()
        if (token == null) {
            Timber.i("Not yet logged in")
            val codeVerifier = customerAuthenticationHelper.createCodeVerifier()
            _uiState.value = _uiState.value.copy(
                status = Status.LoggedOut(
                    redirectUri = customerAuthenticationHelper.redirectUri,
                    loginUrl = customerAuthenticationHelper.buildAuthorizationURL(
                        codeVerifier = codeVerifier,
                        locale = locale
                    )
                ),
                codeVerifier = codeVerifier,
            )
        } else {
            Timber.i("Logged in")
            _uiState.value = _uiState.value.copy(
                status = Status.LoggedIn,
            )
        }
    }

    /**
     * When the customer completes login, an authorization code param is intercepted on the redirect
     * and must be exchanged for an access token along with the code verifier
     */
    fun codeParamIntercepted(code: String) = viewModelScope.launch {
        Timber.i("Code intercepted")
        val customerAccessTokens = customerRepository.createCustomerAccessToken(code, _uiState.value.codeVerifier)
        if (customerAccessTokens != null) {
            _uiState.value = _uiState.value.copy(status = Status.LoggedIn)
        } else {
            _uiState.value = _uiState.value.copy(status = Status.Error("Failed to create token"))
        }
    }
}

data class LoginUIState(
    val status: Status = Status.Loading,
    val codeVerifier: String = "",
)

sealed class Status {
    data object Loading : Status()
    data object LoggedIn : Status()
    data class LoggedOut(
        val loginUrl: String,
        val redirectUri: String,
    ) : Status()

    data class Error(val message: String) : Status()
}

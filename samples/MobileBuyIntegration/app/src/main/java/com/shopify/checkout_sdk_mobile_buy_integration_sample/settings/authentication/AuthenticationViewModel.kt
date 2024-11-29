package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerAccessTokenRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.utils.AuthenticationHelpers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val customerAccessTokenRepository: CustomerAccessTokenRepository,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUIState(status = Status.Loading))
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    /**
     * Updates state (e.g. from Loading) to LoggedOut if the customer has not yet authenticated
     * or LoggedIn if already authenticated
     */
    fun checkLoginState(locale: Locale) = viewModelScope.launch {
        Timber.i("Checking logged in state")
        val tokens = customerAccessTokenRepository.getTokens()
        if (tokens == null) {
            Timber.i("Not yet logged in")
            val codeVerifier = AuthenticationHelpers.createCodeVerifier()
            _uiState.value = _uiState.value.copy(
                status = Status.LoggedOut(
                    loginUrl = AuthenticationHelpers.buildLoginPageUrl(
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
        val customerAccessTokens = customerAccessTokenRepository.createTokens(code, _uiState.value.codeVerifier)
        if (customerAccessTokens != null) {
            _uiState.value = _uiState.value.copy(status = Status.LoggedIn)
        } else {
            _uiState.value = _uiState.value.copy(status = Status.Error("Failed to create tokens"))
        }
    }

    /**
     * To log out, locally stored tokens should be removed, and the login WebView should open the logout URL,
     * which will remove any existing auth related cookies from the CookieManager.
     *
     * Existing carts should also be updated at this point to reset buyer identity
     */
    fun logout() = viewModelScope.launch {
        Timber.i("Log out clicked")
        val customerAccessTokens = customerAccessTokenRepository.getTokens()
        if (customerAccessTokens != null) {
            customerAccessTokenRepository.deleteToken()
            _uiState.value = _uiState.value.copy(
                status = Status.LoggingOut(
                    AuthenticationHelpers.buildLogoutPageUrl(
                        customerAccessTokens.customerApiToken.idToken
                    )
                )
            )
        } else {
            _uiState.value = _uiState.value.copy(
                status = Status.Error("Invalid state - logout clicked, but not tokens are stored")
            )
        }
    }
}

data class LoginUIState(
    val status: Status = Status.Loading,
    val codeVerifier: String = AuthenticationHelpers.createCodeVerifier(),
)

sealed class Status {
    data object Loading : Status()
    data object LoggedIn : Status()
    data class LoggingOut(val logoutUrl: String) : Status()
    data class LoggedOut(val loginUrl: String) : Status()
    data class Error(val message: String) : Status()
}

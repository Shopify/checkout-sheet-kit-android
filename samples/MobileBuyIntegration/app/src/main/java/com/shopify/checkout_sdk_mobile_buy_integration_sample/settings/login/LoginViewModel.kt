package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.data.TokenRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.utils.AuthenticationHelpers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val tokenRepository: TokenRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LoginUIState(
            status = Status.Loading
        )
    )
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    fun checkLoginState(locale: Locale) = viewModelScope.launch {
        Timber.i("Checking logged in state")
        val tokens = tokenRepository.getTokens()
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
                email = tokenRepository.decodeIdToken(tokens.customerApiToken).email,
            )
        }
    }

    fun codeParamIntercepted(code: String) {
        Timber.i("Code intercepted")
        exchangeCodeForTokens(code, _uiState.value.codeVerifier)
    }

    fun logout() = viewModelScope.launch {
        Timber.i("Log out clicked")
        val tokens = tokenRepository.getTokens()
        if (tokens != null) {
            val idToken = tokens.customerApiToken.idToken
            val logoutUrl = AuthenticationHelpers.buildLogoutPageUrl(idToken)
            _uiState.value = _uiState.value.copy(
                status = Status.LoggingOut(logoutUrl)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                status = Status.Error("Invalid state - logout clicked, but not tokens are stored")
            )
        }
    }

    fun loggedOut(locale: Locale) = viewModelScope.launch {
        tokenRepository.deleteToken()
        val newCodeVerifier = AuthenticationHelpers.createCodeVerifier()
        _uiState.value = _uiState.value.copy(
            status = Status.LoggedOut(
                loginUrl = AuthenticationHelpers.buildLoginPageUrl(
                    codeVerifier = newCodeVerifier,
                    locale = locale
                )
            ),
            email = "",
            codeVerifier = newCodeVerifier,
        )
    }

    private fun exchangeCodeForTokens(code: String, codeVerifier: String) = viewModelScope.launch {
        val tokens = tokenRepository.createTokens(code, codeVerifier)
        if (tokens != null) {
            _uiState.value = _uiState.value.copy(
                email = tokenRepository.decodeIdToken(tokens.customerApiToken).email,
                status = Status.LoggedIn,
            )
        } else {
            _uiState.value = _uiState.value.copy(
                status = Status.Error("Failed to create tokens"),
            )
        }
    }
}

data class LoginUIState(
    val status: Status = Status.Loading,
    val codeVerifier: String = AuthenticationHelpers.createCodeVerifier(),
    val email: String = "",
)

sealed class Status {
    data object Loading : Status()
    data object LoggedIn : Status()
    data class LoggingOut(val logoutUrl: String) : Status()
    data class LoggedOut(val loginUrl: String) : Status()
    data class Error(val message: String) : Status()
}

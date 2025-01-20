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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.data.Settings
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.data.SettingsRepository
import com.shopify.checkoutsheetkit.ColorScheme
import com.shopify.checkoutsheetkit.Preloading
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                val token = customerRepository.getCustomerAccessToken()
                _uiState.value = SettingsUiState.Loaded(
                    settings = settings,
                    sdkVersion = ShopifyCheckoutSheetKit.version,
                    sampleAppVersion = BuildConfig.VERSION_NAME,
                    isAuthenticated = token != null
                )
            }
        }
    }

    fun setColorScheme(colorScheme: ColorScheme) = viewModelScope.launch {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = colorScheme
        }
        settingsRepository.setColorScheme(colorScheme)
    }

    fun setPreloadingEnabled(enabled: Boolean) = viewModelScope.launch {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = enabled)
        }
        settingsRepository.setPreloadingEnabled(enabled)
    }

    fun setBuyerIdentityDemoEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setBuyerIdentityDemoEnabled(enabled)
    }

    fun logout() = viewModelScope.launch {
        val state = _uiState.value
        if (state is SettingsUiState.Loaded) {
            customerRepository.logout()
            _uiState.value = state.copy(isAuthenticated = false)
        }
    }
}

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Loaded(
        val settings: Settings,
        val sdkVersion: String,
        val sampleAppVersion: String,
        val isAuthenticated: Boolean,
    ) : SettingsUiState()
}

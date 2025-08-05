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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkoutsheetkit.ColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val preferencesManager: PreferencesManager
) {

    /**
     * Observe changes to settings
     */
    fun observeSettings(): Flow<Settings> {
        return preferencesManager.userPreferencesFlow.map { preferences ->
            Settings(
                colorScheme = preferences.colorScheme,
                preloading = preferences.preloading,
                buyerIdentityDemoEnabled = preferences.buyerIdentityDemoEnabled,
                privacyConsent = preferences.privacyConsent,
            )
        }
    }

    /**
     * Update the [colorScheme](https://github.com/Shopify/checkout-sheet-kit-android/?tab=readme-ov-file#color-scheme) setting
     */
    suspend fun setColorScheme(colorScheme: ColorScheme) {
        preferencesManager.setColorScheme(colorScheme)
    }

    /**
     * Update the [preloading](https://github.com/Shopify/checkout-sheet-kit-android/?tab=readme-ov-file#preloading) setting
     */
    suspend fun setPreloadingEnabled(enabled: Boolean) {
        preferencesManager.setPreloadingEnabled(enabled)
    }

    /**
     * Update the buyerIdentity setting, which sets some pre-known customer details
     * in Cart buyerIdentityInput, prefilling checkout
     */
    suspend fun setBuyerIdentityDemoEnabled(enabled: Boolean) {
        preferencesManager.setBuyerIdentityDemoEnabled(enabled)
    }

    /**
     * Update privacy consent preferences
     */
    suspend fun setPrivacyConsentMarketing(enabled: Boolean) {
        preferencesManager.setPrivacyConsentMarketing(enabled)
    }

    suspend fun setPrivacyConsentAnalytics(enabled: Boolean) {
        preferencesManager.setPrivacyConsentAnalytics(enabled)
    }

    suspend fun setPrivacyConsentPreferences(enabled: Boolean) {
        preferencesManager.setPrivacyConsentPreferences(enabled)
    }

    suspend fun setPrivacyConsentSaleOfData(enabled: Boolean) {
        preferencesManager.setPrivacyConsentSaleOfData(enabled)
    }
}

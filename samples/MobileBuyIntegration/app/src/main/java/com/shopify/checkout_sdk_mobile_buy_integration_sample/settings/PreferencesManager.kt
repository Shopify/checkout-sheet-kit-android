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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shopify.checkoutsheetkit.ColorScheme
import com.shopify.checkoutsheetkit.Preloading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checkoutSheetKitSettingsV2")

class PreferencesManager(private val context: Context) {
    private val decoder: Json = Json { ignoreUnknownKeys = true }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val colorScheme = decoder.decodeFromString<ColorScheme>(
            preferences[COLOR_SCHEME] ?: DEFAULT_COLOR_SCHEME
        )
        val preloading = preferences[PRELOADING] ?: true
        val buyerIdentityDemoEnabled = preferences[BUYER_IDENTITY] ?: false

        UserPreferences(
            colorScheme = colorScheme,
            preloading = Preloading(preloading),
            buyerIdentityDemoEnabled = buyerIdentityDemoEnabled
        )
    }

    suspend fun setColorScheme(colorScheme: ColorScheme) =
        saveData(COLOR_SCHEME, Json.encodeToString(ColorScheme.serializer(), colorScheme))

    suspend fun setPreloadingEnabled(enabled: Boolean) = saveData(PRELOADING, enabled)
    suspend fun setBuyerIdentityDemoEnabled(enabled: Boolean) = saveData(BUYER_IDENTITY, enabled)

    private suspend fun <T> saveData(key: Preferences.Key<T>, value: T) = context.dataStore.edit {
        it[key] = value
    }

    companion object {
        private val COLOR_SCHEME = stringPreferencesKey("colorScheme")
        private val PRELOADING = booleanPreferencesKey("preloading")
        private val BUYER_IDENTITY = booleanPreferencesKey("buyerIdentity")

        private val DEFAULT_COLOR_SCHEME = Json.encodeToString(
            ColorScheme.serializer(),
            ColorScheme.Automatic()
        )
    }
}

data class UserPreferences(
    val colorScheme: ColorScheme,
    val preloading: Preloading,
    val buyerIdentityDemoEnabled: Boolean,
)

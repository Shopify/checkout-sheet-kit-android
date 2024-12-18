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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Token Store backed by EncryptedSharedPreferences to allow storing an reusing tokens
 * until they have expired.
 */
class CustomerAccessTokenStore(
    scope: CoroutineScope,
    private val appContext: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val storage = scope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
        createEncryptedPrefs(appContext)
    }

    /**
     * Find currently stored token
     */
    suspend fun find(): AccessToken? = getStorage()
        .getString(KEY, null)?.let { tokenString ->
            json.decodeFromString<AccessToken>(tokenString)
        }

    /**
     * Save token to the store
     */
    suspend fun save(accessToken: AccessToken) = getStorage()
        .edit { putString(KEY, json.encodeToString(accessToken)) }

    /**
     * Delete a token from the store
     */
    suspend fun delete() = getStorage().edit { remove(KEY) }

    private suspend fun getStorage() = storage.await()

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val PREFS_NAME = "customer_access_tokens"
        private const val KEY = "token"
    }
}

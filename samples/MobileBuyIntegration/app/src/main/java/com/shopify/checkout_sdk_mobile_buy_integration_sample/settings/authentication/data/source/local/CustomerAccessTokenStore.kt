package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerAccessTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Token Store backed by EncryptedSharedPreferences
 */
class CustomerAccessTokenStore(
    scope: CoroutineScope,
    private val appContext: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    private val storage = scope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
        createEncryptedPrefs(appContext)
    }

    suspend fun getTokens(): CustomerAccessTokens? = getStorage()
        .getString(KEY, null)?.let { tokenString ->
            json.decodeFromString<CustomerAccessTokens>(tokenString)
        }

    suspend fun storeTokens(customerAccessTokens: CustomerAccessTokens) = getStorage()
        .edit { putString(KEY, json.encodeToString(customerAccessTokens)) }

    suspend fun clearTokens() = getStorage().edit { remove(KEY) }

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
        private const val PREFS_NAME = "customer_auth_tokens"
        private const val KEY = "tokens"
    }
}

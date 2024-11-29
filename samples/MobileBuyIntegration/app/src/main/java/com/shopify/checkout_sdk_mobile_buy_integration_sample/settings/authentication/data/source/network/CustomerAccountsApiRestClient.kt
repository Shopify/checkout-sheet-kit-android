package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException

/**
 * Client for interacting with Customer Account API
 */
class CustomerAccountsApiRestClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val restBaseUrl: String,
    private val clientId: String,
    private val redirectUri: String,
) {

    suspend fun obtainAccessToken(code: String, codeVerifier: String): OAuthTokenResult {
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .build()

        val request = Request.Builder()
            .url("$restBaseUrl/oauth/token")
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        return executeOAuthTokenRequest(request)
    }

    suspend fun refreshAccessToken(accessToken: AccessToken): OAuthTokenResult {
        val requestBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", clientId)
            .add("refresh_token", accessToken.refreshToken)
            .build()

        val request = Request.Builder()
            .url("$restBaseUrl/oauth/token")
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
        return executeOAuthTokenRequest(request)
    }

    private suspend fun executeOAuthTokenRequest(request: Request): OAuthTokenResult {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val token = json.decodeFromString<AccessToken>(response.bodyOrThrow())
                        OAuthTokenResult.Success(token)
                    } else {
                        val responseBody = response.bodyOrThrow()
                        OAuthTokenResult.Error(responseBody)
                    }
                }
            } catch (e: IOException) {
                Timber.e("Failed to obtain token $e")
                OAuthTokenResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class OAuthTokenResult {
    data class Success(
        val token: AccessToken,
    ) : OAuthTokenResult()

    data class Error(
        val message: String
    ) : OAuthTokenResult()
}

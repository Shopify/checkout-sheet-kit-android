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
package com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication.data.source.network

import com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication.utils.CustomerAuthenticationHelper
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
 * for [Authentication](https://shopify.dev/docs/api/customer#authentication)
 */
class CustomerAccountsApiRestClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val helper: CustomerAuthenticationHelper,
    private val clientId: String,
    private val redirectUri: String,
) {

    /**
     * Executes an [access token request](https://shopify.dev/docs/api/customer#step-obtain-access-token)
     */
    suspend fun fetchAccessToken(code: String, codeVerifier: String): OAuthTokenResult {
        Timber.i("Exchanging code for access token")
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .build()

        val request = Request.Builder()
            .url(helper.buildTokenURL())
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        return executeOAuthTokenRequest(request)
    }

    /**
     * Executes a [refresh token request](https://shopify.dev/docs/api/customer#step-using-refresh-token)
     */
    suspend fun refreshAccessToken(accessToken: AccessToken): OAuthTokenResult {
        Timber.i("Refreshing access token")
        val requestBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", clientId)
            .add("refresh_token", accessToken.refreshToken)
            .build()

        val request = Request.Builder()
            .url(helper.buildTokenURL())
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
        return executeOAuthTokenRequest(request)
    }

    /**
     * Executes a [logout request](https://shopify.dev/docs/api/customer#step-logging-out)
     */
    suspend fun logout(idToken: String) {
        Timber.i("Logging out")
        val request = Request.Builder()
            .url(helper.buildLogoutURL(idToken))
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    Timber.i("Logout request successful? ${response.isSuccessful}")
                }
            } catch (e: IOException) {
                Timber.e("Logout request failed $e")
            }
        }
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

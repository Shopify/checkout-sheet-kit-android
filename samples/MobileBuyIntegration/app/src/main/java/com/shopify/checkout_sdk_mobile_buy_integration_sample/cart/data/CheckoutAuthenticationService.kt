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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

/**
 * Service for fetching checkout authentication tokens using OAuth client credentials flow.
 */
class CheckoutAuthenticationService(
    private val client: OkHttpClient,
    private val json: Json,
    private val authEndpoint: String,
    private val clientId: String,
    private val clientSecret: String,
) {
    /**
     * Checks if the service has all required configuration to fetch tokens.
     */
    fun hasConfiguration(): Boolean {
        return authEndpoint.isNotBlank() && clientId.isNotBlank() && clientSecret.isNotBlank()
    }

    /**
     * Fetches an access token using OAuth client credentials flow.
     *
     * @return The JWT access token
     * @throws Exception if the request fails or configuration is missing
     */
    suspend fun fetchAccessToken(): String = withContext(Dispatchers.IO) {
        if (!hasConfiguration()) {
            throw IllegalStateException("Checkout authentication is not configured")
        }

        val requestBody = json.encodeToString(
            TokenRequest.serializer(),
            TokenRequest(
                clientId = clientId,
                clientSecret = clientSecret,
                grantType = "client_credentials"
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(authEndpoint)
            .post(requestBody)
            .build()

        Timber.d("Fetching checkout authentication token from $authEndpoint")

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            Timber.e("Failed to fetch token: ${response.code} - $errorBody")
            throw Exception("Failed to fetch authentication token: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response body from authentication endpoint")

        val tokenResponse = json.decodeFromString(TokenResponse.serializer(), responseBody)

        Timber.d("Successfully fetched checkout authentication token")
        return@withContext tokenResponse.accessToken
    }

    @Serializable
    private data class TokenRequest(
        @SerialName("client_id")
        val clientId: String,
        @SerialName("client_secret")
        val clientSecret: String,
        @SerialName("grant_type")
        val grantType: String,
    )

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String,
    )
}
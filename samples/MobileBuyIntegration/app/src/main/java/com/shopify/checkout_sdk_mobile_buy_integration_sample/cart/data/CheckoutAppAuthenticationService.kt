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

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Exception thrown when app authentication token fetching fails.
 */
class CheckoutAppAuthenticationException(
    message: String,
    val statusCode: Int? = null,
    val errorBody: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Service for fetching checkout app authentication tokens using OAuth client credentials flow.
 * This authenticates the calling application to enable app-specific checkout customizations.
 *
 * Tokens are cached and reused until they expire (with a 5-minute safety buffer).
 */
class CheckoutAppAuthenticationService(
    private val client: OkHttpClient,
    private val json: Json,
    private val authEndpoint: String,
    private val clientId: String,
    private val clientSecret: String,
) {
    private var cachedToken: String? = null
    private var tokenExpiryTimestamp: Long = 0
    private val mutex = Mutex()

    /**
     * Checks if the service has all required configuration to fetch tokens.
     */
    fun hasConfiguration(): Boolean {
        return authEndpoint.isNotBlank() && clientId.isNotBlank() && clientSecret.isNotBlank()
    }

    /**
     * Fetches an access token using OAuth client credentials flow.
     * Returns a cached token if available and not expired (with 5-minute buffer).
     *
     * @return The JWT access token
     * @throws CheckoutAppAuthenticationException if the request fails or configuration is missing
     */
    suspend fun fetchAccessToken(): String = mutex.withLock {
        val now = Instant.now().epochSecond
        val expiryBuffer = 5.minutes.inWholeSeconds

        cachedToken?.let { token ->
            if (tokenExpiryTimestamp > now + expiryBuffer) {
                Timber.d("Using cached checkout app authentication token (expires in ${tokenExpiryTimestamp - now}s)")
                return@withLock token
            } else {
                Timber.d("Cached token expired or about to expire, fetching new token")
            }
        }

        // Fetch new token
        val token = fetchNewToken()

        // Extract expiry from JWT and cache
        try {
            tokenExpiryTimestamp = extractExpiryFromJwt(token)
            cachedToken = token
            Timber.d("Cached new token (expires in ${tokenExpiryTimestamp - now}s)")
        } catch (e: Exception) {
            Timber.w("Failed to parse token expiry, will not cache: $e")
        }

        return@withLock token
    }

    /**
     * Extracts the expiry timestamp (exp claim) from a JWT token.
     */
    private fun extractExpiryFromJwt(jwt: String): Long {
        val parts = jwt.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
        val jsonElement = Json.parseToJsonElement(payload)
        return jsonElement.jsonObject["exp"]?.jsonPrimitive?.long
            ?: throw IllegalArgumentException("JWT missing exp claim")
    }

    /**
     * Fetches a new token from the authentication endpoint.
     */
    private suspend fun fetchNewToken(): String = withContext(Dispatchers.IO) {
        if (!hasConfiguration()) {
            throw CheckoutAppAuthenticationException("Checkout app authentication is not configured")
        }

        val requestBody = json.encodeToString(
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

        Timber.d("Fetching checkout app authentication token from $authEndpoint")

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            Timber.e("Failed to fetch app authentication token: ${response.code} - $errorBody")
            throw CheckoutAppAuthenticationException(
                message = "Failed to fetch app authentication token",
                statusCode = response.code,
                errorBody = errorBody
            )
        }

        val responseBody = response.body?.string()
            ?: throw CheckoutAppAuthenticationException("Empty response body from authentication endpoint")

        val tokenResponse = json.decodeFromString<TokenResponse>(responseBody)

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

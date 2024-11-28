package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException

class CustomerAccountsApiClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val graphQLBaseUrl: String,
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
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        OAuthTokenResult.Error("Unexpected empty response body")
                    } else {
                        if (!response.isSuccessful) {
                            OAuthTokenResult.Error(responseBody)
                        }
                        val token = json.decodeFromString<AccessToken>(responseBody)
                        OAuthTokenResult.Success(token)
                    }
                }
            } catch (e: IOException) {
                Timber.e("Failed to obtain token $e")
                OAuthTokenResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun exchangeForStorefrontApiToken(accessToken: AccessToken): StorefrontExchangeResult {
        val mutation = """
            mutation {
                storefrontCustomerAccessTokenCreate {
                    customerAccessToken
                    userErrors {
                        field
                        message
                    }
                }
            }
        """

        val jsonBody = JSONObject().apply {
            put("query", mutation)
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(graphQLBaseUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", accessToken.accessToken)
            .build()

        return executeStorefrontExchangeRequest(request)
    }

    private suspend fun executeStorefrontExchangeRequest(request: Request): StorefrontExchangeResult {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        StorefrontExchangeResult.NetworkError("Unexpected empty response body")
                    } else {
                        if (!response.isSuccessful) {
                            val errorResponse = json.decodeFromString<ErrorResponse>(responseBody)
                            if (errorResponse.errors.any { it.message.contains("") }) {
                                StorefrontExchangeResult.InvalidToken
                            }
                            StorefrontExchangeResult.NetworkError("Unknown error")
                        }
                        val tokenResponse = json.decodeFromString<StorefrontCustomerAccessTokenResponse>(responseBody)
                        StorefrontExchangeResult.Success(tokenResponse.data.storefrontCustomerAccessTokenCreate.customerAccessToken)
                    }
                }
            } catch (e: IOException) {
                StorefrontExchangeResult.NetworkError(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class StorefrontExchangeResult {
    data class Success(
        val token: String,
    ) : StorefrontExchangeResult()

    data class NetworkError(val message: String) : StorefrontExchangeResult()
    data object InvalidToken : StorefrontExchangeResult()
}

sealed class OAuthTokenResult {
    data class Success(
        val token: AccessToken,
    ) : OAuthTokenResult()

    data class Error(
        val message: String
    ) : OAuthTokenResult()
}

data class ErrorResponse(
    val errors: List<Error>
)

data class Error(
    val message: String,
)

@Serializable
data class StorefrontCustomerAccessTokenResponse(
    val data: StorefrontCustomerAccessTokenCreate,
)

@Serializable
data class StorefrontCustomerAccessTokenCreate(
    val storefrontCustomerAccessTokenCreate: CustomerAccessToken,
)

@Serializable
data class CustomerAccessToken(
    val customerAccessToken: String,
)

package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * GraphQL client for interacting with Customer Account API
 */
class CustomerAccountsApiGraphQLClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val graphQLBaseUrl: String,
) {

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
                    if (response.isSuccessful) {
                        val tokenResponse = json.decodeFromString<StorefrontCustomerAccessTokenResponse>(
                            response.bodyOrThrow()
                        )
                        StorefrontExchangeResult.Success(tokenResponse.data.storefrontCustomerAccessTokenCreate.customerAccessToken)
                    } else {
                        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyOrThrow())
                        if (errorResponse.errors.any { it.message.contains("invalid token", ignoreCase = true) }) {
                            StorefrontExchangeResult.InvalidToken
                        } else {
                            StorefrontExchangeResult.NetworkError(errorResponse.errors.joinToString())
                        }
                    }
                }
            } catch (e: IOException) {
                StorefrontExchangeResult.NetworkError(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class StorefrontExchangeResult {
    data class Success(val token: String) : StorefrontExchangeResult()
    data class NetworkError(val message: String) : StorefrontExchangeResult()
    data object InvalidToken : StorefrontExchangeResult()
}

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

@Serializable
data class ErrorResponse(
    val errors: List<Error>
)

@Serializable
data class Error(
    val message: String,
)

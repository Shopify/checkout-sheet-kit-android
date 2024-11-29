package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.AccessToken
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * GraphQL client for interacting with Customer Account API
 */
class CustomerAccountsApiGraphQLClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: String,
) {

    suspend fun exchangeForStorefrontApiToken(accessToken: AccessToken): StorefrontAccessTokenResponse {
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
            .url(baseUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", accessToken.accessToken)
            .build()

        return executeStorefrontExchangeRequest(request)
    }

    suspend fun getCustomer(accessToken: AccessToken): CustomerResponse {
        val query = """
            query {
                customer {
                    id
                    displayName
                    imageUrl
                    defaultAddress {
                        id,
                        address1
                        address2
                        city
                        country
                        province
                        zoneCode
                        zip
                        firstName
                        lastName
                        name
                        phoneNumber
                        formatted
                    }
                    phoneNumber {
                        phoneNumber
                        marketingState
                    }
                    emailAddress {
                        emailAddress
                        marketingState
                    }
                }
            }
        """

        val jsonBody = JSONObject().apply {
            put("operationName", "Customer")
            put("query", query)
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", accessToken.accessToken)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val customerResponse = json.decodeFromString<CustomerResponseObj>(
                            response.bodyOrThrow()
                        )
                        CustomerResponse.Success(customerResponse.data.customer)
                    } else {
                        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyOrThrow())
                        CustomerResponse.Error(errorResponse.errors.joinToString())
                    }
                }
            } catch (e: Exception) {
                CustomerResponse.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun executeStorefrontExchangeRequest(request: Request): StorefrontAccessTokenResponse {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val tokenResponse = json.decodeFromString<StorefrontCustomerAccessTokenResponse>(
                            response.bodyOrThrow()
                        )
                        StorefrontAccessTokenResponse.Success(tokenResponse.data.storefrontCustomerAccessTokenCreate.customerAccessToken)
                    } else {
                        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyOrThrow())
                        if (errorResponse.errors.any { it.message.contains("invalid token", ignoreCase = true) }) {
                            StorefrontAccessTokenResponse.InvalidToken
                        } else {
                            StorefrontAccessTokenResponse.Error(errorResponse.errors.joinToString())
                        }
                    }
                }
            } catch (e: Exception) {
                StorefrontAccessTokenResponse.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class StorefrontAccessTokenResponse {
    data class Success(val token: String) : StorefrontAccessTokenResponse()
    data class Error(val message: String) : StorefrontAccessTokenResponse()
    data object InvalidToken : StorefrontAccessTokenResponse()
}

sealed class CustomerResponse {
    data class Success(val customer: Customer) : CustomerResponse()
    data class Error(val message: String) : CustomerResponse()
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

@Serializable
data class CustomerResponseObj(
    val data: CustomerWrapper,
)

@Serializable
data class CustomerWrapper(
    val customer: Customer
)

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
import com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication.data.Customer
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
 *
 * e.g. to perform [customer](https://shopify.dev/docs/api/customer/latest/queries/customer)
 * or [order](https://shopify.dev/docs/api/customer/latest/queries/order) queries.
 */
class CustomerAccountsApiGraphQLClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
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
                        val customerResponse = json.decodeFromString<CustomerGraphQLResponse>(
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
}

sealed class CustomerResponse {
    data class Success(val customer: Customer) : CustomerResponse()
    data class Error(val message: String) : CustomerResponse()
}

@Serializable
data class ErrorResponse(
    val errors: List<Error>
)

@Serializable
data class Error(
    val message: String,
)

@Serializable
data class CustomerGraphQLResponse(
    val data: CustomerData,
)

@Serializable
data class CustomerData(
    val customer: Customer
)

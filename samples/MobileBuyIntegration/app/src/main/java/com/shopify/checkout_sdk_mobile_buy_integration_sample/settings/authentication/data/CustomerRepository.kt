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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local.CustomerAccessTokenStore
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiGraphQLClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiRestClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerResponse
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.OAuthTokenResult
import timber.log.Timber

/**
 * Repository for customers and customer access tokens
 */
class CustomerRepository(
    private val restClient: CustomerAccountsApiRestClient,
    private val graphQLClient: CustomerAccountsApiGraphQLClient,
    private val localTokenStore: CustomerAccessTokenStore,
) {

    /**
     * Returns a customer using a Customer Accounts API access token
     *
     * Returns null if user not logged in, and token is not stored
     * First refreshes the token if it has expired
     */
    suspend fun getCustomer(): Customer? {
        val token = getCustomerAccessToken() ?: return null
        when (val result = graphQLClient.getCustomer(token)) {
            is CustomerResponse.Success -> {
                Timber.i("Fetched customer ${result.customer}")
                return result.customer
            }

            is CustomerResponse.Error -> {
                Timber.e("Error when fetching customer, ${result.message}")
                return null
            }
        }
    }

    /**
     * Gets a stored access token, refreshes if expired.
     *
     * returns null if no token stored or if refresh fails, otherwise an
     * unexpired access token
     */
    suspend fun getCustomerAccessToken(): AccessToken? {
        val localToken = localTokenStore.find() ?: return null

        if (!localToken.hasExpired()) {
            return localToken
        }

        return restClient.refreshAccessToken(localToken).toToken()
    }

    /**
     * Creates a new access token and stores it locally
     */
    suspend fun createCustomerAccessToken(code: String, codeVerifier: String): AccessToken? {
        val customerAccessToken = localTokenStore.find()
        if (customerAccessToken != null) {
            Timber.i("Locally stored customer access token found")
            if (!customerAccessToken.hasExpired()) {
                Timber.i("Returning locally stored customer access token")
                return customerAccessToken
            } else {
                Timber.i("Locally stored customer access token expired, refreshing")
                return restClient.refreshAccessToken(customerAccessToken).toToken()
            }
        }

        Timber.i("No locally stored token found, fetching remote token")
        return restClient.fetchAccessToken(code, codeVerifier).toToken()
    }

    suspend fun logout() {
        val idToken = getCustomerAccessToken()?.idToken ?: ""
        Timber.i("Logging out and deleting stored token")
        restClient.logout(idToken)
        localTokenStore.delete()
    }

    private suspend fun OAuthTokenResult.toToken(): AccessToken? {
        return when (this) {
            is OAuthTokenResult.Success -> {
                Timber.i("Customer Account API token retrieved $this")
                localTokenStore.save(this.token)
                this.token
            }

            is OAuthTokenResult.Error -> {
                Timber.i("Failed to fetch Customer Account API token, ${this.message}")
                null
            }
        }
    }
}

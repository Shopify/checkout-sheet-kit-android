package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local.CustomerAccessTokenStore
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiGraphQLClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiRestClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerResponse
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.OAuthTokenResult
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.StorefrontAccessTokenResponse
import timber.log.Timber
import java.time.Duration
import java.time.Instant

/**
 * Repository for customers and customer access tokens
 *
 * Implementation always fetches both a customerAccessToken and storefrontApiToken
 * Both are needed to authenticate checkout
 */
class CustomerRepository(
    private val restClient: CustomerAccountsApiRestClient,
    private val graphQLClient: CustomerAccountsApiGraphQLClient,
    private val localStore: CustomerAccessTokenStore,
) {

    suspend fun getCustomer(): Customer? {
        var tokens = getCustomerAccessTokens() ?: return null
        if (tokens.expired()) {
            tokens = fetchCustomerAccessTokens {
                restClient.refreshAccessToken(tokens.customerApiToken)
            } ?: return null
        }
        when (val result = graphQLClient.getCustomer(tokens.customerApiToken)) {
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

    suspend fun getCustomerAccessTokens(): CustomerAccessTokens? {
        return localStore.getTokens()
    }

    suspend fun createCustomerAccessTokens(code: String, codeVerifier: String): CustomerAccessTokens? {
        val customerAccessTokens = localStore.getTokens()
        if (customerAccessTokens != null) {
            Timber.i("Locally stored customer access tokens found")
            if (!customerAccessTokens.expired()) {
                Timber.i("Returning locally stored customer access tokens")
                return customerAccessTokens
            } else {
                Timber.i("Locally stored customer access tokens expired, refreshing")
                return fetchCustomerAccessTokens { restClient.refreshAccessToken(customerAccessTokens.customerApiToken) }
            }
        }

        Timber.i("No locally stored token found, fetching remote token")
        return fetchCustomerAccessTokens { restClient.fetchAccessToken(code, codeVerifier) }
    }

    suspend fun deleteCustomerAccessTokens() {
        Timber.i("Deleting stored token if present")
        localStore.clearTokens()
    }

    private suspend fun fetchCustomerAccessTokens(operation: suspend () -> OAuthTokenResult): CustomerAccessTokens? {
        return when (val tokenResult = operation.invoke()) {
            is OAuthTokenResult.Success -> {
                Timber.i("Customer Account API token retrieved, fetching storefront API token")
                when (val exchangeResult = graphQLClient.exchangeForStorefrontApiToken(tokenResult.token)) {
                    is StorefrontAccessTokenResponse.Success -> {
                        Timber.i("Storefront API token retrieved, storing tokens")
                        val tokens = combineTokens(tokenResult.token, exchangeResult.token)
                        localStore.storeTokens(tokens)
                        tokens
                    }

                    is StorefrontAccessTokenResponse.InvalidToken -> {
                        Timber.i("Storefront API returned invalid token error, deleting stored token")
                        deleteCustomerAccessTokens()
                        null
                    }

                    is StorefrontAccessTokenResponse.Error -> {
                        Timber.i("Failed to fetch Storefront API token")
                        null
                    }
                }
            }

            is OAuthTokenResult.Error -> {
                Timber.i("Failed to Customer Account API token, ${tokenResult.message}")
                null
            }
        }
    }

    private fun combineTokens(accessToken: AccessToken, storefrontToken: String): CustomerAccessTokens {
        return CustomerAccessTokens(
            customerApiToken = accessToken,
            storefrontApiToken = storefrontToken,
            expiresAt = Instant.now()
                .plus(Duration.ofSeconds(accessToken.expiresIn))
                .toEpochMilli()
        )
    }
}

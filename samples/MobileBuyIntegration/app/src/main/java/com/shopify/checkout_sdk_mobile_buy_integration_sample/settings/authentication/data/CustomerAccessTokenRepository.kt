package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data

import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local.CustomerAccessTokenStore
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiGraphQLClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiRestClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.OAuthTokenResult
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.StorefrontExchangeResult
import timber.log.Timber
import java.time.Duration
import java.time.Instant

/**
 * Repository for customer access tokens
 *
 * Implementation always fetches both a customerAccessToken and storefrontApiToken
 * Both are needed to authenticate checkout
 */
class CustomerAccessTokenRepository(
    private val restClient: CustomerAccountsApiRestClient,
    private val graphQLClient: CustomerAccountsApiGraphQLClient,
    private val localStore: CustomerAccessTokenStore,
) {
    suspend fun getTokens(): CustomerAccessTokens? {
        return localStore.getTokens()
    }

    suspend fun createTokens(code: String, codeVerifier: String): CustomerAccessTokens? {
        val tokens = localStore.getTokens()
        if (tokens != null) {
            Timber.i("Locally stored token found")
            if (!tokens.expired()) {
                Timber.i("Returning locally stored token")
                return tokens
            } else {
                Timber.i("Locally stored token expired, refreshing")
                return fetchTokens { restClient.refreshAccessToken(tokens.customerApiToken) }
            }
        }

        Timber.i("No locally stored token found, fetching remote token")
        return fetchTokens { restClient.obtainAccessToken(code, codeVerifier) }
    }

    suspend fun deleteToken() {
        Timber.i("Deleting stored token if present")
        localStore.clearTokens()
    }

    private suspend fun fetchTokens(operation: suspend () -> OAuthTokenResult): CustomerAccessTokens? {
        return when (val tokenResult = operation.invoke()) {
            is OAuthTokenResult.Success -> {
                Timber.i("Customer Account API token retrieved, fetching storefront API token")
                when (val exchangeResult = graphQLClient.exchangeForStorefrontApiToken(tokenResult.token)) {
                    is StorefrontExchangeResult.Success -> {
                        Timber.i("Storefront API token retrieved, storing tokens")
                        val tokens = tokens(tokenResult.token, exchangeResult.token)
                        localStore.storeTokens(tokens)
                        tokens
                    }

                    is StorefrontExchangeResult.InvalidToken -> {
                        Timber.i("Storefront API returned invalid token error, deleting stored token")
                        deleteToken()
                        null
                    }

                    is StorefrontExchangeResult.NetworkError -> {
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

    private fun tokens(accessToken: AccessToken, storefrontToken: String): CustomerAccessTokens {
        return CustomerAccessTokens(
            customerApiToken = accessToken,
            storefrontApiToken = storefrontToken,
            expiresAt = Instant.now()
                .plus(Duration.ofSeconds(accessToken.expiresIn))
                .toEpochMilli()
        )
    }
}

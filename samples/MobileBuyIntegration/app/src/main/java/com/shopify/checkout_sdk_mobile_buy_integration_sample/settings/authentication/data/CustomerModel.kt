package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Customer API Access Token
 */
@Serializable
data class AccessToken(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("token_type")
    val tokenType: String,

    @SerialName("expires_in")
    val expiresIn: Long,

    @SerialName("id_token")
    val idToken: String? = null,
)

/**
 * Groups Customer API access token and Storefront API access token
 */
@Serializable
data class CustomerAccessTokens(
    val customerApiToken: AccessToken,
    val storefrontApiToken: String,
    val expiresAt: Long,
) {
    fun expired(): Boolean {
        val storedTokenExpiry = Instant.ofEpochMilli(expiresAt)
        return storedTokenExpiry.isBefore(Instant.now())
    }
}

@Serializable
data class Customer(
    val id: String,
    val imageUrl: String,
    val displayName: String,
    val phoneNumber: CustomerPhoneNumber?,
    val emailAddress: CustomerEmailAddress?,
    val defaultAddress: CustomerAddress?,
)

@Serializable
data class CustomerEmailAddress(
    val emailAddress: String,
    val marketingState: String,
)

@Serializable
data class CustomerPhoneNumber(
    val phoneNumber: String,
    val marketingState: String,
)

@Serializable
data class CustomerAddress(
    val id: String,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val country: String?,
    val province: String?,
    val zoneCode: String?,
    val zip: String?,
    val firstName: String?,
    val lastName: String?,
    val name: String?,
    val phoneNumber: String?,
)

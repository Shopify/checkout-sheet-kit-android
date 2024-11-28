package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

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
    val idToken: String,
)

@Serializable
data class IdTokenDetails(
    val subject: String,
    val email: String,
    val emailVerified: Boolean,
)

@Serializable
data class Tokens(
    val customerApiToken: AccessToken,
    val storefrontApiToken: String,
    val expiresAt: Long,
) {
    fun expired(): Boolean {
        val storedTokenExpiry = Instant.ofEpochMilli(expiresAt)
        return storedTokenExpiry.isBefore(Instant.now())
    }
}

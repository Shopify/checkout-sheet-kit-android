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
package com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Internal data model for Customer Accounts API
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

    val expiresAt: Long = Instant.now().plusSeconds(expiresIn).toEpochMilli()
) {
    fun hasExpired(): Boolean {
        return expiresAt < Instant.now().toEpochMilli()
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

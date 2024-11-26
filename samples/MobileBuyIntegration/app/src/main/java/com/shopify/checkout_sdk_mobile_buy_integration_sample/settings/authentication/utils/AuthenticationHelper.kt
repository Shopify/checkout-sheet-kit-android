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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.utils

import android.net.Uri
import android.util.Base64
import androidx.compose.ui.text.intl.Locale
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility functions for building Authentication related URLs, generating
 * code verifier, code challenge, and state values.
 */
class AuthenticationHelper(
    private val baseUrl: String
) {
    /**
     * Builds an [Authorization URL](https://shopify.dev/docs/api/customer#step-authorization) which
     * will redirect a customer to a login page
     */
    fun buildAuthorizationURL(
        codeVerifier: String,
        locale: Locale,
    ): String {
        val codeChallenge = codeChallenge(codeVerifier)
        val url = Uri.parse("$baseUrl/oauth/authorize").buildUpon()
            .appendQueryParameter("scope", "openid email customer-account-api:full")
            .appendQueryParameter("client_id", BuildConfig.customerAccountsApiClientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", BuildConfig.customerAccountsApiRedirectUri)
            .appendQueryParameter("state", state())
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("ui_locales", uiLocale(locale))
            .build()
            .toString()
        return url
    }

    /**
     * Builds a token URL used to obtain access tokens
     */
    fun buildTokenURL(): String {
        return "$baseUrl/oauth/token"
    }

    /**
     * Builds a logout URL
     */
    fun buildLogoutURL(idToken: String): String {
        return "$baseUrl/logout?id_token_hint=${idToken}"
    }

    /**
     * Generates a [code verifier](https://shopify.dev/docs/api/customer#publicclient-propertydetail-codeverifier) to be used
     * in the Authorization request (see PKCE)
     */
    fun createCodeVerifier(): String {
        val buffer = ByteArray(32)
        SecureRandom().nextBytes(buffer)
        return buffer.base64UrlEncode()
    }

    /**
     * Generates a [code challenge](https://shopify.dev/docs/api/customer#publicclient-propertydetail-codechallenge)
     * based on the code verifier used in the Authorization request (see PCKE)
     */
    private fun codeChallenge(verifier: String): String {
        val data = verifier.toByteArray(Charsets.UTF_8)
        val digest = data.sha256()
        return digest.base64UrlEncode()
    }

    /**
     * Generates a [state value](https://shopify.dev/docs/api/customer#authorization-propertydetail-state)
     */
    private fun state(): String {
        val random = SecureRandom()
        return (1..36)
            .map { ALLOWED_RANDOM_CHARS[random.nextInt(ALLOWED_RANDOM_CHARS.length)] }
            .joinToString("")
    }

    private fun ByteArray.sha256(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(this)
    }

    private fun ByteArray.base64UrlEncode(): String {
        return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Returns a [ui locale](https://shopify.dev/docs/api/customer#authorization-propertydetail-uilocales) for the login page to use
     */
    private fun uiLocale(locale: Locale): String {
        if (SUPPORTED_LOCALES.contains(locale.toString())) {
            return locale.toString()
        }

        if (SUPPORTED_LOCALES.contains(locale.language)) {
            return locale.language
        }

        return DEFAULT_LANGUAGE
    }

    companion object {
        private const val ALLOWED_RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        private val SUPPORTED_LOCALES = listOf(
            "en", "fr", "cs", "da", "de", "el", "es", "fi", "hi", "hr", "hu", "id", "it", "ja", "ko", "lt", "ms", "nb",
            "nl", "pl", "pt-BR", "pt-PT", "ro", "ru", "sk", "sl", "sv", "th", "tr", "vi", "zh-CN", "zh-TW"
        )
        private const val DEFAULT_LANGUAGE = "en"
    }
}

package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.utils

import android.net.Uri
import android.util.Base64
import androidx.compose.ui.text.intl.Locale
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom

object AuthenticationHelpers {
    private const val BASE_PATH = "https://shopify.com/authentication/${BuildConfig.shopId}"
    private const val ALLOWED_RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private val SUPPORTED_LOCALES = listOf(
        "en", "fr", "cs", "da", "de", "el", "es", "fi", "hi", "hr", "hu", "id", "it", "ja", "ko", "lt", "ms", "nb",
        "nl", "pl", "pt-BR", "pt-PT", "ro", "ru", "sk", "sl", "sv", "th", "tr", "vi", "zh-CN", "zh-TW"
    )
    private const val DEFAULT_LANGUAGE = "en"

    fun buildLoginPageUrl(
        codeVerifier: String,
        locale: Locale,
    ): String {
        Timber.i("Building login URL")
        val codeChallenge = codeChallenge(codeVerifier)
        return Uri.parse("${BASE_PATH}/oauth/authorize").buildUpon()
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
    }

    fun createCodeVerifier(): String {
        val buffer = ByteArray(32)
        SecureRandom().nextBytes(buffer)
        return buffer.base64UrlEncode()
    }

    fun buildLogoutPageUrl(idToken: String): String {
        return Uri.parse("$BASE_PATH/logout").buildUpon()
            .appendQueryParameter("id_token_hint", idToken)
            .build()
            .toString()
    }

    private fun codeChallenge(verifier: String): String {
        val data = verifier.toByteArray(Charsets.UTF_8)
        val digest = data.sha256()
        return digest.base64UrlEncode()
    }

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

    private fun uiLocale(locale: Locale): String {
        if (SUPPORTED_LOCALES.contains(locale.toString())) {
            return locale.toString()
        }

        if (SUPPORTED_LOCALES.contains(locale.language)) {
            return locale.language
        }

        return DEFAULT_LANGUAGE
    }
}

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
package com.shopify.checkoutsheetkit

import android.net.Uri

internal object QueryParamKey {
    internal const val EMBED = "embed"
}

internal object EmbedFieldKey {
    internal const val PROTOCOL = "protocol"
    internal const val BRANDING = "branding"
    internal const val LIBRARY = "library"
    internal const val SDK = "sdk"
    internal const val PLATFORM = "platform"
    internal const val ENTRY = "entry"
    internal const val COLOR_SCHEME = "colorscheme"
    internal const val RECOVERY = "recovery"
    internal const val AUTHENTICATION = "authentication"
}

internal object EmbedFieldValue {
    internal const val BRANDING_APP = "app"
    internal const val BRANDING_SHOP = "shop"
    internal const val ENTRY_SHEET = "sheet"
    internal const val COLOR_SCHEME_AUTO = "auto"
}

private object EmbedSeparators {
    const val FIELDS = ","       // Separates embed fields: "key1=val1,key2=val2"
    const val PARAMS = "&"       // Separates query parameters
    const val KEY_VALUE = "="    // Separates keys from values
}

internal object EmbedParamBuilder {
    fun build(
        isRecovery: Boolean = false,
        options: CheckoutOptions? = null,
        includeAuthentication: Boolean = true,
    ): String {
        val configuredColorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme

        val brandingValue = when (configuredColorScheme) {
            is ColorScheme.Web -> EmbedFieldValue.BRANDING_SHOP
            else -> EmbedFieldValue.BRANDING_APP
        }
        val colorScheme = when (configuredColorScheme) {
            is ColorScheme.Web -> null
            is ColorScheme.Automatic -> EmbedFieldValue.COLOR_SCHEME_AUTO
            else -> configuredColorScheme.id
        }

        val fields = mutableMapOf(
            EmbedFieldKey.PROTOCOL to CheckoutBridge.SCHEMA_VERSION,
            EmbedFieldKey.BRANDING to brandingValue,
            EmbedFieldKey.LIBRARY to "CheckoutKit/${BuildConfig.SDK_VERSION}",
            EmbedFieldKey.SDK to ShopifyCheckoutSheetKit.version.split("-").first(),
            EmbedFieldKey.PLATFORM to ShopifyCheckoutSheetKit.configuration.platform.displayName,
            EmbedFieldKey.ENTRY to EmbedFieldValue.ENTRY_SHEET,
            EmbedFieldKey.COLOR_SCHEME to colorScheme,
            EmbedFieldKey.RECOVERY to if (isRecovery) "true" else null,
        )

        if (includeAuthentication) {
            options?.authentication?.let { auth ->
                when (auth) {
                    is Authentication.Token -> fields[EmbedFieldKey.AUTHENTICATION] = auth.value
                    is Authentication.None -> {} // No authentication to include
                }
            }
        }

        return fields.entries
            .filter { (_, value) -> !value.isNullOrEmpty() }
            .joinToString(EmbedSeparators.FIELDS) { (key, value) ->
                "$key${EmbedSeparators.KEY_VALUE}$value"
            }
    }
}

/**
 * Adds or updates the embed query parameter on this URI with the provided options.
 *
 * @param isRecovery Whether this is a recovery/fallback checkout load
 * @param options Checkout configuration options including authentication token
 * @param includeAuthentication Whether to include the authentication token in the embed parameter
 * @return The complete URL string with the embed parameter added/updated
 */
internal fun Uri.withEmbedParam(
    isRecovery: Boolean = false,
    options: CheckoutOptions? = null,
    includeAuthentication: Boolean = true,
): String {
    val embedValue = EmbedParamBuilder.build(
        isRecovery = isRecovery,
        options = options,
        includeAuthentication = includeAuthentication,
    )

    val encodedEmbed = Uri.encode(embedValue)
    val existingParams = encodedQuery
        ?.split(EmbedSeparators.PARAMS)
        ?.filter { segment ->
            val paramName = segment.substringBefore(EmbedSeparators.KEY_VALUE)
            Uri.decode(paramName) != QueryParamKey.EMBED
        }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    val updatedQuery = buildList {
        addAll(existingParams)
        add("${QueryParamKey.EMBED}${EmbedSeparators.KEY_VALUE}$encodedEmbed")
    }.joinToString(separator = EmbedSeparators.PARAMS)

    return buildUpon()
        .encodedQuery(updatedQuery)
        .build()
        .toString()
}

/**
 * Checks whether the URL needs the embed parameter to be added or updated.
 *
 * This function compares the existing embed parameter (if present) with what the current
 * SDK configuration would generate. It returns true if:
 * - No embed parameter exists
 * - The embed parameter differs from current configuration (excluding authentication token)
 *
 * The authentication token is excluded from comparison because it's sent only once per
 * unique value and then omitted from subsequent navigations.
 *
 * @param isRecovery Whether this is a recovery/fallback checkout load
 * @param options Checkout configuration options
 * @return true if the embed parameter needs to be added or updated
 */
internal fun Uri.needsEmbedParam(
    isRecovery: Boolean = false,
    options: CheckoutOptions? = null,
): Boolean {
    val currentEmbedValue = getQueryParameter(QueryParamKey.EMBED) ?: return true
    val expectedEmbedValue = EmbedParamBuilder.build(
        isRecovery = isRecovery,
        options = options,
        includeAuthentication = false,
    )

    // Remove authentication field from current value for comparison
    val currentWithoutAuth = currentEmbedValue
        .split(EmbedSeparators.FIELDS)
        .map { it.trim() }
        .filter { field -> !field.startsWith("${EmbedFieldKey.AUTHENTICATION}${EmbedSeparators.KEY_VALUE}") }
        .joinToString(EmbedSeparators.FIELDS)

    return currentWithoutAuth != expectedEmbedValue
}

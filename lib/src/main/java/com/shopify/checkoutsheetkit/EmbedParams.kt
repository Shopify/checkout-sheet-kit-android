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
}

internal object EmbedFieldValue {
    internal const val BRANDING_APP = "app"
    internal const val BRANDING_SHOP = "shop"
    internal const val ENTRY_SHEET = "sheet"
}

internal object EmbedParamBuilder {
    fun build(isRecovery: Boolean = false): String {
        val configuredColorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme

        val brandingValue = when (configuredColorScheme) {
            is ColorScheme.Web -> EmbedFieldValue.BRANDING_SHOP
            else -> EmbedFieldValue.BRANDING_APP
        }
        val colorScheme = when (configuredColorScheme) {
            is ColorScheme.Web -> null
            is ColorScheme.Automatic -> null
            else -> configuredColorScheme.id
        }

        return mapOf(
            EmbedFieldKey.PROTOCOL to CheckoutBridge.SCHEMA_VERSION,
            EmbedFieldKey.BRANDING to brandingValue,
            EmbedFieldKey.LIBRARY to "CheckoutKit/${BuildConfig.SDK_VERSION}",
            EmbedFieldKey.SDK to ShopifyCheckoutSheetKit.version.split("-").first(),
            EmbedFieldKey.PLATFORM to ShopifyCheckoutSheetKit.configuration.platform.displayName,
            EmbedFieldKey.ENTRY to EmbedFieldValue.ENTRY_SHEET,
            EmbedFieldKey.COLOR_SCHEME to colorScheme,
            EmbedFieldKey.RECOVERY to if (isRecovery) "true" else null
        )
            .filterNot { it.value.isNullOrEmpty() }
            .map { (key, value) -> "$key=$value" }
            .joinToString(", ")
    }
}

internal fun Uri.withEmbedParam(isRecovery: Boolean = false): String {
    if (!needsEmbedParam()) {
        return this.toString()
    }

    val embedValue = EmbedParamBuilder.build(isRecovery)

    return this.buildUpon()
        .appendQueryParameter(QueryParamKey.EMBED, embedValue)
        .build()
        .toString()
}

internal fun Uri.needsEmbedParam(): Boolean {
    return this.getQueryParameter(QueryParamKey.EMBED) == null
}

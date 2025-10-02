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

import androidx.core.net.toUri

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
        val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
        val brandingValue = when (colorScheme) {
            is ColorScheme.Web -> EmbedFieldValue.BRANDING_SHOP
            else -> EmbedFieldValue.BRANDING_APP
        }

        val embedParts = mutableListOf<String>()
        embedParts.add("${EmbedFieldKey.PROTOCOL}=${CheckoutBridge.SCHEMA_VERSION}")
        embedParts.add("${EmbedFieldKey.BRANDING}=$brandingValue")

        val libraryVersion = BuildConfig.SDK_VERSION
        embedParts.add("${EmbedFieldKey.LIBRARY}=CheckoutKit/$libraryVersion")

        val sdkVersion = ShopifyCheckoutSheetKit.version.split("-").first()
        embedParts.add("${EmbedFieldKey.SDK}=$sdkVersion")

        val platformDisplay = ShopifyCheckoutSheetKit.configuration.platform.displayName
        embedParts.add("${EmbedFieldKey.PLATFORM}=$platformDisplay")

        embedParts.add("${EmbedFieldKey.ENTRY}=${EmbedFieldValue.ENTRY_SHEET}")

        if (colorScheme !is ColorScheme.Web && colorScheme !is ColorScheme.Automatic) {
            embedParts.add("${EmbedFieldKey.COLOR_SCHEME}=${colorScheme.id}")
        }

        if (isRecovery) {
            embedParts.add("${EmbedFieldKey.RECOVERY}=true")
        }

        return embedParts.joinToString(", ")
    }
}

internal fun String.withEmbedParam(isRecovery: Boolean = false): String {
    val embedValue = EmbedParamBuilder.build(isRecovery)

    return this.toUri()
        .buildUpon()
        .appendQueryParameter(QueryParamKey.EMBED, embedValue)
        .build()
        .toString()
}

internal fun String.needsEmbedParam(): Boolean {
    val uri = this.toUri()
    return uri.getQueryParameter(QueryParamKey.EMBED) == null
}

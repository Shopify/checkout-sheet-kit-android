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

internal object Headers {
    internal const val PURPOSE = "Sec-Purpose"
    internal const val PURPOSE_PREFETCH = "prefetch"
}

internal object QueryParams {
    internal const val EMBED = "embed"
    internal const val BRANDING_APP = "app"
    internal const val BRANDING_SHOP = "shop"
}

internal fun checkoutKitHeaders(isPreload: Boolean = false): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()

    if (isPreload) {
        headers[Headers.PURPOSE] = Headers.PURPOSE_PREFETCH
    }

    return headers
}

internal fun String.withEmbedParam(): String {
    val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
    val brandingValue = when (colorScheme) {
        is ColorScheme.Web -> QueryParams.BRANDING_SHOP
        else -> QueryParams.BRANDING_APP
    }

    val embedParts = mutableListOf<String>()
    embedParts.add("branding=$brandingValue")

    // Add color scheme if it's not Web or Automatic
    if (colorScheme !is ColorScheme.Web && colorScheme !is ColorScheme.Automatic) {
        embedParts.add("colorscheme=${colorScheme.id}")
    }

    val embedValue = embedParts.joinToString(", ")

    return this.toUri()
        .buildUpon()
        .appendQueryParameter(QueryParams.EMBED, embedValue)
        .build()
        .toString()
}

internal fun String.needsEmbedParam(): Boolean {
    val uri = this.toUri()
    return uri.getQueryParameter(QueryParams.EMBED) == null
}

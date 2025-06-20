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

internal object Headers {
    internal const val PURPOSE = "Sec-Purpose"
    internal const val PURPOSE_PREFETCH = "prefetch"

    internal const val PREFERS_COLOR_SCHEME = "Sec-CH-Prefers-Color-Scheme"

    internal const val BRANDING = "X-Shopify-Checkout-Kit-Branding"
    internal const val BRANDING_CHECKOUT_KIT = "CHECKOUT_KIT"
    internal const val BRANDING_WEB = "WEB_DEFAULT"
}

internal fun checkoutKitHeaders(isPreload: Boolean = false): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()

    if (isPreload) {
        headers[Headers.PURPOSE] = Headers.PURPOSE_PREFETCH
    }

    return headers
        .withColorScheme()
        .withBranding()
}

internal fun MutableMap<String, String>.withColorScheme(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    headers.putAll(this)

    val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
    if (colorScheme !is ColorScheme.Web && colorScheme !is ColorScheme.Automatic) {
        headers[Headers.PREFERS_COLOR_SCHEME] = colorScheme.id
    }

    return headers
}

internal fun MutableMap<String, String>.withBranding(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    headers.putAll(this)

    val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
    when (colorScheme) {
        is ColorScheme.Web -> headers[Headers.BRANDING] = Headers.BRANDING_WEB
        else -> headers[Headers.BRANDING] = Headers.BRANDING_CHECKOUT_KIT
    }

    return headers
}

internal fun MutableMap<String, String>.hasColorSchemeHeader() = hasHeader(Headers.PREFERS_COLOR_SCHEME)

internal fun MutableMap<String, String>.hasBrandingHeader() = hasHeader(Headers.BRANDING)

private fun MutableMap<String, String>.hasHeader(headerName: String) = this.keys.any {
    it.equals(headerName, ignoreCase = true)
}

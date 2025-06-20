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

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class HeadersTest {

    private lateinit var initialConfiguration: Configuration

    @Before
    fun setUp() {
        initialConfiguration = ShopifyCheckoutSheetKit.configuration
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = initialConfiguration.colorScheme
            it.preloading = initialConfiguration.preloading
            it.errorRecovery = initialConfiguration.errorRecovery
        }
    }

    @Test
    fun `checkoutKitHeaders includes color scheme and branding for dark color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val headers = checkoutKitHeaders()

        assertThat(headers[Headers.PREFERS_COLOR_SCHEME]).isEqualTo("dark")
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(headers).doesNotContainKey(Headers.PURPOSE)
        assertThat(headers.size).isEqualTo(2)
    }

    @Test
    fun `checkoutKitHeaders includes color scheme and branding for light color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        val headers = checkoutKitHeaders()

        assertThat(headers[Headers.PREFERS_COLOR_SCHEME]).isEqualTo("light")
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(headers).doesNotContainKey(Headers.PURPOSE)
        assertThat(headers.size).isEqualTo(2)
    }

    @Test
    fun `checkoutKitHeaders only includes branding for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val headers = checkoutKitHeaders()

        assertThat(headers).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_WEB)
        assertThat(headers).doesNotContainKey(Headers.PURPOSE)
        assertThat(headers.size).isEqualTo(1)
    }

    @Test
    fun `checkoutKitHeaders only includes branding for automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        val headers = checkoutKitHeaders()

        assertThat(headers).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(headers).doesNotContainKey(Headers.PURPOSE)
        assertThat(headers.size).isEqualTo(1)
    }

    @Test
    fun `checkoutKitHeaders includes purpose header when isPreload is true`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val headers = checkoutKitHeaders(isPreload = true)

        assertThat(headers[Headers.PURPOSE]).isEqualTo(Headers.PURPOSE_PREFETCH)
        assertThat(headers[Headers.PREFERS_COLOR_SCHEME]).isEqualTo("dark")
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(headers.size).isEqualTo(3)
    }

    @Test
    fun `checkoutKitHeaders with isPreload true and web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val headers = checkoutKitHeaders(isPreload = true)

        assertThat(headers[Headers.PURPOSE]).isEqualTo(Headers.PURPOSE_PREFETCH)
        assertThat(headers).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_WEB)
        assertThat(headers.size).isEqualTo(2)
    }

    @Test
    fun `checkoutKitHeaders with isPreload true and automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        val headers = checkoutKitHeaders(isPreload = true)

        assertThat(headers[Headers.PURPOSE]).isEqualTo(Headers.PURPOSE_PREFETCH)
        assertThat(headers).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(headers[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(headers.size).isEqualTo(2)
    }

    @Test
    fun `withColorScheme adds color scheme header for dark color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val headers = mutableMapOf("existing" to "value")
        val result = headers.withColorScheme()

        assertThat(result["existing"]).isEqualTo("value")
        assertThat(result[Headers.PREFERS_COLOR_SCHEME]).isEqualTo("dark")
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `withColorScheme adds color scheme header for light color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        val headers = mutableMapOf<String, String>()
        val result = headers.withColorScheme()

        assertThat(result[Headers.PREFERS_COLOR_SCHEME]).isEqualTo("light")
    }

    @Test
    fun `withColorScheme does not add color scheme header for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val headers = mutableMapOf("existing" to "value")
        val result = headers.withColorScheme()

        assertThat(result["existing"]).isEqualTo("value")
        assertThat(result).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `withColorScheme does not add color scheme header for automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        val headers = mutableMapOf("existing" to "value")
        val result = headers.withColorScheme()

        assertThat(result["existing"]).isEqualTo("value")
        assertThat(result).doesNotContainKey(Headers.PREFERS_COLOR_SCHEME)
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `withBranding adds checkout kit branding for non-web color schemes`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val headers = mutableMapOf("existing" to "value")
        val result = headers.withBranding()

        assertThat(result["existing"]).isEqualTo("value")
        assertThat(result[Headers.BRANDING]).isEqualTo(Headers.BRANDING_CHECKOUT_KIT)
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `withBranding adds web branding for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val headers = mutableMapOf("existing" to "value")
        val result = headers.withBranding()

        assertThat(result["existing"]).isEqualTo("value")
        assertThat(result[Headers.BRANDING]).isEqualTo(Headers.BRANDING_WEB)
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `hasColorSchemeHeader returns true when color scheme header exists`() {
        val headers = mutableMapOf(Headers.PREFERS_COLOR_SCHEME to "dark")

        assertThat(headers.hasColorSchemeHeader()).isTrue
    }

    @Test
    fun `hasColorSchemeHeader returns false when color scheme header does not exist`() {
        val headers = mutableMapOf("other" to "value")

        assertThat(headers.hasColorSchemeHeader()).isFalse
    }

    @Test
    fun `hasColorSchemeHeader is case insensitive`() {
        val headers = mutableMapOf(Headers.PREFERS_COLOR_SCHEME.lowercase() to "dark")

        assertThat(headers.hasColorSchemeHeader()).isTrue
    }

    @Test
    fun `hasBrandingHeader returns true when branding header exists`() {
        val headers = mutableMapOf(Headers.BRANDING to Headers.BRANDING_CHECKOUT_KIT)

        assertThat(headers.hasBrandingHeader()).isTrue
    }

    @Test
    fun `hasBrandingHeader returns false when branding header does not exist`() {
        val headers = mutableMapOf("other" to "value")

        assertThat(headers.hasBrandingHeader()).isFalse
    }

    @Test
    fun `hasBrandingHeader is case insensitive`() {
        val headers = mutableMapOf(Headers.BRANDING.lowercase() to Headers.BRANDING_CHECKOUT_KIT)

        assertThat(headers.hasBrandingHeader()).isTrue
    }
}

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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestHelpersTest {

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
    fun `checkoutKitHeaders returns empty map for non-preload requests`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val headers = checkoutKitHeaders()

        assertThat(headers).doesNotContainKey(Headers.PURPOSE)
        assertThat(headers.size).isEqualTo(0)
    }

    @Test
    fun `checkoutKitHeaders includes purpose header when isPreload is true`() {
        val headers = checkoutKitHeaders(isPreload = true)

        assertThat(headers[Headers.PURPOSE]).isEqualTo(Headers.PURPOSE_PREFETCH)
        assertThat(headers.size).isEqualTo(1)
    }

    @Test
    fun `withEmbedParam adds embed parameter with branding and color scheme for non-web color schemes`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val result = "https://example.com".withEmbedParam()

        assertThat(result).isEqualTo("https://example.com?embed=branding%3Dapp%2C%20color_scheme%3Ddark")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val result = "https://example.com".withEmbedParam()

        assertThat(result).isEqualTo("https://example.com?embed=branding%3Dshop")
    }

    @Test
    fun `withEmbedParam handles existing query parameters`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val result = "https://example.com?existing=value".withEmbedParam()

        assertThat(result).isEqualTo("https://example.com?existing=value&embed=branding%3Dapp%2C%20color_scheme%3Ddark")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        val result = "https://example.com".withEmbedParam()

        assertThat(result).isEqualTo("https://example.com?embed=branding%3Dapp")
    }

    @Test
    fun `needsEmbedParam returns true when embed parameter is missing`() {
        val needsEmbed = "https://example.com".needsEmbedParam()

        assertThat(needsEmbed).isTrue
    }

    @Test
    fun `needsEmbedParam returns false when embed parameter is present`() {
        val needsEmbed = "https://example.com?embed=branding%3Dapp%2C%20color_scheme%3Ddark".needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `needsEmbedParam handles existing query parameters correctly`() {
        val needsEmbed = "https://example.com?other=value&embed=branding%3Dapp%2C%20color_scheme%3Ddark&another=value".needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }
}

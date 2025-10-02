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
import com.shopify.checkoutsheetkit.BuildConfig
import com.shopify.checkoutsheetkit.CheckoutBridge
import com.shopify.checkoutsheetkit.EmbedFieldKey
import com.shopify.checkoutsheetkit.EmbedFieldValue
import com.shopify.checkoutsheetkit.Platform
import com.shopify.checkoutsheetkit.QueryParamKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmbedParamsTest {

    private lateinit var initialConfiguration: Configuration

    @Before
    fun setUp() {
        initialConfiguration = ShopifyCheckoutSheetKit.configuration.copy()
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = initialConfiguration.colorScheme
            it.preloading = initialConfiguration.preloading
            it.errorRecovery = initialConfiguration.errorRecovery
            it.platform = initialConfiguration.platform
        }
    }

    @Test
    fun `withEmbedParam adds embed parameter with branding and color scheme for non-web color schemes`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val result = "https://example.com".withEmbedParam()

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}, " +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )

        assertThat(result).isEqualTo("https://example.com?${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        val result = "https://example.com".withEmbedParam()

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_SHOP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}"
        )

        assertThat(result).isEqualTo("https://example.com?${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `withEmbedParam handles existing query parameters`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val result = "https://example.com?existing=value".withEmbedParam()

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}, " +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )

        assertThat(result).isEqualTo("https://example.com?existing=value&${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        val result = "https://example.com".withEmbedParam()

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}"
        )

        assertThat(result).isEqualTo("https://example.com?${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `withEmbedParam includes configured platform`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
            it.platform = Platform.REACT_NATIVE
        }

        val result = "https://example.com".withEmbedParam()

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.REACT_NATIVE.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}"
        )

        assertThat(result).isEqualTo("https://example.com?${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `withEmbedParam includes recovery flag when requested`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        val result = "https://example.com".withEmbedParam(isRecovery = true)

        val expectedEmbed = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}, " +
                "${EmbedFieldKey.COLOR_SCHEME}=light, " +
                "${EmbedFieldKey.RECOVERY}=true"
        )

        assertThat(result).isEqualTo("https://example.com?${QueryParamKey.EMBED}=$expectedEmbed")
    }

    @Test
    fun `needsEmbedParam returns true when embed parameter is missing`() {
        val needsEmbed = "https://example.com".needsEmbedParam()

        assertThat(needsEmbed).isTrue
    }

    @Test
    fun `needsEmbedParam returns false when embed parameter is present`() {
        val embedValue = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}, " +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )
        val needsEmbed = "https://example.com?${QueryParamKey.EMBED}=$embedValue".needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `needsEmbedParam handles existing query parameters correctly`() {
        val embedValue = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}, " +
                "branding=${EmbedFieldValue.BRANDING_APP}, " +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}, " +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}, " +
                "platform=${Platform.ANDROID.displayName}, " +
                "entry=${EmbedFieldValue.ENTRY_SHEET}, " +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )
        val needsEmbed = "https://example.com?other=value&${QueryParamKey.EMBED}=$embedValue".needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }
}

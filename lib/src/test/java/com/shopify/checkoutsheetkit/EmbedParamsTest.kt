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
import androidx.core.net.toUri
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
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

        assertThat("https://example.com".toUri().withEmbedParam().toUri())
            .hasBaseUrl("https://example.com")
            .withEmbedParameters(EmbedFieldKey.COLOR_SCHEME to "dark")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for web color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        assertThat("https://example.com".toUri().withEmbedParam().toUri())
            .hasBaseUrl("https://example.com")
            .withEmbedParameters(EmbedFieldKey.BRANDING to EmbedFieldValue.BRANDING_SHOP)
            .withoutEmbedParameters(EmbedFieldKey.COLOR_SCHEME)
    }

    @Test
    fun `withEmbedParam preserves existing query parameters`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        assertThat("https://example.com?existing=value".toUri().withEmbedParam().toUri())
            .hasBaseUrl("https://example.com")
            .hasQueryParameter("existing", "value")
            .withEmbedParameters(EmbedFieldKey.COLOR_SCHEME to "dark")
    }

    @Test
    fun `withEmbedParam adds embed parameter with only branding for automatic color scheme`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        assertThat("https://example.com".toUri().withEmbedParam().toUri())
            .hasBaseUrl("https://example.com")
            .withEmbedParameters(EmbedFieldKey.BRANDING to EmbedFieldValue.BRANDING_APP)
            .withoutEmbedParameters(EmbedFieldKey.COLOR_SCHEME)
    }

    @Test
    fun `withEmbedParam includes configured platform`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
            it.platform = Platform.REACT_NATIVE
        }

        assertThat("https://example.com".toUri().withEmbedParam().toUri())
            .hasBaseUrl("https://example.com")
            .withEmbedParameters(EmbedFieldKey.PLATFORM to Platform.REACT_NATIVE.displayName)
    }

    @Test
    fun `withEmbedParam sets platform to react-native-android when using React Native`() {
        ShopifyCheckoutSheetKit.configure {
            it.platform = Platform.REACT_NATIVE
        }

        assertThat("https://example.com".toUri().withEmbedParam().toUri())
            .withEmbedParameters(EmbedFieldKey.PLATFORM to "react-native-android")
    }

    @Test
    fun `withEmbedParam includes recovery flag when requested`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        assertThat("https://example.com".toUri().withEmbedParam(isRecovery = true).toUri())
            .hasBaseUrl("https://example.com")
            .withEmbedParameters(
                EmbedFieldKey.COLOR_SCHEME to "light",
                EmbedFieldKey.RECOVERY to "true",
            )
    }

    @Test
    fun `needsEmbedParam returns true when embed parameter is missing`() {
        val uri = "https://example.com".toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isTrue
    }

    @Test
    fun `needsEmbedParam returns false when embed parameter matches current configuration`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val embedValue = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}," +
                "branding=${EmbedFieldValue.BRANDING_APP}," +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}," +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}," +
                "platform=${Platform.ANDROID.displayName}," +
                "entry=${EmbedFieldValue.ENTRY_SHEET}," +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )
        val uri = "https://example.com?${QueryParamKey.EMBED}=$embedValue".toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `needsEmbedParam returns true when embed parameter does not match current configuration`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light() // Configured as Light
        }

        val embedValue = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}," +
                "branding=${EmbedFieldValue.BRANDING_APP}," +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}," +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}," +
                "platform=${Platform.ANDROID.displayName}," +
                "entry=${EmbedFieldValue.ENTRY_SHEET}," +
                "${EmbedFieldKey.COLOR_SCHEME}=dark" // But URL has Dark
        )
        val uri = "https://example.com?${QueryParamKey.EMBED}=$embedValue".toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isTrue // Needs update because of mismatch
    }

    @Test
    fun `needsEmbedParam returns false when embed parameter exists alongside other query parameters`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        val embedValue = Uri.encode(
            "protocol=${CheckoutBridge.SCHEMA_VERSION}," +
                "branding=${EmbedFieldValue.BRANDING_APP}," +
                "library=CheckoutKit/${BuildConfig.SDK_VERSION}," +
                "sdk=${ShopifyCheckoutSheetKit.version.split("-").first()}," +
                "platform=${Platform.ANDROID.displayName}," +
                "entry=${EmbedFieldValue.ENTRY_SHEET}," +
                "${EmbedFieldKey.COLOR_SCHEME}=dark"
        )
        val uri = "https://example.com?other=value&${QueryParamKey.EMBED}=$embedValue".toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `Uri withEmbedParam is idempotent`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        val url = "https://example.com".toUri()
        val first = url.withEmbedParam()
        val second = first.toUri().withEmbedParam()

        assertThat(second).isEqualTo(first)
    }

    @Test
    fun `needsEmbedParam returns true for URL without embed parameter`() {
        val uri = "https://example.com".toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isTrue
    }

    @Test
    fun `needsEmbedParam returns false for URL with current embed parameter`() {
        val withEmbed = "https://example.com".toUri().withEmbedParam()
        val uri = withEmbed.toUri()

        val needsEmbed = uri.needsEmbedParam()

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `withEmbedParam adds authentication when provided`() {
        val options = CheckoutOptions(
            authToken = "token-123",
        )
        val uri = "https://example.com".toUri()

        val result = uri.withEmbedParam(options = options)

        assertThat(result.toUri())
            .withEmbedParameters(EmbedFieldKey.AUTHENTICATION to "token-123")
    }

    @Test
    fun `needsEmbedParam ignores authentication field when comparing embed parameters`() {
        val options = CheckoutOptions(
            authToken = "token-abc",
        )
        val withEmbed = "https://example.com".toUri().withEmbedParam(options = options)
        val uri = withEmbed.toUri()

        val needsEmbed = uri.needsEmbedParam(options = options)

        assertThat(needsEmbed).isFalse
    }

    @Test
    fun `withEmbedParam excludes authentication when includeAuthentication is false`() {
        val options = CheckoutOptions(authToken = "token-123")
        val uri = "https://example.com".toUri()

        val result = uri.withEmbedParam(options = options, includeAuthentication = false)

        assertThat(result.toUri())
            .withoutEmbedParameters(EmbedFieldKey.AUTHENTICATION)
    }

    @Test
    fun `withEmbedParam replaces existing authentication token in embed parameter`() {
        val oldOptions = CheckoutOptions(authToken = "old-token")
        val newOptions = CheckoutOptions(authToken = "new-token")
        val withOldToken = "https://example.com".toUri()
            .withEmbedParam(options = oldOptions)
        val uri = withOldToken.toUri()

        val result = uri.withEmbedParam(options = newOptions)

        assertThat(result.toUri())
            .withEmbedParameters(EmbedFieldKey.AUTHENTICATION to "new-token")
    }

    @Test
    fun `withEmbedParam excludes authentication when options authToken is null`() {
        val options = CheckoutOptions(authToken = null)
        val uri = "https://example.com".toUri()

        val result = uri.withEmbedParam(options = options)

        assertThat(result.toUri())
            .withoutEmbedParameters(EmbedFieldKey.AUTHENTICATION)
    }
}

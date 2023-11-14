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
package com.shopify.checkoutkit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CheckoutBridgeMetadataTest {

    @Test
    fun `user agent suffix includes ShopifyCheckoutSDK and version number`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Dark()
        assertThat(CheckoutBridgeMetadata.userAgentSuffix()).startsWith("ShopifyCheckoutSDK/${BuildConfig.SDK_VERSION} ")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - dark`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Dark()
        assertThat(CheckoutBridgeMetadata.userAgentSuffix()).endsWith("(5.3;dark;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - light`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Light()
        assertThat(CheckoutBridgeMetadata.userAgentSuffix()).endsWith("(5.3;light;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - web`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Web()
        assertThat(CheckoutBridgeMetadata.userAgentSuffix()).endsWith("(5.3;web_default;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - automatic`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Automatic()
        assertThat(CheckoutBridgeMetadata.userAgentSuffix()).endsWith("(5.3;automatic;standard)")
    }
}

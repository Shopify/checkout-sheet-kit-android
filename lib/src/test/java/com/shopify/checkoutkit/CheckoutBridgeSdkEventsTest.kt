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

import android.webkit.WebView
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CheckoutBridgeSdkEventsTest {

    @Test
    fun `user agent suffix includes ShopifyCheckoutSDK and version number`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Dark()
        assertThat(CheckoutBridge.userAgentSuffix()).startsWith("ShopifyCheckoutSDK/${BuildConfig.SDK_VERSION} ")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - dark`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Dark()
        assertThat(CheckoutBridge.userAgentSuffix()).endsWith("(6.0;dark;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - light`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Light()
        assertThat(CheckoutBridge.userAgentSuffix()).endsWith("(6.0;light;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - web`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Web()
        assertThat(CheckoutBridge.userAgentSuffix()).endsWith("(6.0;web_default;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - automatic`() {
        ShopifyCheckoutKit.configuration.colorScheme = ColorScheme.Automatic()
        assertThat(CheckoutBridge.userAgentSuffix()).endsWith("(6.0;automatic;standard)")
    }

    @Test
    fun `instrumentation sends message to the bridge`() {
        val webView = Mockito.mock(WebView::class.java)
        val payload = InstrumentationPayload(
            name = "Test",
            value = 123L,
            type = InstrumentationType.Histogram,
            tags = mapOf("tag1" to "value1", "tag2" to "value2")
        )
        val expectedPayload = """{"detail":{"name":"Test","value":123,"type":"histogram","tags":{"tag1":"value1","tag2":"value2"}}}"""
        val expectedJavascript = "window.MobileCheckoutSdk.dispatchMessage('instrumentation', ${expectedPayload})"
        val timeoutEncapsulatedExpectation = "setTimeout(()=> {${expectedJavascript}; }, 2000)"

        CheckoutBridge.instrument(webView, payload)
        verify(webView).evaluateJavascript(timeoutEncapsulatedExpectation, null)
    }
}

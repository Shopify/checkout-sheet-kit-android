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

import android.graphics.Color
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class FallbackWebViewTest {

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configuration.platform = Platform.ANDROID
    }

    @Test
    fun `configures web view on initialization`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())

            assertThat(view.visibility).isEqualTo(VISIBLE)
            assertThat(view.settings.javaScriptEnabled).isTrue
            assertThat(view.settings.domStorageEnabled).isTrue
            assertThat(view.layoutParams.height).isEqualTo(MATCH_PARENT)
            assertThat(view.layoutParams.width).isEqualTo(MATCH_PARENT)
            assertThat(view.id).isNotNull
            assertThat(shadowOf(view).webViewClient.javaClass).isEqualTo(FallbackWebView.FallbackWebViewClient::class.java)
            assertThat(shadowOf(view).backgroundColor).isEqualTo(Color.TRANSPARENT)
            assertThat(shadowOf(view).getJavascriptInterface("android")).isNull()
        }
    }

    @Test
    fun `calls update progress when new progress is reported by WebChromeClient`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val webViewEventProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(webViewEventProcessor)

            val shadow = shadowOf(view)

            shadow.webChromeClient?.onProgressChanged(view, 20)
            verify(webViewEventProcessor).updateProgressBar(20)

            shadow.webChromeClient?.onProgressChanged(view, 50)
            verify(webViewEventProcessor).updateProgressBar(50)
        }
    }

    @Test
    fun `should not recover from errors`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            assertThat(view.recoverErrors).isFalse()
        }
    }

    @Test
    fun `loadUrl adds recovery flag to embed parameter`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())

            val url = "https://checkout.shopify.com".toUri().withEmbedParam(isRecovery = true)
            view.loadUrl(url)

            assertThat(shadowOf(view).lastLoadedUrl.toUri())
                .hasBaseUrl("https://checkout.shopify.com")
                .withEmbedParameters(EmbedFieldKey.RECOVERY to "true")
        }
    }

    @Test
    fun `loadCheckout adds authentication once for fallback view`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())

            view.loadCheckout(
                url = "https://checkout.shopify.com",
                options = CheckoutOptions(authToken = "token-1")
            )

            view.FallbackWebViewClient().onPageFinished(view, "https://checkout.shopify.com")

            assertThat(shadowOf(view).lastLoadedUrl.toUri())
                .hasBaseUrl("https://checkout.shopify.com")
                .withEmbedParameters(
                    EmbedFieldKey.RECOVERY to "true",
                    EmbedFieldKey.AUTHENTICATION to "token-1",
                )

            view.loadCheckout("https://checkout.shopify.com")

            assertThat(shadowOf(view).lastLoadedUrl.toUri())
                .hasBaseUrl("https://checkout.shopify.com")
                .withEmbedParameters(EmbedFieldKey.RECOVERY to "true")
                .withoutEmbedParameters(EmbedFieldKey.AUTHENTICATION)
        }
    }
}

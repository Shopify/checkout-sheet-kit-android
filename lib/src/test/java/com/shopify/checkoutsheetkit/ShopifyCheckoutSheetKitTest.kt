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

import androidx.activity.ComponentActivity
import android.net.Uri
import com.shopify.checkoutsheetkit.BuildConfig
import com.shopify.checkoutsheetkit.CheckoutBridge
import com.shopify.checkoutsheetkit.EmbedFieldKey
import com.shopify.checkoutsheetkit.EmbedFieldValue
import com.shopify.checkoutsheetkit.QueryParamKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class ShopifyCheckoutSheetKitTest {

    @Before
    fun setUp() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = false)
            it.colorScheme = ColorScheme.Automatic()
        }
    }

    @After
    fun tearDown() {
        CheckoutWebView.cacheEntry = null
    }

    @Test
    fun `preload is a noop if preloading is not enabled`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val url = "https://shopify.dev"
            ShopifyCheckoutSheetKit.preload(url, activityController.get())
            ShadowLooper.shadowMainLooper().runToEndOfTasks()

            assertThat(CheckoutWebView.cacheEntry).isNull()
        }
    }

    @Test
    fun `preload caches a WebView and loads the URL if cache is currently empty`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val url = "https://shopify.dev"
                ShopifyCheckoutSheetKit.preload(url, activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                assertThat(CheckoutWebView.cacheEntry).isNotNull()
                val entry = CheckoutWebView.cacheEntry!!

                assertThat(entry.isStale).isFalse()
                assertThat(entry.view).isInstanceOf(CheckoutWebView::class.java)
            }
        }
    }

    @Test
    fun `invalidate marks cache entry as stale meaning it will not be used when presenting`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val url = "https://shopify.dev"
                ShopifyCheckoutSheetKit.preload(url, activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                ShopifyCheckoutSheetKit.invalidate()
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                assertThat(CheckoutWebView.cacheEntry).isNotNull()
                val entry = CheckoutWebView.cacheEntry!!

                assertThat(entry.isStale).isTrue()
            }
        }
    }

    @Test
    fun `preload caches a new WebView, loads the URL, and destroys old view if cache is not empty`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val url = "https://shopify.dev"
                ShopifyCheckoutSheetKit.preload(url, activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()
                val originalEntry = CheckoutWebView.cacheEntry

                ShopifyCheckoutSheetKit.preload(url, activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                assertThat(CheckoutWebView.cacheEntry).isNotNull()
                val entry = CheckoutWebView.cacheEntry!!

                assertThat(entry.isStale).isFalse()
                assertThat(entry.view).isInstanceOf(CheckoutWebView::class.java)
                assertThat(entry.view).isNotEqualTo(originalEntry)
                assertThat(shadowOf(originalEntry?.view).wasDestroyCalled()).isTrue()
            }
        }
    }

    @Test
    fun `preload while presented loads url in the existing view`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val activity = activityController.get()

                // first preload caches the view
                ShopifyCheckoutSheetKit.preload("https://one.com", activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                val originalEntry = CheckoutWebView.cacheEntry
                assertThat(originalEntry!!.key).isEqualTo("https://one.com")
                assertThat(originalEntry.isStale).isFalse()

                // present loads the cached view
                ShopifyCheckoutSheetKit.present("https://one.com", activity, noopDefaultCheckoutEventProcessor(activity))
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                val secondEntry = CheckoutWebView.cacheEntry
                assertThat(secondEntry!!.key).isEqualTo("https://one.com")
                assertThat(secondEntry.isStale).isFalse()

                // preload after present loads URL in the cached view
                ShopifyCheckoutSheetKit.preload("https://one.com", activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                val thirdEntry = CheckoutWebView.cacheEntry
                assertThat(thirdEntry!!.key).isEqualTo("https://one.com")
                assertThat(thirdEntry.isStale).isFalse()

                assertEmbed(shadowOf(thirdEntry.view).lastLoadedUrl, "https://one.com", defaultEmbed())
            }
        }
    }

    @Test
    fun `preload after presented loads the url and marks cache stale if url doesn't match cache key`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val activity = activityController.get()

                // first preload caches the view
                ShopifyCheckoutSheetKit.preload("https://one.com", activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                val originalEntry = CheckoutWebView.cacheEntry
                assertThat(originalEntry?.key).isEqualTo("https://one.com")
                assertThat(originalEntry?.isStale).isFalse()

                // present loads the cached view
                ShopifyCheckoutSheetKit.present("https://one.com", activity, noopDefaultCheckoutEventProcessor(activity))
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                val secondEntry = CheckoutWebView.cacheEntry
                assertThat(secondEntry?.key).isEqualTo("https://one.com")
                assertThat(secondEntry?.isStale).isFalse()

                // preload after present loads URL in the cached view
                ShopifyCheckoutSheetKit.preload("https://two.com", activityController.get())
                ShadowLooper.shadowMainLooper().runToEndOfTasks()

                // as the URL no longer matches the cache key, mark the cache entry as stale
                val thirdEntry = CheckoutWebView.cacheEntry
                assertThat(thirdEntry?.key).isEqualTo("https://one.com")
                assertThat(thirdEntry?.isStale).isTrue()

                assertEmbed(shadowOf(thirdEntry?.view).lastLoadedUrl, "https://two.com", defaultEmbed())
            }
        }
    }

    private fun assertEmbed(actualUrl: String?, expectedBase: String, expectedEmbed: Map<String, String>) {
        assertThat(actualUrl).isNotNull
        val uri = Uri.parse(actualUrl)
        val base = buildString {
            append(uri.scheme)
            append("://")
            append(uri.host)
            if (uri.port != -1) {
                append(":")
                append(uri.port)
            }
            append(uri.path ?: "")
        }
        assertThat(base).isEqualTo(expectedBase)

        val embedParam = uri.getQueryParameter(QueryParamKey.EMBED)
        assertThat(embedParam).isNotNull

        val actualMap = embedParam!!.split(", ").associate { entry ->
            val parts = entry.split("=")
            parts[0] to parts[1]
        }

        assertThat(actualMap).containsExactlyInAnyOrderEntriesOf(expectedEmbed)
    }

    private fun defaultEmbed(): Map<String, String> {
        val sdkVersion = ShopifyCheckoutSheetKit.version.split("-").first()
        return mapOf(
            EmbedFieldKey.PROTOCOL to CheckoutBridge.SCHEMA_VERSION,
            EmbedFieldKey.BRANDING to EmbedFieldValue.BRANDING_APP,
            EmbedFieldKey.LIBRARY to "CheckoutKit/${BuildConfig.SDK_VERSION}",
            EmbedFieldKey.SDK to sdkVersion,
            EmbedFieldKey.PLATFORM to Platform.ANDROID.displayName,
            EmbedFieldKey.ENTRY to EmbedFieldValue.ENTRY_SHEET,
        )
    }
}

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

import android.os.Looper
import androidx.activity.ComponentActivity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewCacheTest {

    private lateinit var activity: ComponentActivity
    private lateinit var eventProcessor: CheckoutEventProcessor

    @Before
    fun setUp() {
        CheckoutWebView.clearCache()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
        eventProcessor = eventProcessor()
    }

    @Test
    fun `cacheable checkout view returns a view if preloading enabled and cache is empty`() {
        withPreloadingEnabled {
            val view = CheckoutWebView.cacheableCheckoutView(URL, activity)
            assertThat(view).isNotNull
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            assertThat(shadowOf(view).lastLoadedUrl).isEqualTo("https://a.checkout.testurl?embed=branding%3Dapp")
        }
    }

    @Test
    fun `cacheableCheckoutView returns the same view if preloading enabled and cache is populated`() {
        withPreloadingEnabled {
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            val viewTwo = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()

            assertThat(viewOne).isEqualTo(viewTwo)
            assertThat(shadowOf(viewOne).lastLoadedUrl).isEqualTo("https://a.checkout.testurl?embed=branding%3Dapp")
            assertThat(shadowOf(viewTwo).lastLoadedUrl).isEqualTo("https://a.checkout.testurl?embed=branding%3Dapp")
        }
    }

    @Test
    fun `calls onPause if preloading so the PageVisibility API reports the correct value`() {
        withPreloadingEnabled {
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            assertThat(shadowOf(viewOne).wasOnPauseCalled()).isTrue()
        }
    }

    @Test
    fun `does not call onPause if not preloading`() {
        withPreloadingEnabled {
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, false)

            shadowOf(Looper.getMainLooper()).runToEndOfTasks()

            assertThat(shadowOf(viewOne).wasOnPauseCalled()).isFalse()
        }
    }

    @Test
    fun `cacheableCheckoutView returns the new view if URL has changed`() {
        withPreloadingEnabled {
            val newUrl = "https://another.checkout.url"
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            val viewTwo = CheckoutWebView.cacheableCheckoutView(newUrl, activity, true)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()

            assertThat(viewOne).isNotEqualTo(viewTwo)
            assertThat(shadowOf(viewOne).lastLoadedUrl).isEqualTo(
                "https://a.checkout.testurl?embed=branding%3Dapp"
            )
            assertThat(shadowOf(viewTwo).lastLoadedUrl).isEqualTo(
                "https://another.checkout.url?embed=branding%3Dapp"
            )
            assertThat(shadowOf(viewOne).wasDestroyCalled()).isTrue
            assertThat(shadowOf(viewTwo).wasDestroyCalled()).isFalse
        }
    }

    @Test
    fun `cacheableCheckoutView returns the a new view for each call if preloading disabled`() {
//        withSpecificColorScheme(ColorScheme.Automatic()) {
        val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
        val viewTwo = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        assertThat(viewOne).isNotEqualTo(viewTwo)
        assertThat(shadowOf(viewOne).lastLoadedUrl).isEqualTo(
            "https://a.checkout.testurl?embed=branding%3Dapp"
        )
        assertThat(shadowOf(viewTwo).lastLoadedUrl).isEqualTo(
            "https://a.checkout.testurl?embed=branding%3Dapp"
        )

        assertThat(shadowOf(viewOne).wasDestroyCalled()).isTrue
        assertThat(shadowOf(viewTwo).wasDestroyCalled()).isFalse
//        }
    }

    @Test
    fun `clear cache removes the view from the cache and destroys it`() {
        withPreloadingEnabled {
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            CheckoutWebView.clearCache()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            val viewTwo = CheckoutWebView.cacheableCheckoutView(URL, activity, true)

            assertThat(viewOne).isNotEqualTo(viewTwo)
            assertThat(shadowOf(viewOne).wasDestroyCalled()).isTrue
            assertThat(shadowOf(viewTwo).wasDestroyCalled()).isFalse
        }
    }

    @Test
    fun `clear cache does nothing if cache empty`() {
        withPreloadingEnabled {
            assertThatNoException().isThrownBy {
                CheckoutWebView.clearCache()
                shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            }
        }
    }

    @Test
    fun `markCacheEntryStale makes the cache entry stale but does not clear the cache or destroy the view`() {
        withPreloadingEnabled {
            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            CheckoutWebView.markCacheEntryStale()

            assertThat(CheckoutWebView).isNotNull()
            assertThat(CheckoutWebView.cacheEntry!!.isValid(URL)).isFalse()

            val viewTwo = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()

            assertThat(viewOne).isNotEqualTo(viewTwo)
            assertThat(shadowOf(viewOne).lastLoadedUrl).isEqualTo("https://a.checkout.testurl?embed=branding%3Dapp")
            assertThat(shadowOf(viewTwo).lastLoadedUrl).isEqualTo("https://a.checkout.testurl?embed=branding%3Dapp")
        }
    }

    @Test
    fun `web view cache should have a ttl of 5 minutes`() {
        withPreloadingEnabled {
            val now = System.currentTimeMillis()
            val mockCacheClock = mock<CheckoutWebView.CheckoutWebViewCacheClock>()
            whenever(mockCacheClock.currentTimeMillis())
                .thenReturn(now)
                .thenReturn(now.plus(2.minutes.inWholeMilliseconds))
                .thenReturn(now.plus(5.minutes.inWholeMilliseconds))

            CheckoutWebView.cacheClock = mockCacheClock

            val viewOne = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            val viewTwo = CheckoutWebView.cacheableCheckoutView(URL, activity, true)
            val viewThree = CheckoutWebView.cacheableCheckoutView(URL, activity, true)

            // only 2 minutes have passed, cache entry still valid
            assertThat(viewOne).isEqualTo(viewTwo)

            // 5 minutes have passed, cache entry now invalid
            assertThat(viewTwo).isNotEqualTo(viewThree)
        }
    }

    private fun eventProcessor(): CheckoutEventProcessor = NoopEventProcessor()

    companion object {
        private const val URL = "https://a.checkout.testurl"
    }
}

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
import org.junit.After
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
class CheckoutWebViewContainerTest {

    private val mockCacheClock = mock<CheckoutWebView.CheckoutWebViewCacheClock>()

    @Before
    fun setUp() {
        CheckoutWebView.cacheClock = mockCacheClock
    }

    @After
    fun tearDown() {
        CheckoutWebView.clearCache()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()
    }

    @Test
    fun `should destroy FallbackWebView when it is removed in all cases`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val activity = activityController.get()

                val container = CheckoutWebViewContainer(activity)
                val fallbackView = FallbackWebView(activity)
                val shadow = shadowOf(fallbackView)

                container.addView(fallbackView)
                assertThat(shadow.wasDestroyCalled()).isFalse()

                container.removeView(fallbackView)

                assertThat(shadow.wasDestroyCalled()).isTrue()
            }
        }
    }

    // cache entries are essentially immediately stale if preloading is disabled
    @Test
    fun `should destroy CheckoutWebView when retainCacheEntry is IF_NOT_STALE and preloading is disabled`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val activity = activityController.get()

            val container = CheckoutWebViewContainer(activity)
            val checkoutWebView = CheckoutWebView.cacheableCheckoutView("https://shopify.dev", activity)
            val shadow = shadowOf(checkoutWebView)

            container.addView(checkoutWebView)
            assertThat(shadow.wasDestroyCalled()).isFalse()

            CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
            container.removeView(checkoutWebView)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()

            assertThat(shadow.wasDestroyCalled()).isTrue()
        }
    }

    @Test
    fun `should destroy CheckoutWebView when retainCacheEntry is IF_NOT_STALE and entry is stale`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                whenever(mockCacheClock.currentTimeMillis()).thenReturn(System.currentTimeMillis())

                val activity = activityController.get()

                val container = CheckoutWebViewContainer(activity)
                val checkoutWebView = CheckoutWebView.cacheableCheckoutView("https://shopify.dev", activity, true)
                val shadow = shadowOf(checkoutWebView)

                container.addView(checkoutWebView)
                assertThat(shadow.wasDestroyCalled()).isFalse()

                CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
                makeCacheEntryStale()

                container.removeView(checkoutWebView)
                shadowOf(Looper.getMainLooper()).runToEndOfTasks()

                assertThat(shadow.wasDestroyCalled()).isTrue()
            }
        }
    }

    @Test
    fun `should not destroy non-stale CheckoutWebView when retainCacheEntry == IF_NOT_STALE and entry is not stale`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                val activity = activityController.get()

                val container = CheckoutWebViewContainer(activity)
                val checkoutWebView = CheckoutWebView.cacheableCheckoutView("https://shopify.dev", activity, true)
                val shadow = shadowOf(checkoutWebView)

                container.addView(checkoutWebView)
                assertThat(shadow.wasDestroyCalled()).isFalse()

                CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
                container.removeView(checkoutWebView)
                shadowOf(Looper.getMainLooper()).runToEndOfTasks()

                assertThat(shadow.wasDestroyCalled()).isFalse()
            }
        }
    }

    @Test
    fun `should not destroy CheckoutWebView when retainCacheEntry == YES and entry is stale`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                whenever(mockCacheClock.currentTimeMillis()).thenReturn(System.currentTimeMillis())

                val activity = activityController.get()

                val container = CheckoutWebViewContainer(activity)
                val checkoutWebView = CheckoutWebView.cacheableCheckoutView("https://shopify.dev", activity, true)
                val shadow = shadowOf(checkoutWebView)

                container.addView(checkoutWebView)
                assertThat(shadow.wasDestroyCalled()).isFalse()

                CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.YES
                makeCacheEntryStale()

                container.removeView(checkoutWebView)
                shadowOf(Looper.getMainLooper()).runToEndOfTasks()
                assertThat(shadow.wasDestroyCalled()).isFalse()
                assertThat(CheckoutWebViewContainer.retainCacheEntry).isEqualTo(RetainCacheEntry.IF_NOT_STALE)
            }
        }
    }

    @Test
    fun `should not destroy CheckoutWebView when retainCacheEntry == YES and entry is not stale`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            withPreloadingEnabled {
                whenever(mockCacheClock.currentTimeMillis()).thenReturn(System.currentTimeMillis())

                val activity = activityController.get()

                val container = CheckoutWebViewContainer(activity)
                val checkoutWebView = CheckoutWebView.cacheableCheckoutView("https://shopify.dev", activity, true)
                val shadow = shadowOf(checkoutWebView)

                container.addView(checkoutWebView)
                assertThat(shadow.wasDestroyCalled()).isFalse()

                CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.YES

                container.removeView(checkoutWebView)
                shadowOf(Looper.getMainLooper()).runToEndOfTasks()
                assertThat(shadow.wasDestroyCalled()).isFalse()
                assertThat(CheckoutWebViewContainer.retainCacheEntry).isEqualTo(RetainCacheEntry.IF_NOT_STALE)
            }
        }
    }

    private fun makeCacheEntryStale() {
        val initialTime = mockCacheClock.currentTimeMillis()
        whenever(mockCacheClock.currentTimeMillis()).thenReturn(initialTime.plus(60 * 10 * 1000))
    }
}

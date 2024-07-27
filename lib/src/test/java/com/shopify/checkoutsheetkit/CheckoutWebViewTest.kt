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

import android.Manifest
import android.graphics.Color
import android.os.Looper
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.PermissionRequest
import androidx.activity.ComponentActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.contains
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewTest {

    private lateinit var activity: ComponentActivity

    @Before
    fun setUp() {
        CheckoutWebView.clearCache()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configuration.platform = null
    }

    @Test
    fun `configures web view on initialization`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.visibility).isEqualTo(VISIBLE)
        assertThat(view.settings.javaScriptEnabled).isTrue
        assertThat(view.settings.domStorageEnabled).isTrue
        assertThat(view.layoutParams.height).isEqualTo(MATCH_PARENT)
        assertThat(view.layoutParams.width).isEqualTo(MATCH_PARENT)
        assertThat(view.id).isNotNull
        assertThat(shadowOf(view).webViewClient.javaClass).isEqualTo(CheckoutWebView.CheckoutWebViewClient::class.java)
        assertThat(shadowOf(view).backgroundColor).isEqualTo(Color.TRANSPARENT)
        assertThat(shadowOf(view).getJavascriptInterface("android").javaClass)
            .isEqualTo(CheckoutBridge::class.java)
    }

    @Test
    fun `user agent suffix includes ShopifyCheckoutSDK and version number`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Dark()
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).contains("ShopifyCheckoutSDK/3.0.2 ")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - dark`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Dark()
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).endsWith("(8.1;dark;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - light`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Light()
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).endsWith("(8.1;light;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - web`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Web()
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).endsWith("(8.1;web_default;standard)")
    }

    @Test
    fun `user agent suffix includes metadata for the schema version, theme, and variant - automatic`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Automatic()
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).endsWith("(8.1;automatic;standard)")
    }

    @Test
    fun `user agent suffix includes platform if specified`() {
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Automatic()
        ShopifyCheckoutSheetKit.configuration.platform = Platform.REACT_NATIVE
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.settings.userAgentString).endsWith("(8.1;automatic;standard) ReactNative")
    }

    @Test
    fun `sends prefetch header for preloads`() {
        withPreloadingEnabled {
            val isPreload = true
            val view = CheckoutWebView.cacheableCheckoutView(URL, activity, isPreload)

            val shadow = shadowOf(view)
            ShadowLooper.shadowMainLooper().runToEndOfTasks()

            assertThat(shadow.lastAdditionalHttpHeaders.getOrDefault("Sec-Purpose", "")).isEqualTo("prefetch")
        }
    }

    @Test
    fun `does not send prefetch header for preloads`() {
        val isPreload = false
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity, isPreload)

        val shadow = shadowOf(view)
        ShadowLooper.shadowMainLooper().runToEndOfTasks()

        assertThat(shadow.lastAdditionalHttpHeaders.getOrDefault("Sec-Purpose", "")).isEqualTo("")
    }

    @Test
    fun `attaches javascript interface onAttachedToWindow`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        val shadow = shadowOf(view)
        shadow.callOnAttachedToWindow()

        assertThat(shadow.getJavascriptInterface("android").javaClass)
            .isEqualTo(CheckoutBridge::class.java)
    }

    @Test
    fun `removes javascript interface onDetachedFromWindow`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        val shadow = shadowOf(view)
        shadow.callOnDetachedFromWindow()

        assertThat(shadow.getJavascriptInterface("android")).isNull()
    }

    @Test
    fun `sends presented message each time when view is loaded if it has been presented`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        val shadow = shadowOf(view)
        shadow.webViewClient.onPageFinished(view, "https://anything")

        val spy = spy(view)
        spy.notifyPresented()

        verify(spy).evaluateJavascript(
            contains("window.MobileCheckoutSdk.dispatchMessage('presented');"),
            eq(null)
        )
    }

    @Test
    fun `calls update progress when new progress is reported by WebChromeClient`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)
        val webViewEventProcessor = mock<CheckoutWebViewEventProcessor>()
        view.setEventProcessor(webViewEventProcessor)

        val shadow = shadowOf(view)

        shadow.webChromeClient?.onProgressChanged(view, 20)
        verify(webViewEventProcessor).updateProgressBar(20)

        shadow.webChromeClient?.onProgressChanged(view, 50)
        verify(webViewEventProcessor).updateProgressBar(50)
    }

    @Test
    fun `calls processors onPermissionRequest when resource permission requested`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)
        val webViewEventProcessor = mock<CheckoutWebViewEventProcessor>()
        view.setEventProcessor(webViewEventProcessor)

        val permissionRequest = mock<PermissionRequest>()
        val requestedResources = arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        whenever(permissionRequest.resources).thenReturn(requestedResources)

        val shadow = shadowOf(view)
        shadow.webChromeClient?.onPermissionRequest(permissionRequest)

        verify(webViewEventProcessor).onPermissionRequest(permissionRequest)
    }

    @Test
    fun `should recover from errors`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = CheckoutWebView(activityController.get())
            assertThat(view.recoverErrors).isTrue()
        }
    }

    companion object {
        private const val URL = "https://a.checkout.testurl"
    }
}

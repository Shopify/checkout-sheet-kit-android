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
import android.os.Looper
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewTest {

    private lateinit var activity: ComponentActivity

    @Before
    fun setUp() {
        CheckoutWebView.clearCache()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
    }

    @Test
    fun `configures web view on initialization`() {
        val view = CheckoutWebView.cacheableCheckoutView(URL, activity)

        assertThat(view.visibility).isEqualTo(VISIBLE)
        assertThat(view.settings.javaScriptEnabled).isTrue
        assertThat(view.settings.domStorageEnabled).isTrue
        assertThat(view.settings.userAgentString).contains("ShopifyCheckoutSDK")
        assertThat(view.layoutParams.height).isEqualTo(MATCH_PARENT)
        assertThat(view.layoutParams.width).isEqualTo(MATCH_PARENT)
        assertThat(view.id).isNotNull
        assertThat(shadowOf(view).webViewClient.javaClass).isEqualTo(CheckoutWebView.CheckoutWebViewClient::class.java)
        assertThat(shadowOf(view).backgroundColor).isEqualTo(Color.TRANSPARENT)
        assertThat(shadowOf(view).getJavascriptInterface("android").javaClass)
            .isEqualTo(CheckoutBridge::class.java)
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


    companion object {
        private const val URL = "https://a.checkout.testurl"
    }
}

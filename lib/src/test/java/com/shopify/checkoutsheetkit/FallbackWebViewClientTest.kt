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
import com.shopify.checkoutsheetkit.lifecycleevents.emptyCompletedEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FallbackWebViewClientTest {

    @Test
    fun `should call onCheckoutCompleted in onPageFinished if url looks like a typ - hyphen`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val mockProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(mockProcessor)

            val client = view.FallbackWebViewClient()
            client.onPageFinished(view, "https://abc.com/cn-12345678/thank-you?a=b")

            verify(mockProcessor).onCheckoutViewComplete(emptyCompletedEvent())
        }
    }

    @Test
    fun `should call onCheckoutCompleted in onPageFinished if url looks like a typ - underscore`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val mockProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(mockProcessor)

            val client = view.FallbackWebViewClient()
            client.onPageFinished(view, "https://abc.com/cn-12345678/thank_you")

            verify(mockProcessor).onCheckoutViewComplete(emptyCompletedEvent())
        }
    }

    @Test
    fun `should call onCheckoutCompleted in onPageFinished if url looks like a typ - mixed case`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val mockProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(mockProcessor)

            val client = view.FallbackWebViewClient()
            client.onPageFinished(view, "https://abc.com/cn-12345678/tHAnk_you")

            verify(mockProcessor).onCheckoutViewComplete(emptyCompletedEvent())
        }
    }

    @Test
    fun `should call onCheckoutCompleted with order id in onPageFinished if url looks like a typ and query param present`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val mockProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(mockProcessor)

            val client = view.FallbackWebViewClient()
            client.onPageFinished(view, "https://abc.com/cn-12345678/thank-you?order_id=123")

            verify(mockProcessor).onCheckoutViewComplete(emptyCompletedEvent(id = "123"))
        }
    }

    @Test
    fun `should not call onCheckoutCompleted in onPageFinished if url does not look like a typ`() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activityController ->
            val view = FallbackWebView(activityController.get())
            val mockProcessor = mock<CheckoutWebViewEventProcessor>()
            view.setEventProcessor(mockProcessor)

            val client = view.FallbackWebViewClient()
            client.onPageFinished(view, "https://abc.com/cn-12345678/processing?a=b")

            verify(mockProcessor, never()).onCheckoutViewComplete(any())
        }
    }

}

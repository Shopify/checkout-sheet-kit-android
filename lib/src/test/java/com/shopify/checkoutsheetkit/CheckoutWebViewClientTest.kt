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
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.activity.ComponentActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewClientTest {

    private lateinit var activity: ComponentActivity
    private val mockEventProcessor = mock<CheckoutEventProcessor>()
    private val checkoutWebViewEventProcessor = spy(
        CheckoutWebViewEventProcessor(
            mockEventProcessor
        )
    )

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
    }

    @Test
    fun `overrides url loading to call event processor for mailto links`() {
        val loadedUri = Uri.parse("mailto:daniel.kift@shopify.com")
        val mockRequest = mockWebRequest(loadedUri)

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(loadedUri)
    }

    @Test
    fun `overrides url loading to call event processor for tel links`() {
        val loadedUri = Uri.parse("tel:0123456789")
        val mockRequest = mockWebRequest(loadedUri)

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(loadedUri)
    }

    @Test
    fun `does not override url loading to call event processor for web links`() {
        val loadedUri = Uri.parse("https://checkout-sdk.myshopify.com")
        val mockRequest = mockWebRequest(loadedUri)

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isFalse
        verify(mockEventProcessor, never()).onCheckoutLinkClicked(loadedUri)
    }

    @Test
    fun `should call event processor and clear cache on web resource load error for main frame`() {
        val loadedUri = Uri.parse("https://checkout-sdk.myshopify.com")
        val mockRequest = mockWebRequest(loadedUri, true)
        val mockError = mockWebResourceError()

        val view = viewWithProcessor(activity)
        CheckoutWebView.cacheEntry = view.toCacheEntry(loadedUri.toString())
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onReceivedError(view, mockRequest, mockError)
        ShadowLooper.shadowMainLooper().runToEndOfTasks()

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue).isInstanceOf(CheckoutExpiredException::class.java)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame`() {
        val loadedUri = Uri.parse("https://checkout-sdk.myshopify.com")
        val mockRequest = mockWebRequest(loadedUri, true)
        val checkoutExpiredResponse = mockWebResourceResponse()

        val view = viewWithProcessor(activity)
        CheckoutWebView.cacheEntry = view.toCacheEntry(loadedUri.toString())
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onReceivedHttpError(view, mockRequest, checkoutExpiredResponse)
        ShadowLooper.shadowMainLooper().runToEndOfTasks()

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue).isInstanceOf(CheckoutExpiredException::class.java)
    }

    @Test
    fun `links with open_externally are delegated to the contact link function`() {
        val host = "https://go.shop.com"
        val loadedUri = Uri.parse("$host?open_externally=true&random_param=1")
        val mockRequest = mockWebRequest(loadedUri)

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(Uri.parse("$host?random_param=1"))
    }

    @Test
    fun `onPageFinished calls delegate to remove loading spinner`() {
        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onPageFinished(view, "https://anything")

        verify(checkoutWebViewEventProcessor).onCheckoutViewLoadComplete()
    }

    @Test
    fun `onRenderProcessGone should return false if sdk version is too low to check detail#didCrash()`() {
        val view = viewWithProcessor(activity).withParent()
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(false)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isFalse
        assertThat(view.parent).isNotNull
    }

    @Config(sdk = [26])
    @Test
    fun `onRenderProcessGone should do nothing if the renderer crashed`() {
        val view = viewWithProcessor(activity).withParent()
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(true)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isFalse
        assertThat(view.parent).isNotNull
    }

    @Config(sdk = [26])
    @Test
    fun `onRenderProcessGone should remove the view from its parent if the render process crashed due to low memory`() {
        val view = viewWithProcessor(activity).withParent()
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(false)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isTrue
        assertThat(view.parent).isNull()
    }

    private fun mockWebRequest(uri: Uri, forMainFrame: Boolean = false): WebResourceRequest {
        val mockRequest = mock<WebResourceRequest>()
        whenever(mockRequest.url).thenReturn(uri)
        whenever(mockRequest.isForMainFrame).thenReturn(forMainFrame)
        return mockRequest
    }

    private fun viewWithProcessor(
        activity: ComponentActivity,
    ): CheckoutWebView {
        val view = CheckoutWebView(activity)
        view.setEventProcessor(checkoutWebViewEventProcessor)
        return view
    }

    private fun mockWebResourceError(
        status: Int = 410,
        description: String = "Checkout expired"
    ): WebResourceError {
        val mock = mock<WebResourceError>()
        whenever(mock.errorCode).thenReturn(status)
        whenever(mock.description).thenReturn(description)
        return mock
    }

    private fun mockWebResourceResponse(
        status: Int = 410,
        description: String = "Checkout expired"
    ): WebResourceResponse {
        val mock = mock<WebResourceResponse>()
        whenever(mock.statusCode).thenReturn(status)
        whenever(mock.reasonPhrase).thenReturn(description)
        return mock
    }

    private fun CheckoutWebView.withParent(): CheckoutWebView {
        val container = CheckoutWebViewContainer(activity)
        container.addView(this)
        return this
    }

    private fun CheckoutWebView.toCacheEntry(key: String): CheckoutWebView.CheckoutWebViewCacheEntry {
        return CheckoutWebView.CheckoutWebViewCacheEntry(
            key = key,
            view = this,
            clock = CheckoutWebView.CheckoutWebViewCacheClock(),
            timeout = 5.minutes.inWholeMilliseconds,
        )
    }
}

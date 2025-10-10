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
import android.webkit.WebViewClient.ERROR_BAD_URL
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
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
import java.net.HttpURLConnection
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewClientTest {

    private lateinit var activity: ComponentActivity
    private val mockEventProcessor = mock<CheckoutEventProcessor>()
    private val checkoutWebViewEventProcessor = spy(CheckoutWebViewEventProcessor(mockEventProcessor))

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
    }

    @Test
    fun `overrides url loading to call event processor for mailto links`() {
        val mockRequest = mockWebRequest(Uri.parse("mailto:daniel.kift@shopify.com"))

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(mockRequest.url)
    }

    @Test
    fun `overrides url loading to call event processor for tel links`() {
        val mockRequest = mockWebRequest(Uri.parse("tel:0123456789"))

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(mockRequest.url)
    }

    @Test
    fun `overrides url loading to call event processor for deep links`() {
        val mockRequest = mockWebRequest(Uri.parse("geo:40.712776,-74.005974?q=Statue+of+Liberty"))

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isTrue
        verify(mockEventProcessor).onCheckoutLinkClicked(mockRequest.url)
    }

    @Test
    fun `does not override url loading to call event processor for about blank`() {
        val mockRequest = mockWebRequest(Uri.parse("about:blank"))

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isFalse()
        verify(mockEventProcessor, never()).onCheckoutLinkClicked(any())
    }

    @Test
    fun `does not override url loading to call event processor for web links`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"))

        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val overridden = webViewClient.shouldOverrideUrlLoading(view, mockRequest)

        assertThat(overridden).isFalse
        verify(mockEventProcessor, never()).onCheckoutLinkClicked(mockRequest.url)
    }

    @Test
    fun `should call event processor and clear cache on web resource load error for main frame`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceError()

        val view = viewWithProcessor(activity)
        CheckoutWebView.cacheEntry = view.toCacheEntry(mockRequest.url.toString())
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onReceivedError(view, mockRequest, mockResponse)
        ShadowLooper.shadowMainLooper().runToEndOfTasks()

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutExpiredException::class.java)
            .isNotRecoverable()
            .hasErrorCode(CheckoutExpiredException.CART_EXPIRED)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - 410`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_GONE
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutExpiredException::class.java)
            .isNotRecoverable()
            .hasErrorCode(CheckoutExpiredException.CART_EXPIRED)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - 404`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_NOT_FOUND,
            description = "Not Found",
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(HttpException::class.java)
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
            .isNotRecoverable()
            .hasDescription("Not Found")
            .hasStatusCode(404)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - 500`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_INTERNAL_ERROR
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutUnavailableException::class.java)
            .isRecoverable()
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - 504`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_GATEWAY_TIMEOUT
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutUnavailableException::class.java)
            .isRecoverable()
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - 502`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_BAD_GATEWAY
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutUnavailableException::class.java)
            .isRecoverable()
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - bad url`() {
        val loadedUri = Uri.parse("https://checkout-sdk.myshopify.com")
        val mockRequest = mockWebRequest(loadedUri, true)
        val mockResponse = mockWebResourceResponse(
            status = ERROR_BAD_URL,
            description = "Bad url"
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutUnavailableException::class.java)
            .isNotRecoverable()
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
            .hasDescription("Bad url")
    }

    @Test
    fun `should call event processor calls onCheckoutViewFailedWithError on http error for main frame - other`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_BAD_REQUEST,
            description = "Bad request"
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isNotRecoverable()
            .isInstanceOf(CheckoutUnavailableException::class.java)
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
            .hasDescription("Bad request")
    }

    @Test
    fun `should call event processor with default description including the status code if the reason phrase is blank`() {
        val mockRequest = mockWebRequest(Uri.parse("https://checkout-sdk.myshopify.com"), true)
        val mockResponse = mockWebResourceResponse(
            status = HttpURLConnection.HTTP_BAD_GATEWAY,
            description = ""
        )

        triggerOnReceivedHttpError(mockRequest, mockResponse)

        val captor = argumentCaptor<CheckoutException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isRecoverable()
            .isInstanceOf(HttpException::class.java)
            .hasErrorCode(CheckoutUnavailableException.HTTP_ERROR)
            .hasDescription("HTTP 502 Error")
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
    fun `onPageFinished calls delegate to remove progress indicator`() {
        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onPageFinished(view, "https://anything")

        verify(checkoutWebViewEventProcessor).onCheckoutViewLoadComplete()
    }

    @Test
    fun `onRenderProcessGone should return false if sdk version is too low to check detail#didCrash()`() {
        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(false)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isFalse
        verify(checkoutWebViewEventProcessor, never()).onCheckoutViewFailedWithError(any())
    }

    @Config(sdk = [26])
    @Test
    fun `onRenderProcessGone should do nothing if the renderer crashed`() {
        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(true)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isFalse
        verify(checkoutWebViewEventProcessor, never()).onCheckoutViewFailedWithError(any())
    }

    @Config(sdk = [26])
    @Test
    fun `onRenderProcessGone should call onCheckoutFailed if the render process crashed due to low memory`() {
        val view = viewWithProcessor(activity)
        val webViewClient = view.CheckoutWebViewClient()
        val detail = mock<RenderProcessGoneDetail>()
        whenever(detail.didCrash()).thenReturn(false)

        val result = webViewClient.onRenderProcessGone(view, detail)

        assertThat(result).isTrue
        val captor = argumentCaptor<CheckoutSheetKitException>()
        verify(checkoutWebViewEventProcessor).onCheckoutViewFailedWithError(captor.capture())
        assertThat(captor.firstValue)
            .isInstanceOf(CheckoutSheetKitException::class.java)
            .hasDescription("Render process gone.")
            .hasErrorCode(CheckoutSheetKitException.RENDER_PROCESS_GONE)
    }

    private fun triggerOnReceivedHttpError(mockRequest: WebResourceRequest, checkoutExpiredResponse: WebResourceResponse) {
        val view = viewWithProcessor(activity)
        CheckoutWebView.cacheEntry = view.toCacheEntry(mockRequest.url.toString())
        val webViewClient = view.CheckoutWebViewClient()

        webViewClient.onReceivedHttpError(view, mockRequest, checkoutExpiredResponse)
        ShadowLooper.shadowMainLooper().runToEndOfTasks()
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
        description: String = "Checkout expired",
        headers: MutableMap<String, String> = mutableMapOf()
    ): WebResourceResponse {
        val mock = mock<WebResourceResponse>()
        whenever(mock.statusCode).thenReturn(status)
        whenever(mock.reasonPhrase).thenReturn(description)
        whenever(mock.responseHeaders).thenReturn(headers)
        return mock
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

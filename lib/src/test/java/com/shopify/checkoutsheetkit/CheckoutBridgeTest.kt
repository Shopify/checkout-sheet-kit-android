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

import android.webkit.WebView
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.COMPLETED
import com.shopify.checkoutsheetkit.CheckoutBridge.CheckoutWebOperation.MODAL
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutBridgeTest {

    private var mockEventProcessor = mock<CheckoutWebViewEventProcessor>()
    private lateinit var checkoutBridge: CheckoutBridge

    @Before
    fun init() {
        checkoutBridge = CheckoutBridge(mockEventProcessor)
    }

    @Test
    fun `postMessage calls web event processor onCheckoutViewComplete when completed message received`() {
        checkoutBridge.postMessage(Json.encodeToString(WebToSdkEvent(COMPLETED.key)))
        verify(mockEventProcessor).onCheckoutViewComplete(any())
    }

    @Test
    fun `postMessage calls web event processor onCheckoutModalToggled when modal message received - false`() {
        checkoutBridge.postMessage(
            Json.encodeToString(
                WebToSdkEvent(
                    MODAL.key,
                    "false"
                )
            )
        )
        verify(mockEventProcessor).onCheckoutViewModalToggled(false)
    }

    @Test
    fun `postMessage calls web event processor onCheckoutModalToggled when modal message received - true`() {
        checkoutBridge.postMessage(
            Json.encodeToString(
                WebToSdkEvent(
                    MODAL.key,
                    "true"
                )
            )
        )
        verify(mockEventProcessor).onCheckoutViewModalToggled(true)
    }

    @Test
    fun `postMessage does not issue a msg to the event processor when unsupported message received`() {
        checkoutBridge.postMessage(Json.encodeToString(WebToSdkEvent("boom")))
        verifyNoInteractions(mockEventProcessor)
    }

    @Test
    fun `sendMessage evaluates javascript on the provided WebView`() {
        val webView = mock<WebView>()
        checkoutBridge.sendMessage(webView, CheckoutBridge.SDKOperation.Presented)

        verify(webView).evaluateJavascript("""|
        |if (window.MobileCheckoutSdk && window.MobileCheckoutSdk.dispatchMessage) {
        |    window.MobileCheckoutSdk.dispatchMessage('presented');
        |} else {
        |    window.addEventListener('mobileCheckoutBridgeReady', function () {
        |        window.MobileCheckoutSdk.dispatchMessage('presented');
        |    }, {passive: true, once: true});
        |}
        |""".trimMargin(), null)
    }

    @Test
    fun `sendMessage returns error if evaluating javascript fails`() {
        val webView = mock<WebView>()
        whenever(webView.evaluateJavascript(any(), eq(null))).thenThrow(RuntimeException("something went wrong"))

        checkoutBridge.sendMessage(webView, CheckoutBridge.SDKOperation.Presented)

        val errorCaptor = argumentCaptor<CheckoutSheetKitException>()
        verify(mockEventProcessor).onCheckoutViewFailedWithError(errorCaptor.capture())

        val error = errorCaptor.firstValue
        assertThat(error.message).isEqualTo(
            "Failed to send 'presented' message to checkout, some features may not work."
        )
        assertThat(error.isRecoverable).isTrue()
        assertThat(error.errorCode).isEqualTo(CheckoutSheetKitException.ERROR_SENDING_MESSAGE_TO_CHECKOUT)
    }

    @Test
    fun `instrumentation sends message to the bridge`() {
        val webView = mock<WebView>()
        val payload = InstrumentationPayload(
            name = "Test",
            value = 123L,
            type = InstrumentationType.histogram,
            tags = mapOf("tag1" to "value1", "tag2" to "value2")
        )
        val expectedPayload = """{"detail":{"name":"Test","value":123,"type":"histogram","tags":{"tag1":"value1","tag2":"value2"}}}"""
        val expectedJavascript = """|
        |if (window.MobileCheckoutSdk && window.MobileCheckoutSdk.dispatchMessage) {
        |    window.MobileCheckoutSdk.dispatchMessage('instrumentation', $expectedPayload);
        |} else {
        |    window.addEventListener('mobileCheckoutBridgeReady', function () {
        |        window.MobileCheckoutSdk.dispatchMessage('instrumentation', $expectedPayload);
        |    }, {passive: true, once: true});
        |}
        |""".trimMargin()

        checkoutBridge.sendMessage(webView, CheckoutBridge.SDKOperation.Instrumentation(payload))

        Mockito.verify(webView).evaluateJavascript(expectedJavascript, null)
    }

    @Test
    fun `calls onPixelEvent when valid webPixels event received`() {
        val eventString = """|
            |{
            |   "name":"webPixels",
            |   "body": "{
            |       \"name\": \"checkout_started\",
            |       \"event\": {
            |           \"type\": \"standard\",
            |           \"id\": \"sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B\",
            |           \"name\": \"checkout_started\",
            |           \"timestamp\": \"2023-12-20T16:39:23+0000\",
            |           \"data\": {
            |               \"checkout\": {
            |                   \"order\": {
            |                       \"id\": \"123\"
            |                   }
            |               }
            |           }
            |       }
            |   }"
            |}
        |""".trimMargin()


       checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<PixelEvent>()
        verify(mockEventProcessor, timeout(2000).times(1)).onWebPixelEvent(captor.capture())

        assertThat(captor.firstValue).isInstanceOf(CheckoutStartedPixelEvent::class.java)
    }

    @Test
    fun `should decode a checkout expired error payload and call processor#onCheckoutViewFailedWithError - invalid`() {
        val eventString = """|
            |{
            |   "name":"error",
            |   "body": "[{
            |       \"group\": \"expired\",
            |       \"reason\": \"Cart is invalid\",
            |       \"flowType\": \"regular\",
            |       \"code\": \"invalid_cart\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor, timeout(2000).times(1)).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(error.message).isEqualTo("Cart is invalid")
        assertThat(error.isRecoverable).isFalse()
        assertThat(error.errorCode).isEqualTo(CheckoutExpiredException.INVALID_CART)
    }

    @Test
    fun `should decode a checkout expired error payload and call processor#onCheckoutViewFailedWithError - completed`() {
        val eventString = """|
            |{
            |   "name":"error",
            |   "body": "[{
            |       \"group\": \"expired\",
            |       \"reason\": \"Checkout has been completed\",
            |       \"flowType\": \"regular\",
            |       \"code\": \"cart_completed\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor, timeout(2000).times(1)).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(error.message).isEqualTo("Checkout has been completed")
        assertThat(error.isRecoverable).isFalse()
        assertThat(error.errorCode).isEqualTo(CheckoutExpiredException.CART_COMPLETED)
    }


    @Test
    fun `should decode a barebones expired error payload and call processor#onCheckoutViewFailedWithError`() {
        val eventString =  """|
            |{
            |   "name": "error",
            |   "body": "[{
            |       \"group\": \"expired\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor, timeout(2000).times(1)).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(error.message).isEqualTo(
            "Checkout is no longer available with the provided token. Please generate a new checkout URL"
        )
        assertThat(error.isRecoverable).isFalse()
        assertThat(error.errorCode).isEqualTo(CheckoutExpiredException.CART_EXPIRED)
    }

    @Test
    fun `should decode an unrecoverable error payload and call processor#onCheckoutViewFailedWithError`() {
        val eventString = """|
            |{
            |   "name":"error",
            |   "body": "[{
            |       \"group\": \"unrecoverable\",
            |       \"reason\": \"Checkout crashed\",
            |       \"code\": \"sdk_not_enabled\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor, timeout(2000).times(1)).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(CheckoutUnavailableException::class.java)
        assertThat(error.message).isEqualTo("Checkout crashed")
        assertThat(error.isRecoverable).isTrue()
        assertThat(error.errorCode).isEqualTo(CheckoutUnavailableException.CLIENT_ERROR)
    }
    @Test
    fun `should decode a configuration error payload and call processor#onCheckoutViewFailedWithError - storefront pw required`() {
        val eventString = """|
            |{
            |   "name":"error",
            |   "body": "[{
            |       \"group\": \"configuration\",
            |       \"reason\": \"Storefront password required\",
            |       \"code\": \"storefront_password_required\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor, timeout(2000).times(1)).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(ConfigurationException::class.java)
        assertThat(error.message).isEqualTo("Storefront password required")
        assertThat(error.isRecoverable).isFalse()
        assertThat(error.errorCode).isEqualTo(ConfigurationException.STOREFRONT_PASSWORD_REQUIRED)
    }

    @Test
    fun `should ignore unsupported error payloads`() {
        val eventString = """|
            |{
            |   "name":"error",
            |   "body": "[{
            |       \"group\": \"authentication\",
            |       \"reason\": \"invalid signature\",
            |       \"code\": \"invalid_signature\"
            |   }]"
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        verifyNoInteractions(mockEventProcessor)
    }

    @Test
    fun `should call onCheckoutViewFailedWithError if message cannot be decoded`() {
        val eventString = """|
            |{
            |   "name":"error
            |}
        |""".trimMargin()

        checkoutBridge.postMessage(eventString)

        val captor = argumentCaptor<CheckoutException>()
        verify(mockEventProcessor).onCheckoutViewFailedWithError(captor.capture())

        val error = captor.firstValue
        assertThat(error).isInstanceOf(CheckoutSheetKitException::class.java)
        assertThat(error.message).isEqualTo("Error decoding message from checkout.")
        assertThat(error.isRecoverable).isTrue()
        assertThat(error.errorCode).isEqualTo(CheckoutSheetKitException.ERROR_RECEIVING_MESSAGE_FROM_CHECKOUT)
    }
}

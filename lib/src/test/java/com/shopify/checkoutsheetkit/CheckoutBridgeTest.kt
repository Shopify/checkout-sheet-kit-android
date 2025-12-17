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

import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import com.shopify.checkoutsheetkit.lifecycleevents.CartAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CartCredential
import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CartPayment
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrument
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentMethod
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.RemoteTokenPaymentCredential
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class CheckoutBridgeTest {

    private val mockEventProcessor = mock<CheckoutWebViewEventProcessor>()
    private lateinit var checkoutBridge: CheckoutBridge

    @Before
    fun setUp() {
        CheckoutWebView.cacheEntry = null
        checkoutBridge = CheckoutBridge(mockEventProcessor)
    }

    @After
    fun tearDown() {
        CheckoutWebView.cacheEntry = null
    }

    @Test
    fun `postMessage dispatches address change start to event processor`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-1",
            "method":"checkout.addressChangeStart",
            "params":{
                "addressType":"shipping",
                "cart":${Json.encodeToString(cart)}
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStartEvent>()
        verify(mockEventProcessor).onCheckoutViewAddressChangeStart(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.addressType).isEqualTo("shipping")
        assertThat(event.cart.id).isEqualTo(cart.id)
    }

    @Test
    fun `postMessage dispatches submit start to event processor`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-submit",
            "method":"checkout.submitStart",
            "params":{
                "cart":${Json.encodeToString(cart)},
                "sessionId":"checkout-session-123"
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutSubmitStartEvent>()
        verify(mockEventProcessor).onCheckoutViewSubmitStart(eventCaptor.capture())
        val event = eventCaptor.firstValue
        assertThat(event.cart.id).isEqualTo(cart.id)
        assertThat(event.sessionId).isEqualTo("checkout-session-123")
    }

    @Test
    fun `submitStart respondWith posts payment token to window`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-submit-response",
            "method":"checkout.submitStart",
            "params":{
                "cart":${Json.encodeToString(cart)},
                "sessionId":"checkout-session-456"
            }
        }""".trimIndent()

        val mockWebView = mock<CheckoutWebView>()
        checkoutBridge.setWebView(mockWebView)
        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutSubmitStartEvent>()
        verify(mockEventProcessor).onCheckoutViewSubmitStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val updatedCart = event.cart.copy(
            payment = CartPayment(
                methods = listOf(
                    CartPaymentMethod(
                        instruments = listOf(
                            CartPaymentInstrument(
                                externalReferenceId = "payment-123",
                                credentials = listOf(
                                    CartCredential(
                                        remoteTokenPaymentCredential = RemoteTokenPaymentCredential(
                                            token = "tok_test_123",
                                            tokenType = "card",
                                            tokenHandler = "delegated"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
        )
        val payload = CheckoutSubmitStartResponsePayload(cart = updatedCart)

        event.respondWith(payload)
        ShadowLooper.runUiThreadTasks()

        val scriptCaptor = argumentCaptor<String>()
        verify(mockWebView).evaluateJavascript(scriptCaptor.capture(), anyOrNull())
        assertThat(scriptCaptor.firstValue)
            .contains("window.postMessage")
            .contains("\"id\":\"request-id-submit-response\"")
            .contains("\"result\":")
            .contains("\"token\":\"tok_test_123\"")
    }

    @Test
    fun `postMessage dispatches payment method change start to event processor`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"payment-request-1",
            "method":"checkout.paymentMethodChangeStart",
            "params":{
                "cart":${Json.encodeToString(cart)}
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutPaymentMethodChangeStartEvent>()
        verify(mockEventProcessor).onCheckoutViewPaymentMethodChangeStart(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.cart.id).isEqualTo(cart.id)
    }

    @Test
    fun `postMessage ignores invalid JSON`() {
        val invalidJson = "{ this is not valid json }"

        checkoutBridge.postMessage(invalidJson)

        verifyNoInteractions(mockEventProcessor)
    }

    @Test
    fun `postMessage ignores unsupported JSON-RPC methods`() {
        val unsupportedMethod = """{
            "jsonrpc":"2.0",
            "method":"checkout.unsupported",
            "params":{}
        }""".trimIndent()

        checkoutBridge.postMessage(unsupportedMethod)

        verifyNoInteractions(mockEventProcessor)
    }

    @Test
    fun `postMessage dispatches checkout complete to event processor`() {
        val params = createTestCheckoutCompleteEvent(
            cart = createTestCart(subtotalAmount = "0.00", totalAmount = "0.00")
        )

        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "method":"checkout.complete",
            "params":${Json.encodeToString(params)}
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        verify(mockEventProcessor).onCheckoutViewComplete(any())
    }

    @Test
    fun `postMessage dispatches checkout start to event processor`() {
        val params = createTestCheckoutStartEvent()

        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "method":"checkout.start",
            "params":${Json.encodeToString(params)}
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        verify(mockEventProcessor).onCheckoutViewStart(any())
    }

    @Test
    fun `respondWith posts JSON-RPC payload to window`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-response",
            "method":"checkout.addressChangeStart",
            "params":{
                "addressType":"shipping",
                "cart":${Json.encodeToString(cart)}
            }
        }""".trimIndent()

        val mockWebView = mock<CheckoutWebView>()
        checkoutBridge.setWebView(mockWebView)
        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStartEvent>()
        verify(mockEventProcessor).onCheckoutViewAddressChangeStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val updatedCart = event.cart.copy(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddress(
                        address = CartDeliveryAddress(
                            firstName = "Ada",
                            countryCode = "US"
                        )
                    )
                )
            )
        )
        val payload = CheckoutAddressChangeStartResponsePayload(cart = updatedCart)

        event.respondWith(payload)
        ShadowLooper.runUiThreadTasks()

        val scriptCaptor = argumentCaptor<String>()
        verify(mockWebView).evaluateJavascript(scriptCaptor.capture(), anyOrNull())
        assertThat(scriptCaptor.firstValue)
            .contains("window.postMessage")
            .contains("\"id\":\"request-id-response\"")
            .contains("\"result\":")
            .contains("\"firstName\":\"Ada\"")
    }

    @Test
    fun `respondWith handles evaluateJavascript failure gracefully without calling onCheckoutViewFailedWithError`() {
        val cart = createTestCart()
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-error",
            "method":"checkout.addressChangeStart",
            "params":{
                "addressType":"shipping",
                "cart":${Json.encodeToString(cart)}
            }
        }""".trimIndent()

        val invocationCount = AtomicInteger(0)
        val mockWebView = mock<CheckoutWebView>()
        whenever(mockWebView.evaluateJavascript(any(), anyOrNull())).thenAnswer { _ ->
            if (invocationCount.getAndIncrement() == 0) {
                error("Failed to evaluate JavaScript")
            }
            null
        }

        checkoutBridge.setWebView(mockWebView)
        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStartEvent>()
        verify(mockEventProcessor).onCheckoutViewAddressChangeStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val updatedCart = event.cart.copy(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddress(
                        address = CartDeliveryAddress(
                            firstName = "Ada",
                            countryCode = "US"
                        )
                    )
                )
            )
        )
        val payload = CheckoutAddressChangeStartResponsePayload(cart = updatedCart)

        event.respondWith(payload)
        ShadowLooper.runUiThreadTasks()

        val scriptCaptor = argumentCaptor<String>()
        verify(mockWebView).evaluateJavascript(scriptCaptor.capture(), anyOrNull())
        assertThat(scriptCaptor.firstValue).contains("window.postMessage")

        // Verify that evaluateJavascript failure doesn't trigger onCheckoutViewFailedWithError
        verify(mockEventProcessor, never()).onCheckoutViewFailedWithError(any())
    }
}

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
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.PaymentTokenInput
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutSubmitStart
import com.shopify.checkoutsheetkit.rpc.events.PaymentMethodChangeStart
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

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStart>()
        verify(mockEventProcessor).onCheckoutAddressChangeStart(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.params.addressType).isEqualTo("shipping")
        assertThat(event.params.cart.id).isEqualTo(cart.id)
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
                "checkout":{"id":"checkout-session-123"}
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutSubmitStart>()
        verify(mockEventProcessor).onCheckoutSubmitStart(eventCaptor.capture())
        val event = eventCaptor.firstValue
        assertThat(event.params.cart.id).isEqualTo(cart.id)
        assertThat(event.params.checkout.id).isEqualTo("checkout-session-123")
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
                "checkout":{"id":"checkout-session-456"}
            }
        }""".trimIndent()

        val mockWebView = mock<CheckoutWebView>()
        checkoutBridge.setWebView(mockWebView)
        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutSubmitStart>()
        verify(mockEventProcessor).onCheckoutSubmitStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val payload = CheckoutSubmitStartResponsePayload(
            payment = PaymentTokenInput(
                token = "tok_test_123",
                tokenType = "card",
                tokenProvider = "delegated"
            )
        )

        event.respondWith(payload)
        ShadowLooper.runUiThreadTasks()

        val scriptCaptor = argumentCaptor<String>()
        verify(mockWebView).evaluateJavascript(scriptCaptor.capture(), anyOrNull())
        assertThat(scriptCaptor.firstValue)
            .contains("window.postMessage")
            .contains("\"id\":\"request-id-submit-response\"")
            .contains("\"result\":")
            .contains("\"token\":\"tok_test_123\"")
            .contains("\"tokenType\":\"card\"")
    fun `postMessage dispatches payment method change start to event processor`() {
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"payment-request-1",
            "method":"checkout.paymentMethodChangeStart",
            "params":{
                "currentCard":{
                    "last4":"4242",
                    "brand":"visa"
                }
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<PaymentMethodChangeStart>()
        verify(mockEventProcessor).onPaymentMethodChangeStart(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.params.currentCard?.last4).isEqualTo("4242")
        assertThat(event.params.currentCard?.brand).isEqualTo("visa")
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

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStart>()
        verify(mockEventProcessor).onCheckoutAddressChangeStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(
                                firstName = "Ada",
                                countryCode = "US"
                            ),
                            selected = true
                        )
                    )
                )
            )
        )

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

        val eventCaptor = argumentCaptor<CheckoutAddressChangeStart>()
        verify(mockEventProcessor).onCheckoutAddressChangeStart(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(
                                firstName = "Ada",
                                countryCode = "US"
                            ),
                            selected = true
                        )
                    )
                )
            )
        )

        event.respondWith(payload)
        ShadowLooper.runUiThreadTasks()

        val scriptCaptor = argumentCaptor<String>()
        verify(mockWebView).evaluateJavascript(scriptCaptor.capture(), anyOrNull())
        assertThat(scriptCaptor.firstValue).contains("window.postMessage")

        // Verify that evaluateJavascript failure doesn't trigger onCheckoutViewFailedWithError
        verify(mockEventProcessor, never()).onCheckoutViewFailedWithError(any())
    }
}

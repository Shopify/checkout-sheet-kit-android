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

import com.shopify.checkoutsheetkit.lifecycleevents.CartInfo
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.lifecycleevents.OrderDetails
import com.shopify.checkoutsheetkit.lifecycleevents.Price
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
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
    fun `postMessage handles JSON-RPC address change requested message`() {
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-1",
            "method":"checkout.addressChangeRequested",
            "params":{
                "addressType":"shipping",
                "selectedAddress":{
                    "firstName":"Ada",
                    "lastName":"Lovelace"
                }
            }
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeRequestedEvent>()
        verify(mockEventProcessor).onAddressChangeRequested(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.addressType).isEqualTo("shipping")
        assertThat(event.selectedAddress?.firstName).isEqualTo("Ada")
        assertThat(event.selectedAddress?.lastName).isEqualTo("Lovelace")
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
    fun `postMessage handles checkout completed JSON-RPC message`() {
        val params = CheckoutCompletedEvent(
            orderDetails = OrderDetails(
                cart = CartInfo(
                    lines = emptyList(),
                    price = Price(),
                    token = "token-123",
                ),
                id = "order-id-123",
            )
        )

        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "method":"checkout.completed",
            "params":${Json.encodeToString(params)}
        }""".trimIndent()

        checkoutBridge.postMessage(jsonRpcMessage)

        verify(mockEventProcessor).onCheckoutViewComplete(any())
    }

    @Test
    fun `respondWith posts JSON-RPC payload to window`() {
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-response",
            "method":"checkout.addressChangeRequested",
            "params":{
                "addressType":"shipping"
            }
        }""".trimIndent()

        val mockWebView = mock<CheckoutWebView>()
        checkoutBridge.setWebView(mockWebView)
        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeRequestedEvent>()
        verify(mockEventProcessor).onAddressChangeRequested(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val payload = DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddressInput(
                        CartDeliveryAddressInput(firstName = "Ada"),
                    ),
                ),
            ),
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
        val jsonRpcMessage = """{
            "jsonrpc":"2.0",
            "id":"request-id-error",
            "method":"checkout.addressChangeRequested",
            "params":{
                "addressType":"shipping"
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

        val eventCaptor = argumentCaptor<CheckoutAddressChangeRequestedEvent>()
        verify(mockEventProcessor).onAddressChangeRequested(eventCaptor.capture())
        val event = eventCaptor.firstValue

        val payload = DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddressInput(
                        CartDeliveryAddressInput(firstName = "Ada"),
                    ),
                ),
            ),
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

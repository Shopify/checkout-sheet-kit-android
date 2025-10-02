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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutBridgeTest {

    private val mockEventProcessor = mock<CheckoutWebViewEventProcessor>()
    private lateinit var checkoutBridge: CheckoutBridge

    @Before
    fun setUp() {
        checkoutBridge = CheckoutBridge(mockEventProcessor)
    }

    @Test
    fun `postMessage handles JSON-RPC address change requested message`() {
        val jsonRpcMessage = envelope(
            """{
                "jsonrpc":"2.0",
                "method":"checkout.addressChangeRequested",
                "params":{
                    "addressType":"shipping",
                    "selectedAddress":{
                        "firstName":"Ada",
                        "lastName":"Lovelace"
                    }
                }
            }""".trimIndent()
        )

        checkoutBridge.postMessage(jsonRpcMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeRequestedEvent>()
        verify(mockEventProcessor).onAddressChangeRequested(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.addressType).isEqualTo("shipping")
        assertThat(event.selectedAddress?.firstName).isEqualTo("Ada")
        assertThat(event.selectedAddress?.lastName).isEqualTo("Lovelace")
    }

    @Test
    fun `postMessage handles JSON-RPC payload nested inside body`() {
        val bodyWrappedMessage = envelope(
            """{
                "jsonrpc":"2.0",
                "method":"checkout.addressChangeRequested",
                "params":{
                    "addressType":"shipping",
                    "selectedAddress":{
                        "firstName":"Ada",
                        "lastName":"Lovelace"
                    }
                }
            }""".trimIndent()
        )

        checkoutBridge.postMessage(bodyWrappedMessage)

        val eventCaptor = argumentCaptor<CheckoutAddressChangeRequestedEvent>()
        verify(mockEventProcessor).onAddressChangeRequested(eventCaptor.capture())

        val event = eventCaptor.firstValue
        assertThat(event.addressType).isEqualTo("shipping")
        assertThat(event.selectedAddress?.firstName).isEqualTo("Ada")
        assertThat(event.selectedAddress?.lastName).isEqualTo("Lovelace")
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

        val jsonRpcMessage = envelope(
            """{
                "jsonrpc":"2.0",
                "method":"checkout.completed",
                "params":${Json.encodeToString(params)}
            }""".trimIndent()
        )

        checkoutBridge.postMessage(jsonRpcMessage)

        verify(mockEventProcessor).onCheckoutViewComplete(any())
    }

    @Test
    fun `postMessage ignores non JSON-RPC payloads`() {
        val unsupported = envelope(
            """{
                "jsonrpc":"2.0",
                "method":"checkout.unsupported",
                "params":{}
            }""".trimIndent()
        )

        checkoutBridge.postMessage(unsupported)

        verifyNoInteractions(mockEventProcessor)
    }

    private fun envelope(message: String): String {
        val encodedBody = Json.encodeToString(message)
        return """{ "body": $encodedBody }"""
    }
}

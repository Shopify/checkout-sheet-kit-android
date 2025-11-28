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
import com.shopify.checkoutsheetkit.lifecycleevents.Checkout
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartResponsePayload
import com.shopify.checkoutsheetkit.rpc.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.CheckoutSubmitStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutSubmitStartEvent
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CheckoutSubmitStartTest {

    @Test
    fun `registry decodes CheckoutSubmitStart from JSON`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.submitStart",
                "params": {
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-789",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "50.00", "currencyCode": "CAD"},
                            "totalAmount": {"amount": "50.00", "currencyCode": "CAD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []}
                    },
                    "checkout": {
                        "id": "checkout-session-789"
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutSubmitStart::class.java)

        val request = decoded as CheckoutSubmitStart
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.params.checkout.id).isEqualTo("checkout-session-789")
    }

    @Test
    fun `companion object provides correct method`() {
        assertThat(CheckoutSubmitStart.method).isEqualTo("checkout.submitStart")
    }

    @Test
    fun `respondWith empty payload does not throw`() {
        val request = createTestRequest()
        val payload = CheckoutSubmitStartResponsePayload()

        assertThatCode { request.respondWith(payload) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `respondWith JSON string throws DecodingFailed on invalid JSON`() {
        val request = createTestRequest()
        val invalidJson = "{ this is not valid JSON }"

        assertThatThrownBy { request.respondWith(invalidJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to parse JSON")
            .hasCauseInstanceOf(Exception::class.java)
    }

    @Test
    fun `respondWith JSON string throws DecodingFailed on type mismatch`() {
        val request = createTestRequest()
        val typeMismatchJson = """
            {
                "payment": "this should be an object, not a string"
            }
        """.trimIndent()

        assertThatThrownBy { request.respondWith(typeMismatchJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to decode response")
            .hasCauseInstanceOf(Exception::class.java)
    }

    private fun createTestRequest() = CheckoutSubmitStart(
        id = "test-id",
        params = CheckoutSubmitStartEvent(
            cart = createTestCart(
                id = "gid://shopify/Cart/test-cart",
                subtotalAmount = "100.00",
                totalAmount = "100.00"
            ),
            checkout = Checkout(id = "checkout-session-123")
        ),
        responseSerializer = CheckoutSubmitStartResponsePayload.serializer()
    )
}

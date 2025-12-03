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

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartParams
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutAddressChangeStartTest {

    @Test
    fun `test decode CheckoutAddressChangeStart from JSON`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "shipping",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-123",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {
                            "email": "test@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-123")
        assertThat(request.addressType).isEqualTo("shipping")
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart-123")
    }

    @Test
    fun `test decode CheckoutAddressChangeStart for billing address`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "billing",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-456",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "50.00", "currencyCode": "CAD"},
                            "totalAmount": {"amount": "50.00", "currencyCode": "CAD"}
                        },
                        "buyerIdentity": {
                            "email": "billing@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-456")
        assertThat(request.addressType).isEqualTo("billing")
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart-456")
    }

    @Test
    fun `test registry can decode CheckoutAddressChangeStart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "shipping",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-789",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {
                            "email": "test@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.addressType).isEqualTo("shipping")
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutAddressChangeStartEvent.method).isEqualTo("checkout.addressChangeStart")
    }

    @Test
    fun `test respondWith payload`() {
        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(
                                firstName = "Ada",
                                lastName = "Lovelace",
                                countryCode = "US"
                            ),
                            selected = true
                        )
                    )
                )
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        request.respondWith(payload)

        assertThat(request.addressType).isEqualTo("shipping")
        assertThat(request.cart.id).isEqualTo(cart.id)
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "cart": {
                    "delivery": {
                        "addresses": [
                            {
                                "address": {
                                    "firstName": "Ada",
                                    "lastName": "Lovelace",
                                    "countryCode": "US"
                                },
                                "selected": true
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        request.respondWith(json)

        assertThat(request.addressType).isEqualTo("shipping")
    }

    @Test
    fun `test multiple respondWith calls are ignored`() {
        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        val payload1 = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(
                                firstName = "First",
                                lastName = "Response",
                                countryCode = "US"
                            ),
                            selected = true
                        )
                    )
                )
            )
        )

        val payload2 = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(
                                firstName = "Second",
                                lastName = "Response",
                                countryCode = "CA"
                            ),
                            selected = true
                        )
                    )
                )
            )
        )

        // First call should succeed (no WebView, so won't actually send, but won't throw)
        assertThatCode { request.respondWith(payload1) }
            .doesNotThrowAnyException()

        // Second call should be ignored (logged but not throw)
        assertThatCode { request.respondWith(payload2) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "gid://shopify/Cart/test-cart",
            subtotalAmount = "100.00",
            totalAmount = "100.00"
        )
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "billing",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request.cart).isEqualTo(cart)
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart")
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
                "cart": "this should be an object, not a string"
            }
        """.trimIndent()

        assertThatThrownBy { request.respondWith(typeMismatchJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to decode response")
            .hasCauseInstanceOf(Exception::class.java)
    }

    @Test
    fun `test toString includes id, method, addressType and cart`() {
        val request = createTestRequest()
        val result = request.toString()

        assertThat(result).contains("id='test-id'")
        assertThat(result).contains("method='checkout.addressChangeStart'")
        assertThat(result).contains("addressType='shipping'")
        assertThat(result).contains("cart=Cart(")
    }

    @Test
    fun `test equals returns true for same id`() {
        val request1 = CheckoutAddressChangeStartEvent(
            id = "same-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "shipping",
                cart = createTestCart()
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutAddressChangeStartEvent(
            id = "same-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "billing", // Different addressType
                cart = createTestCart(id = "different-cart") // Different cart
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isEqualTo(request2)
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode())
    }

    @Test
    fun `test equals returns false for different id`() {
        val request1 = createTestRequest()
        val request2 = CheckoutAddressChangeStartEvent(
            id = "different-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "shipping",
                cart = createTestCart()
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isNotEqualTo(request2)
    }

    private fun createTestRequest() = CheckoutAddressChangeStartEvent(
        id = "test-id",
        params = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = createTestCart(
                id = "gid://shopify/Cart/test-cart",
                subtotalAmount = "100.00",
                totalAmount = "100.00"
            )
        ),
        responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
    )

}

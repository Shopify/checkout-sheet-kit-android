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

import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.lifecycleevents.CartBuyerIdentity
import com.shopify.checkoutsheetkit.lifecycleevents.CartCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.rpc.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStartEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
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
                        "delivery": {"addresses": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutAddressChangeStart)

        val request = decoded as CheckoutAddressChangeStart
        assertEquals("test-123", request.id)
        assertEquals("shipping", request.params.addressType)
        assertEquals("gid://shopify/Cart/test-cart-123", request.params.cart.id)
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
                        "delivery": {"addresses": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutAddressChangeStart)

        val request = decoded as CheckoutAddressChangeStart
        assertEquals("test-456", request.id)
        assertEquals("billing", request.params.addressType)
        assertEquals("gid://shopify/Cart/test-cart-456", request.params.cart.id)
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
                        "delivery": {"addresses": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutAddressChangeStart)

        val request = decoded as CheckoutAddressChangeStart
        assertEquals("test-789", request.id)
        assertEquals("shipping", request.params.addressType)
    }

    @Test
    fun `test companion object provides correct method`() {
        assertEquals("checkout.addressChangeStart", CheckoutAddressChangeStart.method)
    }

    @Test
    fun `test respondWith payload`() {
        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartEvent(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStart(
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

        assertEquals("shipping", request.params.addressType)
        assertEquals(cart.id, request.params.cart.id)
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
        val eventData = CheckoutAddressChangeStartEvent(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        request.respondWith(json)

        assertEquals("shipping", request.params.addressType)
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "gid://shopify/Cart/test-cart",
            subtotalAmount = "100.00",
            totalAmount = "100.00"
        )
        val eventData = CheckoutAddressChangeStartEvent(
            addressType = "billing",
            cart = cart
        )
        val request = CheckoutAddressChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertEquals(cart, request.params.cart)
        assertEquals("gid://shopify/Cart/test-cart", request.params.cart.id)
    }

    @Test
    fun `validate accepts valid 2-character country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(countryCode = "US")
                        )
                    )
                )
            )
        )

        assertThatCode { request.validate(payload) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validate rejects empty country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(countryCode = "")
                        )
                    )
                )
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("Country code is required")
    }

    @Test
    fun `validate rejects null country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(countryCode = null)
                        )
                    )
                )
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("Country code is required")
    }

    @Test
    fun `validate rejects 1-character country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(countryCode = "U")
                        )
                    )
                )
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("must be exactly 2 characters")
            .hasMessageContaining("got: 'U'")
    }

    @Test
    fun `validate rejects 3-character country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(
                            address = CartDeliveryAddressInput(countryCode = "USA")
                        )
                    )
                )
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("must be exactly 2 characters")
            .hasMessageContaining("got: 'USA'")
    }

    @Test
    fun `validate rejects empty addresses list`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(addresses = emptyList())
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("At least one address is required")
    }

    @Test
    fun `validate rejects null addresses list`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(addresses = null)
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("At least one address is required")
    }

    @Test
    fun `validate includes index in error message for invalid country code`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = CartInput(
                delivery = CartDeliveryInput(
                    addresses = listOf(
                        CartSelectableAddressInput(address = CartDeliveryAddressInput(countryCode = "US")),
                        CartSelectableAddressInput(address = CartDeliveryAddressInput(countryCode = "CAN"))
                    )
                )
            )
        )

        assertThatThrownBy { request.validate(payload) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("at index 1")
            .hasMessageContaining("got: 'CAN'")
    }

    @Test
    fun `validate allows null cart in payload`() {
        val request = createTestRequest()
        val payload = CheckoutAddressChangeStartResponsePayload(cart = null)

        assertThatCode { request.validate(payload) }
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
                "cart": "this should be an object, not a string"
            }
        """.trimIndent()

        assertThatThrownBy { request.respondWith(typeMismatchJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to decode response")
            .hasCauseInstanceOf(Exception::class.java)
    }

    @Test
    fun `respondWith JSON string throws ValidationFailed on missing country code`() {
        val request = createTestRequest()
        val jsonWithoutCountryCode = """
            {
                "cart": {
                    "delivery": {
                        "addresses": [
                            {
                                "address": {
                                    "firstName": "Test"
                                },
                                "selected": true
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        assertThatThrownBy { request.respondWith(jsonWithoutCountryCode) }
            .isInstanceOf(CheckoutEventResponseException.ValidationFailed::class.java)
            .hasMessageContaining("Country code is required")
    }

    private fun createTestRequest() = CheckoutAddressChangeStart(
        id = "test-id",
        params = CheckoutAddressChangeStartEvent(
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

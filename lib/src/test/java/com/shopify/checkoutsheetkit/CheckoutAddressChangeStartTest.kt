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
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeStartEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
        val eventData = AddressChangeStartEvent(
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
                                lastName = "Lovelace"
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
                                    "lastName": "Lovelace"
                                },
                                "selected": true
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        val cart = createTestCart()
        val eventData = AddressChangeStartEvent(
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
        val cart = createTestCart()
        val eventData = AddressChangeStartEvent(
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
        assertEquals("test@example.com", request.params.cart.buyerIdentity.email)
    }

    private fun createTestCart() = com.shopify.checkoutsheetkit.lifecycleevents.Cart(
        id = "gid://shopify/Cart/test-cart",
        lines = emptyList(),
        cost = com.shopify.checkoutsheetkit.lifecycleevents.CartCost(
            subtotalAmount = com.shopify.checkoutsheetkit.lifecycleevents.Money("100.00", "USD"),
            totalAmount = com.shopify.checkoutsheetkit.lifecycleevents.Money("100.00", "USD")
        ),
        buyerIdentity = com.shopify.checkoutsheetkit.lifecycleevents.CartBuyerIdentity(
            email = "test@example.com"
        ),
        deliveryGroups = emptyList(),
        discountCodes = emptyList(),
        appliedGiftCards = emptyList(),
        discountAllocations = emptyList(),
        delivery = com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery(
            addresses = emptyList()
        )
    )
}

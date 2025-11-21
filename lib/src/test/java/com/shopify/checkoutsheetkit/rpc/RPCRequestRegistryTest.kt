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
package com.shopify.checkoutsheetkit.rpc

import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequested
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RPCRequestRegistryTest {

    @Test
    fun `decode returns CheckoutStart for checkout start method`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "cart": {
                        "id": "cart-123",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {
                                "amount": "0.00",
                                "currencyCode": "USD"
                            },
                            "totalAmount": {
                                "amount": "0.00",
                                "currencyCode": "USD"
                            }
                        },
                        "buyerIdentity": {
                            "email": null,
                            "phone": null,
                            "customer": null,
                            "countryCode": null
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNotNull("Should decode message successfully", result)
        assertTrue("Should be a CheckoutStart", result is CheckoutStart)
        val request = result as CheckoutStart
        assertEquals("cart-123", request.params.cart.id)
        assertEquals("checkout.start", request.method)
    }

    @Test
    fun `decode returns CheckoutComplete for checkout complete method`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.complete",
                "params": {
                    "orderConfirmation": {
                        "url": null,
                        "order": {
                            "id": "order-123"
                        },
                        "number": "#1001",
                        "isFirstOrder": false
                    },
                    "cart": {
                        "id": "cart-456",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {
                                "amount": "100.00",
                                "currencyCode": "USD"
                            },
                            "totalAmount": {
                                "amount": "100.00",
                                "currencyCode": "USD"
                            }
                        },
                        "buyerIdentity": {
                            "email": "test@example.com",
                            "phone": null,
                            "customer": null,
                            "countryCode": "US"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNotNull("Should decode message successfully", result)
        assertTrue("Should be a CheckoutComplete", result is CheckoutComplete)
        val request = result as CheckoutComplete
        assertEquals("order-123", request.params.orderConfirmation.order.id)
        assertEquals("#1001", request.params.orderConfirmation.number)
        assertEquals("cart-456", request.params.cart.id)
        assertEquals("checkout.complete", request.method)
    }

    @Test
    fun `decode returns AddressChangeRequested for address change method`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "id": "request-123",
                "method": "checkout.addressChangeRequested",
                "params": {
                    "addressType": "shipping",
                    "selectedAddress": {
                        "firstName": "Ada",
                        "lastName": "Lovelace"
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNotNull("Should decode message successfully", result)
        assertTrue("Should be an AddressChangeRequested", result is AddressChangeRequested)
        val request = result as AddressChangeRequested
        assertEquals("request-123", request.id)
        assertEquals("shipping", request.params.addressType)
        assertEquals("Ada", request.params.selectedAddress?.firstName)
        assertEquals("Lovelace", request.params.selectedAddress?.lastName)
        assertEquals("checkout.addressChangeRequested", request.method)
    }

    @Test
    fun `decode returns null for unsupported method`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.unsupportedMethod",
                "params": {}
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNull("Should return null for unsupported method", result)
    }

    @Test
    fun `decode returns null for invalid JSON`() {
        val invalidJson = "{ this is not valid json }"

        val result = RPCRequestRegistry.decode(invalidJson)

        assertNull("Should return null for invalid JSON", result)
    }

    @Test
    fun `decode returns null for wrong JSON-RPC version`() {
        val jsonString = """
            {
                "jsonrpc": "1.0",
                "method": "checkout.start",
                "params": {}
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNull("Should return null for wrong JSON-RPC version", result)
    }

    @Test
    fun `decode returns null for missing method field`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "params": {}
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(jsonString)

        assertNull("Should return null for missing method field", result)
    }

    @Test
    fun `isRegistered returns true for registered methods`() {
        assertTrue("checkout.start should be registered", RPCRequestRegistry.isRegistered("checkout.start"))
        assertTrue("checkout.complete should be registered", RPCRequestRegistry.isRegistered("checkout.complete"))
        assertTrue("checkout.addressChangeRequested should be registered",
            RPCRequestRegistry.isRegistered("checkout.addressChangeRequested"))
    }

    @Test
    fun `isRegistered returns false for unregistered methods`() {
        assertFalse("checkout.unsupported should not be registered", RPCRequestRegistry.isRegistered("checkout.unsupported"))
    }

    @Test
    fun `getRegisteredMethods returns all registered methods`() {
        val methods = RPCRequestRegistry.getRegisteredMethods()

        assertTrue("Should contain checkout.start", methods.contains("checkout.start"))
        assertTrue("Should contain checkout.complete", methods.contains("checkout.complete"))
        assertTrue("Should contain checkout.addressChangeRequested", methods.contains("checkout.addressChangeRequested"))
        assertEquals("Should have exactly 3 registered methods", 3, methods.size)
    }
}
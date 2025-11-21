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

import com.shopify.checkoutsheetkit.rpc.CheckoutStart
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CheckoutStartTest {

    @Test
    fun `test decode CheckoutStart with valid minimal cart`() {
        val json = """
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

        val result = RPCRequestRegistry.decode(json)

        assertNotNull("Should decode message successfully", result)
        assertTrue("Should be a CheckoutStart", result is CheckoutStart)
        val request = result as CheckoutStart
        assertEquals("cart-123", request.params.cart.id)
        assertEquals("checkout.start", request.method)
        assertNull("Should have null id for notification", request.id)
    }

    @Test
    fun `test decode returns null when cart is null`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "cart": null
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when cart is null", result)
    }

    @Test
    fun `test decode returns null when cart is missing`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {}
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when cart is missing", result)
    }

    @Test
    fun `test decode returns null when params is empty object`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {}
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when params is empty", result)
    }

    @Test
    fun `test decode returns null when params is null`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": null
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when params is null", result)
    }

    @Test
    fun `test decode returns null when params is missing`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start"
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when params is missing", result)
    }

    @Test
    fun `test decode returns null when cart id is missing`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "cart": {
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
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when cart id is missing", result)
    }

    @Test
    fun `test decode returns null when cart cost is missing`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "cart": {
                        "id": "cart-123",
                        "lines": [],
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNull("Should return null when cart cost is missing", result)
    }

    @Test
    fun `test decode handles cart with minimal required fields only`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "cart": {
                        "id": "minimal-cart",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {
                                "amount": "100.00",
                                "currencyCode": "CAD"
                            },
                            "totalAmount": {
                                "amount": "113.00",
                                "currencyCode": "CAD"
                            }
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNotNull("Should decode with minimal required fields", result)
        assertTrue("Should be a CheckoutStart", result is CheckoutStart)
        val request = result as CheckoutStart
        assertEquals("minimal-cart", request.params.cart.id)
        assertEquals("100.00", request.params.cart.cost.subtotalAmount.amount)
        assertEquals("113.00", request.params.cart.cost.totalAmount.amount)
        assertEquals("CAD", request.params.cart.cost.subtotalAmount.currencyCode)
    }

    @Test
    fun `test companion object provides correct method`() {
        assertEquals("checkout.start", CheckoutStart.method)
    }

    @Test
    fun `test CheckoutStart is a notification event`() {
        val json = """
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
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json) as? CheckoutStart

        assertNotNull("Should decode successfully", result)
        assertNull("Should not have an id (notification event)", result?.id)
        assertTrue("Should be identified as a notification", result?.isNotification ?: false)
    }

    @Test
    fun `test decode with id still works but id is ignored`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "should-be-ignored",
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
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "delivery": {
                            "addresses": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertNotNull("Should decode even with id present", result)
        assertTrue("Should be a CheckoutStart", result is CheckoutStart)
        val request = result as CheckoutStart
        assertNull("Id should be null for notification events", request.id)
    }
}
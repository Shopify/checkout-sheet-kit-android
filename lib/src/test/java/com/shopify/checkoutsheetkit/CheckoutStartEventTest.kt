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
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutStartEventTest {

    @Test
    fun `test decode CheckoutStartEvent with valid minimal cart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "locale": "en-US",
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
                        },
                        "payment": {
                            "instruments": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(CheckoutStartEvent::class.java)
        val event = result as CheckoutStartEvent
        assertThat(event.cart.id).isEqualTo("cart-123")
        assertThat(event.method).isEqualTo("checkout.start")
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
    }

    @Test
    fun `test decode handles cart with minimal required fields only`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "locale": "en-US",
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
                        },
                        "payment": {
                            "instruments": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(CheckoutStartEvent::class.java)
        val event = result as CheckoutStartEvent
        assertThat(event.cart.id).isEqualTo("minimal-cart")
        assertThat(event.cart.cost.subtotalAmount).isEqualTo(Money(amount = "100.00", currencyCode = "CAD"))
        assertThat(event.cart.cost.totalAmount).isEqualTo(Money(amount = "113.00", currencyCode = "CAD"))
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutStartEvent.method).isEqualTo("checkout.start")
    }

    @Test
    fun `test CheckoutStartEvent is a notification event`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "method": "checkout.start",
                "params": {
                    "locale": "en-US",
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
                        },
                        "payment": {
                            "instruments": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json) as? CheckoutStartEvent

        assertThat(result).isNotNull()
        assertThat(result!!).isInstanceOf(CheckoutNotification::class.java)
    }

    @Test
    fun `test decode with id in JSON returns notification event without id`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "should-be-ignored",
                "method": "checkout.start",
                "params": {
                    "locale": "en-US",
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
                        },
                        "payment": {
                            "instruments": []
                        }
                    }
                }
            }
        """.trimIndent()

        val result = RPCRequestRegistry.decode(json)

        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(CheckoutStartEvent::class.java)
        val event = result as CheckoutStartEvent
        assertThat(event).isInstanceOf(CheckoutNotification::class.java)
    }
}

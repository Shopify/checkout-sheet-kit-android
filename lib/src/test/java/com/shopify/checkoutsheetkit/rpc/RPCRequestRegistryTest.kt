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

import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
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

        assertThat(result)
            .isNotNull()
            .isInstanceOf(CheckoutStart::class.java)

        val request = result as CheckoutStart
        assertThat(request.params.cart.id).isEqualTo("cart-123")
        assertThat(request.method).isEqualTo("checkout.start")
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

        assertThat(result)
            .isNotNull()
            .isInstanceOf(CheckoutComplete::class.java)

        val request = result as CheckoutComplete
        assertThat(request.params.orderConfirmation.order.id).isEqualTo("order-123")
        assertThat(request.params.orderConfirmation.number).isEqualTo("#1001")
        assertThat(request.params.cart.id).isEqualTo("cart-456")
        assertThat(request.method).isEqualTo("checkout.complete")
    }

    @Test
    fun `decode returns CheckoutAddressChangeStart for address change method`() {
        val jsonString = """
            {
                "jsonrpc": "2.0",
                "id": "request-123",
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

        val result = RPCRequestRegistry.decode(jsonString)

        assertThat(result)
            .isNotNull()
            .isInstanceOf(CheckoutAddressChangeStart::class.java)

        val request = result as CheckoutAddressChangeStart
        assertThat(request.id).isEqualTo("request-123")
        assertThat(request.params.addressType).isEqualTo("shipping")
        assertThat(request.params.cart.id).isEqualTo("gid://shopify/Cart/test-cart-123")
        assertThat(request.method).isEqualTo("checkout.addressChangeStart")
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

        assertThat(result).isNull()
    }

    @Test
    fun `decode returns null for invalid JSON`() {
        val invalidJson = "{ this is not valid json }"

        val result = RPCRequestRegistry.decode(invalidJson)

        assertThat(result).isNull()
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

        assertThat(result).isNull()
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

        assertThat(result).isNull()
    }

    @Test
    fun `isRegistered returns true for registered methods`() {
        assertThat(RPCRequestRegistry.isRegistered("checkout.start")).isTrue()
        assertThat(RPCRequestRegistry.isRegistered("checkout.complete")).isTrue()
        assertThat(RPCRequestRegistry.isRegistered("checkout.addressChangeStart")).isTrue()
    }

    @Test
    fun `isRegistered returns false for unregistered methods`() {
        assertThat(RPCRequestRegistry.isRegistered("checkout.unsupported")).isFalse()
    }

    @Test
    fun `getRegisteredMethods returns all registered methods`() {
        val methods = RPCRequestRegistry.getRegisteredMethods()

        assertThat(methods).containsExactlyInAnyOrder(
            "checkout.start",
            "checkout.complete",
            "checkout.addressChangeStart",
            "checkout.submitStart",
            "checkout.paymentMethodChangeStart",
        )
    }
}

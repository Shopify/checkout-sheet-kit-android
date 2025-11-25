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

import com.shopify.checkoutsheetkit.lifecycleevents.CardBrand
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrumentInput
import com.shopify.checkoutsheetkit.lifecycleevents.ResponseError
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.CheckoutPaymentMethodChangeStart
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
class CheckoutPaymentMethodChangeStartTest {

    @Test
    fun `test decode CheckoutPaymentMethodChangeStart with cart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-123",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "paymentInstruments": [
                            {"identifier": "instrument-1"},
                            {"identifier": "instrument-2"}
                        ]
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutPaymentMethodChangeStart)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertEquals("test-123", request.id)
        assertEquals("cart-123", request.params.cart.id)
        assertEquals(2, request.params.cart.paymentInstruments.size)
        assertEquals("instrument-1", request.params.cart.paymentInstruments[0].identifier)
        assertEquals("instrument-2", request.params.cart.paymentInstruments[1].identifier)
    }

    @Test
    fun `test decode CheckoutPaymentMethodChangeStart with empty paymentInstruments`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-456",
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
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutPaymentMethodChangeStart)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertEquals("test-456", request.id)
        assertEquals("cart-456", request.params.cart.id)
        assertTrue(request.params.cart.paymentInstruments.isEmpty())
    }

    @Test
    fun `test registry can decode CheckoutPaymentMethodChangeStart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-789",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "25.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "25.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "paymentInstruments": [{"identifier": "pi-abc"}]
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertNotNull(decoded)
        assertTrue(decoded is CheckoutPaymentMethodChangeStart)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertEquals("test-789", request.id)
        assertEquals("cart-789", request.params.cart.id)
        assertEquals(1, request.params.cart.paymentInstruments.size)
        assertEquals("pi-abc", request.params.cart.paymentInstruments[0].identifier)
    }

    @Test
    fun `test companion object provides correct method`() {
        assertEquals("checkout.paymentMethodChangeStart", CheckoutPaymentMethodChangeStart.method)

        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStart(
            null,
            CheckoutPaymentMethodChangeStartParams(cart),
            CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        assertEquals("checkout.paymentMethodChangeStart", request.method)
    }

    @Test
    fun `test onCheckoutPaymentMethodChangeStart method name is consistent`() {
        assertEquals("checkout.paymentMethodChangeStart", CheckoutPaymentMethodChangeStart.method)
    }

    @Test
    fun `test respondWith payload with cart and payment instruments`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = CartInput(
                paymentInstruments = listOf(
                    CartPaymentInstrumentInput(
                        identifier = "new-instrument-123",
                        lastDigits = "4242",
                        cardHolderName = "John Doe",
                        brand = CardBrand.VISA,
                        expiryMonth = 12,
                        expiryYear = 2025,
                        billingAddress = CartDeliveryAddressInput(
                            firstName = "John",
                            lastName = "Doe",
                            address1 = "123 Main St",
                            city = "Toronto",
                            countryCode = "CA",
                            provinceCode = "ON",
                            zip = "M5V 1A1"
                        )
                    )
                )
            ),
            errors = null
        )

        request.respondWith(payload)

        assertEquals("cart-id-123", request.params.cart.id)
    }

    @Test
    fun `test respondWith payload with errors`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = null,
            errors = listOf(
                ResponseError(
                    code = "INVALID_PAYMENT_METHOD",
                    message = "The selected payment method is not available",
                    fieldTarget = "paymentInstruments"
                )
            )
        )

        request.respondWith(payload)

        assertNotNull(request.params.cart)
    }

    @Test
    fun `test respondWith null cart is valid`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = null,
            errors = null
        )

        request.respondWith(payload)

        assertNotNull(request.params.cart)
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "cart": {
                    "paymentInstruments": [
                        {
                            "identifier": "pi-json-123",
                            "lastDigits": "1234",
                            "cardHolderName": "Jane Smith",
                            "brand": "MASTERCARD",
                            "expiryMonth": 6,
                            "expiryYear": 2026,
                            "billingAddress": {
                                "firstName": "Jane",
                                "lastName": "Smith",
                                "address1": "456 Oak Ave",
                                "city": "Vancouver",
                                "countryCode": "CA",
                                "provinceCode": "BC",
                                "zip": "V6B 2N2"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        request.respondWith(json)

        assertEquals("cart-id-123", request.params.cart.id)
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "exposed-cart-id",
            totalAmount = "199.99"
        )
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStart(
            id = "request-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertEquals("exposed-cart-id", request.params.cart.id)
        assertEquals("199.99", request.params.cart.cost.totalAmount.amount)
    }

    @Test
    fun `test CardBrand enum values`() {
        assertEquals("VISA", CardBrand.VISA.name)
        assertEquals("MASTERCARD", CardBrand.MASTERCARD.name)
        assertEquals("AMERICAN_EXPRESS", CardBrand.AMERICAN_EXPRESS.name)
        assertEquals("DISCOVER", CardBrand.DISCOVER.name)
        assertEquals("DINERS_CLUB", CardBrand.DINERS_CLUB.name)
        assertEquals("JCB", CardBrand.JCB.name)
        assertEquals("MAESTRO", CardBrand.MAESTRO.name)
        assertEquals("UNKNOWN", CardBrand.UNKNOWN.name)
    }
}

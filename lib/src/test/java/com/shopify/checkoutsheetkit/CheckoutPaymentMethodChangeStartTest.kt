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
import com.shopify.checkoutsheetkit.lifecycleevents.CardBrand
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrumentDisplayInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrumentInput
import com.shopify.checkoutsheetkit.lifecycleevents.ExpiryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartMailingAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.ResponseError
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
                        "payment": {
                            "instruments": [
                                {"externalReference": "instrument-1"},
                                {"externalReference": "instrument-2"}
                            ]
                        }
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-123")
        assertThat(request.cart.id).isEqualTo("cart-123")
        assertThat(request.cart.payment.instruments).hasSize(2)
        assertThat(request.cart.payment.instruments[0].externalReference).isEqualTo("instrument-1")
        assertThat(request.cart.payment.instruments[1].externalReference).isEqualTo("instrument-2")
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
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-456")
        assertThat(request.cart.id).isEqualTo("cart-456")
        assertThat(request.cart.payment.instruments).isEmpty()
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
                        "payment": {"instruments": [{"externalReference": "pi-abc"}]}
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.cart.id).isEqualTo("cart-789")
        assertThat(request.cart.payment.instruments).hasSize(1)
        assertThat(request.cart.payment.instruments[0].externalReference).isEqualTo("pi-abc")
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutPaymentMethodChangeStartEvent.method).isEqualTo("checkout.paymentMethodChangeStart")

        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStartEvent(
            "test-id",
            CheckoutPaymentMethodChangeStartParams(cart),
            CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        assertThat(request.method).isEqualTo("checkout.paymentMethodChangeStart")
    }

    @Test
    fun `test onCheckoutPaymentMethodChangeStart method name is consistent`() {
        assertThat(CheckoutPaymentMethodChangeStartEvent.method).isEqualTo("checkout.paymentMethodChangeStart")
    }

    @Test
    fun `test toString includes id, method and cart`() {
        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-123",
            params = CheckoutPaymentMethodChangeStartParams(cart = cart),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val result = request.toString()

        assertThat(result).contains("id='test-123'")
        assertThat(result).contains("method='checkout.paymentMethodChangeStart'")
        assertThat(result).contains("cart=Cart(")
    }

    @Test
    fun `test equals returns true for same id`() {
        val request1 = CheckoutPaymentMethodChangeStartEvent(
            id = "same-id",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutPaymentMethodChangeStartEvent(
            id = "same-id",
            params = CheckoutPaymentMethodChangeStartParams(
                cart = createTestCart(id = "different-cart") // Different cart
            ),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isEqualTo(request2)
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode())
    }

    @Test
    fun `test equals returns false for different id`() {
        val request1 = CheckoutPaymentMethodChangeStartEvent(
            id = "id-1",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutPaymentMethodChangeStartEvent(
            id = "id-2",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isNotEqualTo(request2)
    }

    @Test
    fun `test respondWith payload with cart and payment instruments`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = CartInput(
                paymentInstruments = listOf(
                    CartPaymentInstrumentInput(
                        externalReference = "new-instrument-123",
                        display = CartPaymentInstrumentDisplayInput(
                            last4 = "4242",
                            cardHolderName = "John Doe",
                            brand = CardBrand.VISA,
                            expiry = ExpiryInput(
                                month = 12,
                                year = 2025
                            )
                        ),
                        billingAddress = CartMailingAddressInput(
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

        assertThat(request.cart.id).isEqualTo("cart-id-123")
    }

    @Test
    fun `test respondWith payload with errors`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
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

        assertThat(request.cart).isNotNull()
    }

    @Test
    fun `test respondWith null cart is valid`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = null,
            errors = null
        )

        request.respondWith(payload)

        assertThat(request.cart).isNotNull()
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "cart": {
                    "paymentInstruments": [
                        {
                            "externalReference": "pi-json-123",
                            "display": {
                                "last4": "1234",
                                "cardHolderName": "Jane Smith",
                                "brand": "MASTERCARD",
                                "expiry": {
                                    "month": 6,
                                    "year": 2026
                                }
                            },
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
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        request.respondWith(json)

        assertThat(request.cart.id).isEqualTo("cart-id-123")
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "exposed-cart-id",
            totalAmount = "199.99"
        )
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "request-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request.cart.id).isEqualTo("exposed-cart-id")
        assertThat(request.cart.cost.totalAmount.amount).isEqualTo("199.99")
    }

    @Test
    fun `test CardBrand enum values`() {
        val expectedValues = listOf(
            CardBrand.VISA to "VISA",
            CardBrand.MASTERCARD to "MASTERCARD",
            CardBrand.AMERICAN_EXPRESS to "AMERICAN_EXPRESS",
            CardBrand.DISCOVER to "DISCOVER",
            CardBrand.DINERS_CLUB to "DINERS_CLUB",
            CardBrand.JCB to "JCB",
            CardBrand.MAESTRO to "MAESTRO",
            CardBrand.UNKNOWN to "UNKNOWN"
        )

        expectedValues.forEach { (brand, expectedName) ->
            assertThat(brand.name).isEqualTo(expectedName)
        }
    }

    @Test
    fun `test ExpiryInput deserialization`() {
        val json = """{"month":6,"year":2026}"""
        val expiry = Json.decodeFromString<ExpiryInput>(json)

        assertThat(expiry).isEqualTo(
            ExpiryInput(
                month = 6,
                year = 2026
            )
        )
    }

    @Test
    fun `test CartPaymentInstrumentDisplayInput deserialization`() {
        val json = """
            {
                "last4": "5555",
                "brand": "MASTERCARD",
                "cardHolderName": "Jane Smith",
                "expiry": {"month": 3, "year": 2027}
            }
        """.trimIndent()

        val display = Json.decodeFromString<CartPaymentInstrumentDisplayInput>(json)

        assertThat(display).isEqualTo(
            CartPaymentInstrumentDisplayInput(
                last4 = "5555",
                brand = CardBrand.MASTERCARD,
                cardHolderName = "Jane Smith",
                expiry = ExpiryInput(month = 3, year = 2027)
            )
        )
    }

    @Test
    fun `test CartMailingAddressInput deserialization with optional fields`() {
        val json = """
            {
                "firstName": "Jane",
                "lastName": "Smith",
                "address1": "456 Oak Ave",
                "city": "San Francisco",
                "countryCode": "US",
                "provinceCode": "CA",
                "zip": "94102"
            }
        """.trimIndent()

        val address = Json.decodeFromString<CartMailingAddressInput>(json)

        assertThat(address).isEqualTo(
            CartMailingAddressInput(
                firstName = "Jane",
                lastName = "Smith",
                address1 = "456 Oak Ave",
                city = "San Francisco",
                countryCode = "US",
                provinceCode = "CA",
                zip = "94102"
            )
        )
    }

    @Test
    fun `test CartPaymentInstrumentInput deserialization`() {
        val json = """
            {
                "externalReference": "payment-456",
                "display": {
                    "last4": "0005",
                    "brand": "AMERICAN_EXPRESS",
                    "cardHolderName": "Alex Johnson",
                    "expiry": {"month": 9, "year": 2028}
                },
                "billingAddress": {
                    "firstName": "Alex",
                    "lastName": "Johnson",
                    "address1": "789 Pine St",
                    "city": "Chicago",
                    "countryCode": "US",
                    "provinceCode": "IL",
                    "zip": "60601"
                }
            }
        """.trimIndent()

        val paymentInstrument = Json.decodeFromString<CartPaymentInstrumentInput>(json)

        assertThat(paymentInstrument).isEqualTo(
            CartPaymentInstrumentInput(
                externalReference = "payment-456",
                display = CartPaymentInstrumentDisplayInput(
                    last4 = "0005",
                    brand = CardBrand.AMERICAN_EXPRESS,
                    cardHolderName = "Alex Johnson",
                    expiry = ExpiryInput(month = 9, year = 2028)
                ),
                billingAddress = CartMailingAddressInput(
                    firstName = "Alex",
                    lastName = "Johnson",
                    address1 = "789 Pine St",
                    city = "Chicago",
                    countryCode = "US",
                    provinceCode = "IL",
                    zip = "60601"
                )
            )
        )
    }

    @Test
    fun `test CardBrand deserialization values`() {
        val testCases = listOf(
            "VISA" to CardBrand.VISA,
            "MASTERCARD" to CardBrand.MASTERCARD,
            "AMERICAN_EXPRESS" to CardBrand.AMERICAN_EXPRESS,
            "DISCOVER" to CardBrand.DISCOVER,
            "DINERS_CLUB" to CardBrand.DINERS_CLUB,
            "JCB" to CardBrand.JCB,
            "MAESTRO" to CardBrand.MAESTRO,
            "UNKNOWN" to CardBrand.UNKNOWN
        )

        testCases.forEach { (jsonValue, expectedBrand) ->
            assertThat(Json.decodeFromString<CardBrand>("\"$jsonValue\""))
                .describedAs("CardBrand deserialization for $jsonValue")
                .isEqualTo(expectedBrand)
        }
    }

}

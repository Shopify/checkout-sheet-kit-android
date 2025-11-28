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
import com.shopify.checkoutsheetkit.rpc.events.CheckoutPaymentMethodChangeStart
import kotlinx.serialization.json.Json
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

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStart::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertThat(request.id).isEqualTo("test-123")
        assertThat(request.params.cart.id).isEqualTo("cart-123")
        assertThat(request.params.cart.paymentInstruments).hasSize(2)
        assertThat(request.params.cart.paymentInstruments[0].identifier).isEqualTo("instrument-1")
        assertThat(request.params.cart.paymentInstruments[1].identifier).isEqualTo("instrument-2")
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

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStart::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertThat(request.id).isEqualTo("test-456")
        assertThat(request.params.cart.id).isEqualTo("cart-456")
        assertThat(request.params.cart.paymentInstruments).isEmpty()
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

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStart::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStart
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.params.cart.id).isEqualTo("cart-789")
        assertThat(request.params.cart.paymentInstruments).hasSize(1)
        assertThat(request.params.cart.paymentInstruments[0].identifier).isEqualTo("pi-abc")
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutPaymentMethodChangeStart.method).isEqualTo("checkout.paymentMethodChangeStart")

        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStart(
            null,
            CheckoutPaymentMethodChangeStartParams(cart),
            CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        assertThat(request.method).isEqualTo("checkout.paymentMethodChangeStart")
    }

    @Test
    fun `test onCheckoutPaymentMethodChangeStart method name is consistent`() {
        assertThat(CheckoutPaymentMethodChangeStart.method).isEqualTo("checkout.paymentMethodChangeStart")
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

        assertThat(request.params.cart.id).isEqualTo("cart-id-123")
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

        assertThat(request.params.cart).isNotNull()
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

        assertThat(request.params.cart).isNotNull()
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
        val request = CheckoutPaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        request.respondWith(json)

        assertThat(request.params.cart.id).isEqualTo("cart-id-123")
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

        assertThat(request.params.cart.id).isEqualTo("exposed-cart-id")
        assertThat(request.params.cart.cost.totalAmount.amount).isEqualTo("199.99")
    }

    @Test
    fun `test CardBrand enum values`() {
        assertThat(CardBrand.VISA.name).isEqualTo("VISA")
        assertThat(CardBrand.MASTERCARD.name).isEqualTo("MASTERCARD")
        assertThat(CardBrand.AMERICAN_EXPRESS.name).isEqualTo("AMERICAN_EXPRESS")
        assertThat(CardBrand.DISCOVER.name).isEqualTo("DISCOVER")
        assertThat(CardBrand.DINERS_CLUB.name).isEqualTo("DINERS_CLUB")
        assertThat(CardBrand.JCB.name).isEqualTo("JCB")
        assertThat(CardBrand.MAESTRO.name).isEqualTo("MAESTRO")
        assertThat(CardBrand.UNKNOWN.name).isEqualTo("UNKNOWN")
    }

    @Test
    fun `test ExpiryInput deserialization`() {
        val json = """{"month":6,"year":2026}"""
        val expiry = Json.decodeFromString<ExpiryInput>(json)

        assertThat(expiry.month).isEqualTo(6)
        assertThat(expiry.year).isEqualTo(2026)
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

        assertThat(display.last4).isEqualTo("5555")
        assertThat(display.brand).isEqualTo(CardBrand.MASTERCARD)
        assertThat(display.cardHolderName).isEqualTo("Jane Smith")
        assertThat(display.expiry.month).isEqualTo(3)
        assertThat(display.expiry.year).isEqualTo(2027)
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

        assertThat(address.firstName).isEqualTo("Jane")
        assertThat(address.lastName).isEqualTo("Smith")
        assertThat(address.address1).isEqualTo("456 Oak Ave")
        assertThat(address.address2).isNull()
        assertThat(address.city).isEqualTo("San Francisco")
        assertThat(address.company).isNull()
        assertThat(address.countryCode).isEqualTo("US")
        assertThat(address.phone).isNull()
        assertThat(address.provinceCode).isEqualTo("CA")
        assertThat(address.zip).isEqualTo("94102")
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

        assertThat(paymentInstrument.externalReference).isEqualTo("payment-456")
        assertThat(paymentInstrument.display.last4).isEqualTo("0005")
        assertThat(paymentInstrument.display.brand).isEqualTo(CardBrand.AMERICAN_EXPRESS)
        assertThat(paymentInstrument.display.cardHolderName).isEqualTo("Alex Johnson")
        assertThat(paymentInstrument.display.expiry.month).isEqualTo(9)
        assertThat(paymentInstrument.display.expiry.year).isEqualTo(2028)
        assertThat(paymentInstrument.billingAddress.firstName).isEqualTo("Alex")
        assertThat(paymentInstrument.billingAddress.lastName).isEqualTo("Johnson")
        assertThat(paymentInstrument.billingAddress.address1).isEqualTo("789 Pine St")
        assertThat(paymentInstrument.billingAddress.city).isEqualTo("Chicago")
        assertThat(paymentInstrument.billingAddress.countryCode).isEqualTo("US")
        assertThat(paymentInstrument.billingAddress.provinceCode).isEqualTo("IL")
        assertThat(paymentInstrument.billingAddress.zip).isEqualTo("60601")
    }

    @Test
    fun `test CardBrand deserialization values`() {
        assertThat(Json.decodeFromString<CardBrand>("\"VISA\"")).isEqualTo(CardBrand.VISA)
        assertThat(Json.decodeFromString<CardBrand>("\"MASTERCARD\"")).isEqualTo(CardBrand.MASTERCARD)
        assertThat(Json.decodeFromString<CardBrand>("\"AMERICAN_EXPRESS\"")).isEqualTo(CardBrand.AMERICAN_EXPRESS)
        assertThat(Json.decodeFromString<CardBrand>("\"DISCOVER\"")).isEqualTo(CardBrand.DISCOVER)
        assertThat(Json.decodeFromString<CardBrand>("\"DINERS_CLUB\"")).isEqualTo(CardBrand.DINERS_CLUB)
        assertThat(Json.decodeFromString<CardBrand>("\"JCB\"")).isEqualTo(CardBrand.JCB)
        assertThat(Json.decodeFromString<CardBrand>("\"MAESTRO\"")).isEqualTo(CardBrand.MAESTRO)
        assertThat(Json.decodeFromString<CardBrand>("\"UNKNOWN\"")).isEqualTo(CardBrand.UNKNOWN)
    }

}

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

import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.PaymentMethodChangeStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PaymentMethodChangeStartTest {

    @Test
    fun `test decode PaymentMethodChangeStart with currentCard`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "currentCard": {
                        "last4": "4242",
                        "brand": "visa"
                    }
                }
            }
        """.trimIndent()

        val decoded = PaymentMethodChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is PaymentMethodChangeStart)

        val request = decoded as PaymentMethodChangeStart
        assertEquals("test-123", request.id)
        assertEquals("4242", request.params.currentCard?.last4)
        assertEquals("visa", request.params.currentCard?.brand)
    }

    @Test
    fun `test decode PaymentMethodChangeStart without currentCard`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.paymentMethodChangeStart",
                "params": {}
            }
        """.trimIndent()

        val decoded = PaymentMethodChangeStart.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is PaymentMethodChangeStart)

        val request = decoded as PaymentMethodChangeStart
        assertEquals("test-456", request.id)
        assertNull(request.params.currentCard)
    }

    @Test
    fun `test registry can decode PaymentMethodChangeStart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "currentCard": {
                        "last4": "1234",
                        "brand": "mastercard"
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertNotNull(decoded)
        assertTrue(decoded is PaymentMethodChangeStart)

        val request = decoded as PaymentMethodChangeStart
        assertEquals("test-789", request.id)
        assertEquals("1234", request.params.currentCard?.last4)
        assertEquals("mastercard", request.params.currentCard?.brand)
    }

    @Test
    fun `test companion object provides correct method`() {
        assertEquals("checkout.paymentMethodChangeStart", PaymentMethodChangeStart.method)
        // Also test that instance method matches
        val request = PaymentMethodChangeStart(
            null,
            PaymentMethodChangeStartParams(null),
            PaymentMethodChangePayload.serializer()
        )
        assertEquals("checkout.paymentMethodChangeStart", request.method)
    }

    @Test
    fun `test respondWith payload with useDeliveryAddress true`() {
        val eventData = PaymentMethodChangeStartParams(
            currentCard = CurrentCard("4242", "visa")
        )
        val request = PaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = PaymentMethodChangePayload.serializer()
        )

        val payload = PaymentMethodChangePayload(
            card = PaymentCard(
                last4 = "5678",
                brand = "mastercard"
            ),
            billing = BillingInfo(
                useDeliveryAddress = true,
                address = null
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        request.respondWith(payload)

        assertEquals("4242", request.params.currentCard?.last4)
        assertEquals("visa", request.params.currentCard?.brand)
    }

    @Test
    fun `test respondWith payload with useDeliveryAddress false`() {
        val eventData = PaymentMethodChangeStartParams(null)
        val request = PaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = PaymentMethodChangePayload.serializer()
        )

        val payload = PaymentMethodChangePayload(
            card = PaymentCard(
                last4 = "5678",
                brand = "amex"
            ),
            billing = BillingInfo(
                useDeliveryAddress = false,
                address = CartDeliveryAddressInput(
                    firstName = "John",
                    lastName = "Smith",
                    address1 = "456 Oak St",
                    city = "Vancouver",
                    countryCode = "CA",
                    provinceCode = "BC",
                    zip = "V6B 2N2"
                )
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        request.respondWith(payload)

        assertNull(request.params.currentCard)
    }

    @Test
    fun `test PaymentCard validation requires exactly 4 characters for last4`() {
        assertThrows(IllegalArgumentException::class.java) {
            PaymentCard(last4 = "123", brand = "visa")
        }

        assertThrows(IllegalArgumentException::class.java) {
            PaymentCard(last4 = "12345", brand = "visa")
        }

        // Should not throw
        val validCard = PaymentCard(last4 = "1234", brand = "visa")
        assertEquals("1234", validCard.last4)
    }

    @Test
    fun `test PaymentCard validation requires non-empty brand`() {
        assertThrows(IllegalArgumentException::class.java) {
            PaymentCard(last4 = "1234", brand = "")
        }

        // Should not throw
        val validCard = PaymentCard(last4 = "1234", brand = "visa")
        assertEquals("visa", validCard.brand)
    }

    @Test
    fun `test BillingInfo validation requires address when useDeliveryAddress is false`() {
        assertThrows(IllegalArgumentException::class.java) {
            BillingInfo(useDeliveryAddress = false, address = null)
        }

        // Should not throw when useDeliveryAddress is true
        val validBillingWithoutAddress = BillingInfo(useDeliveryAddress = true, address = null)
        assertTrue(validBillingWithoutAddress.useDeliveryAddress)

        // Should not throw when address is provided with useDeliveryAddress false
        val validBillingWithAddress = BillingInfo(
            useDeliveryAddress = false,
            address = CartDeliveryAddressInput(firstName = "Test")
        )
        assertEquals("Test", validBillingWithAddress.address?.firstName)
    }

    @Test
    fun `test BillingInfo validation requires non-empty countryCode when provided`() {
        assertThrows(IllegalArgumentException::class.java) {
            BillingInfo(
                useDeliveryAddress = false,
                address = CartDeliveryAddressInput(countryCode = "")
            )
        }

        // Should not throw when countryCode is null
        val validBillingNullCountry = BillingInfo(
            useDeliveryAddress = false,
            address = CartDeliveryAddressInput(countryCode = null)
        )
        assertNull(validBillingNullCountry.address?.countryCode)

        // Should not throw when countryCode is non-empty
        val validBillingWithCountry = BillingInfo(
            useDeliveryAddress = false,
            address = CartDeliveryAddressInput(countryCode = "CA")
        )
        assertEquals("CA", validBillingWithCountry.address?.countryCode)
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "card": {
                    "last4": "9876",
                    "brand": "discover"
                },
                "billing": {
                    "useDeliveryAddress": true
                }
            }
        """.trimIndent()

        val eventData = PaymentMethodChangeStartParams(
            currentCard = CurrentCard("1111", "visa")
        )
        val request = PaymentMethodChangeStart(
            id = "test-id",
            params = eventData,
            responseSerializer = PaymentMethodChangePayload.serializer()
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        request.respondWith(json)

        assertEquals("1111", request.params.currentCard?.last4)
    }
}
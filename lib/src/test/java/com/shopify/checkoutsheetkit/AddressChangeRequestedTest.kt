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
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequested
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequestedEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AddressChangeRequestedTest {

    @Test
    fun `test decode AddressChangeRequested from JSON`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.addressChangeRequested",
                "params": {
                    "addressType": "shipping",
                    "selectedAddress": {
                        "firstName": "John",
                        "lastName": "Doe",
                        "address1": "123 Main St",
                        "city": "Toronto",
                        "countryCode": "CA",
                        "provinceCode": "ON",
                        "zip": "M5V 2T6"
                    }
                }
            }
        """.trimIndent()

        val decoded = AddressChangeRequested.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is AddressChangeRequested)

        val request = decoded as AddressChangeRequested
        assertEquals("test-123", request.id)
        assertEquals("shipping", request.params.addressType)
        assertEquals("John", request.params.selectedAddress?.firstName)
        assertEquals("Doe", request.params.selectedAddress?.lastName)
        assertEquals("123 Main St", request.params.selectedAddress?.address1)
        assertEquals("Toronto", request.params.selectedAddress?.city)
        assertEquals("CA", request.params.selectedAddress?.countryCode)
        assertEquals("ON", request.params.selectedAddress?.provinceCode)
        assertEquals("M5V 2T6", request.params.selectedAddress?.zip)
    }

    @Test
    fun `test decode AddressChangeRequested without selectedAddress`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.addressChangeRequested",
                "params": {
                    "addressType": "billing"
                }
            }
        """.trimIndent()

        val decoded = AddressChangeRequested.Companion.decodeErased(json)

        assertNotNull(decoded)
        assertTrue(decoded is AddressChangeRequested)

        val request = decoded as AddressChangeRequested
        assertEquals("test-456", request.id)
        assertEquals("billing", request.params.addressType)
        assertNull(request.params.selectedAddress)
    }

    @Test
    fun `test registry can decode AddressChangeRequested`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.addressChangeRequested",
                "params": {
                    "addressType": "shipping"
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertNotNull(decoded)
        assertTrue(decoded is AddressChangeRequested)

        val request = decoded as AddressChangeRequested
        assertEquals("test-789", request.id)
        assertEquals("shipping", request.params.addressType)
    }

    @Test
    fun `test companion object provides correct method`() {
        assertEquals("checkout.addressChangeRequested", AddressChangeRequested.method)
        // Also test that instance method matches
        val request = AddressChangeRequested(null, AddressChangeRequestedEvent("shipping"))
        assertEquals("checkout.addressChangeRequested", request.method)
    }

    @Test
    fun `test respondWith payload`() {
        val eventData = AddressChangeRequestedEvent(
            addressType = "shipping",
            selectedAddress = null,
        )
        val request = AddressChangeRequested(
            id = "test-id",
            params = eventData
        )

        val payload = DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddressInput(
                        address = CartDeliveryAddressInput(
                            firstName = "Ada",
                            lastName = "Lovelace"
                        )
                    )
                )
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        request.respondWith(payload)

        assertEquals("shipping", request.params.addressType)
        assertNull(request.params.selectedAddress)
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "delivery": {
                    "addresses": [
                        {
                            "address": {
                                "firstName": "Ada",
                                "lastName": "Lovelace"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val eventData = AddressChangeRequestedEvent(
            addressType = "shipping",
            selectedAddress = null,
        )
        val request = AddressChangeRequested(
            id = "test-id",
            params = eventData
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        request.respondWith(json)

        assertEquals("shipping", request.params.addressType)
    }

    @Test
    fun `test exposes selectedAddress from params`() {
        val selectedAddress = CartDeliveryAddressInput(
            firstName = "Ada",
            lastName = "Lovelace",
            city = "London",
        )
        val eventData = AddressChangeRequestedEvent(
            addressType = "billing",
            selectedAddress = selectedAddress,
        )
        val request = AddressChangeRequested(
            id = "test-id",
            params = eventData
        )

        assertEquals(selectedAddress, request.params.selectedAddress)
        assertEquals("Ada", request.params.selectedAddress?.firstName)
        assertEquals("Lovelace", request.params.selectedAddress?.lastName)
        assertEquals("London", request.params.selectedAddress?.city)
    }
}
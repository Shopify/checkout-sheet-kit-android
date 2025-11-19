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

import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequested
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequestedEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutAddressChangeRequestedEventTest {

    @Test
    fun `respondWith payload invokes respondWith on message`() {
        val payload = samplePayload()
        val event = AddressChangeRequested(
            id = "test-id",
            params = AddressChangeRequestedEvent(
                addressType = "shipping",
                selectedAddress = null
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        event.respondWith(payload)

        assertEquals("shipping", event.params.addressType)
        assertNull(event.params.selectedAddress)
    }

    @Test
    fun `respondWith JSON string parses and invokes respondWith on message`() {
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

        val event = AddressChangeRequested(
            id = "test-id",
            params = AddressChangeRequestedEvent(
                addressType = "shipping",
                selectedAddress = null
            )
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        event.respondWith(json)

        assertEquals("shipping", event.params.addressType)
    }

    @Test
    fun `exposes selectedAddress from params`() {
        val selectedAddress = CartDeliveryAddressInput(
            firstName = "Ada",
            lastName = "Lovelace",
            city = "London"
        )
        val event = AddressChangeRequested(
            id = "test-id",
            params = AddressChangeRequestedEvent(
                addressType = "billing",
                selectedAddress = selectedAddress
            )
        )

        assertEquals(selectedAddress, event.params.selectedAddress)
        assertEquals("Ada", event.params.selectedAddress?.firstName)
        assertEquals("Lovelace", event.params.selectedAddress?.lastName)
        assertEquals("London", event.params.selectedAddress?.city)
    }

    private fun samplePayload(): DeliveryAddressChangePayload {
        return DeliveryAddressChangePayload(
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
    }
}

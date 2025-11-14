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
import com.shopify.checkoutsheetkit.events.CartDelivery
import com.shopify.checkoutsheetkit.events.CartDeliveryAddress
import com.shopify.checkoutsheetkit.events.CartSelectableAddress
import com.shopify.checkoutsheetkit.events.CheckoutAddressChangeRequestedEvent
import com.shopify.checkoutsheetkit.events.CheckoutAddressChangeRequestedEventData
import com.shopify.checkoutsheetkit.events.DeliveryAddressChangePayload
import com.shopify.checkoutsheetkit.events.parser.CheckoutMessageParser
import org.junit.Test

class CheckoutAddressChangeRequestedEventTest {

    @Test
    fun `respondWith payload invokes respondWith on message`() {
        val payload = samplePayload()
        val eventData = CheckoutAddressChangeRequestedEventData(
            addressType = "shipping",
            selectedAddress = null,
        )
        val message = CheckoutMessageParser.JSONRPCMessage.AddressChangeRequested(
            id = "test-id",
            params = eventData,
        )
        val event = CheckoutAddressChangeRequestedEvent(message)

        // This will fail to send since no WebView is attached, but we're testing the flow
        event.respondWith(payload)

        assertThat(event.addressType).isEqualTo("shipping")
        assertThat(event.selectedAddress).isNull()
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

        val eventData = CheckoutAddressChangeRequestedEventData(
            addressType = "shipping",
            selectedAddress = null,
        )
        val message = CheckoutMessageParser.JSONRPCMessage.AddressChangeRequested(
            id = "test-id",
            params = eventData,
        )
        val event = CheckoutAddressChangeRequestedEvent(message)

        // This will fail to send since no WebView is attached, but we're testing the parsing
        event.respondWith(json)

        assertThat(event.addressType).isEqualTo("shipping")
    }

    @Test
    fun `exposes selectedAddress from params`() {
        val selectedAddress = CartDeliveryAddress(
            firstName = "Ada",
            lastName = "Lovelace",
            city = "London",
        )
        val eventData = CheckoutAddressChangeRequestedEventData(
            addressType = "billing",
            selectedAddress = selectedAddress,
        )
        val message = CheckoutMessageParser.JSONRPCMessage.AddressChangeRequested(
            id = "test-id",
            params = eventData,
        )
        val event = CheckoutAddressChangeRequestedEvent(message)

        assertThat(event.selectedAddress).isEqualTo(selectedAddress)
        assertThat(event.selectedAddress?.firstName).isEqualTo("Ada")
        assertThat(event.selectedAddress?.lastName).isEqualTo("Lovelace")
        assertThat(event.selectedAddress?.city).isEqualTo("London")
    }

    private fun samplePayload(): DeliveryAddressChangePayload {
        return DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddress(
                        address = CartDeliveryAddress(
                            firstName = "Ada",
                            lastName = "Lovelace"
                        )
                    )
                )
            )
        )
    }
}

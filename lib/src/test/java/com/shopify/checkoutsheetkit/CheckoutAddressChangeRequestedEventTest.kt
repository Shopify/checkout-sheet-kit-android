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

import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.DeliveryAddressChangePayload
import java.util.concurrent.CompletableFuture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CheckoutAddressChangeRequestedEventTest {

    @Test
    fun `respondWith future completes with payload`() {
        var respondedPayload: DeliveryAddressChangePayload? = null
        var cancelled = false
        val event = CheckoutAddressChangeRequestedEvent(
            addressType = "shipping",
            selectedAddress = null,
            onResponse = { respondedPayload = it },
            onCancel = { cancelled = true }
        )

        val payload = samplePayload()
        val future = CompletableFuture<DeliveryAddressChangePayload>()
        event.respondWith(future)
        future.complete(payload)

        assertThat(respondedPayload).isEqualTo(payload)
        assertThat(cancelled).isFalse()
    }

    @Test
    fun `respondWith future completes exceptionally cancels`() {
        var respondedPayload: DeliveryAddressChangePayload? = null
        var cancelled = false
        val event = CheckoutAddressChangeRequestedEvent(
            addressType = "shipping",
            selectedAddress = null,
            onResponse = { respondedPayload = it },
            onCancel = { cancelled = true }
        )

        val future = CompletableFuture<DeliveryAddressChangePayload>()
        event.respondWith(future)
        future.completeExceptionally(RuntimeException("boom"))

        assertThat(respondedPayload).isNull()
        assertThat(cancelled).isTrue()
    }

    @Test
    fun `cancel cancels pending future`() {
        var respondedPayload: DeliveryAddressChangePayload? = null
        var cancelled = false
        val event = CheckoutAddressChangeRequestedEvent(
            addressType = "shipping",
            selectedAddress = null,
            onResponse = { respondedPayload = it },
            onCancel = { cancelled = true }
        )

        val future = CompletableFuture<DeliveryAddressChangePayload>()
        event.respondWith(future)
        event.cancel()

        assertThat(future).isCompletedExceptionally
        assertThat(respondedPayload).isNull()
        assertThat(cancelled).isTrue()
    }

    private fun samplePayload(): DeliveryAddressChangePayload {
        return DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddressInput(
                        address = CheckoutAddressChangeRequestedSelectedAddress(
                            firstName = "Ada",
                            lastName = "Lovelace"
                        ).toCartDeliveryAddressInput()
                    )
                )
            )
        )
    }

    private fun CheckoutAddressChangeRequestedSelectedAddress.toCartDeliveryAddressInput() =
        com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput(
            firstName = firstName,
            lastName = lastName,
            company = company,
            address1 = address1,
            address2 = address2,
            city = city,
            countryCode = countryCode,
            phone = phone,
            provinceCode = provinceCode,
            zip = zip,
        )
}

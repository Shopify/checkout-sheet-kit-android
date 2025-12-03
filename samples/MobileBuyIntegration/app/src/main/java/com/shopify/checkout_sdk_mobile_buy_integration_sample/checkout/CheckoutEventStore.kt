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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.shopify.checkoutsheetkit.CheckoutRequest

/**
 * Stores any CheckoutRequest (address change, payment change, etc.) by ID for deferred response.
 */
class CheckoutEventStore {
    val events = mutableMapOf<String, CheckoutRequest<*>>()

    fun storeEvent(event: CheckoutRequest<*>): String {
        events[event.id] = event
        return event.id
    }

    /**
     * Retrieves a stored event by ID with type-safe casting.
     *
     * @param T The expected event type
     * @param eventId The ID of the stored event
     * @return The event cast to the specified type, or null if not found or wrong type
     */
    inline fun <reified T : CheckoutRequest<*>> getEvent(eventId: String): T? {
        return events[eventId] as? T
    }

    fun removeEvent(eventId: String) {
        events.remove(eventId)
    }
}

val LocalCheckoutEventStore = staticCompositionLocalOf<CheckoutEventStore> {
    error("No CheckoutEventStore provided")
}

@Composable
fun CheckoutEventProvider(content: @Composable () -> Unit) {
    val eventStore = remember { CheckoutEventStore() }
    CompositionLocalProvider(LocalCheckoutEventStore provides eventStore) {
        content()
    }
}

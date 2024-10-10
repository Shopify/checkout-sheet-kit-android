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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics

import com.shopify.checkoutsheetkit.pixelevents.CheckoutCompletedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import kotlinx.serialization.json.Json

object Analytics {
    fun userId(): String  {
        // return ID associated with user in analytics system
        return "123"
    }

    fun record(analyticsEvent: AnalyticsEvent) {
        // implement record, e.g. via calling analytics sdk function
        println(analyticsEvent)
    }
}

data class AnalyticsEvent(
    val id: String,
    val name: String,
    val userId: String,
    val timestamp: String,
    val checkoutAmount: Double,
)

data class FirstCustomEventData(
    val attr1: Double,
)

data class SecondCustomEventData(
    val attr2: Double,
)

fun CheckoutStartedPixelEvent.toAnalyticsEvent(): AnalyticsEvent {
    return AnalyticsEvent(
        id = id ?: "",
        name = name ?: "",
        timestamp = timestamp ?: "",
        userId = Analytics.userId(),
        checkoutAmount = data?.checkout?.totalPrice?.amount ?: 0.0
    )
}

fun CheckoutCompletedPixelEvent.toAnalyticsEvent(): AnalyticsEvent {
    return AnalyticsEvent(
        id = id ?: "",
        name = name ?: "",
        timestamp = timestamp ?: "",
        userId = Analytics.userId(),
        checkoutAmount = data?.checkout?.totalPrice?.amount ?: 0.0
    )
}

fun CustomPixelEvent.toAnalyticsEvent(): AnalyticsEvent? {
    return when (name) {
        "first_custom_event" -> {
            val eventData = Json.decodeFromString<FirstCustomEventData>(customData!!)
            AnalyticsEvent(
                id = id ?: "",
                name = name ?: "",
                timestamp = timestamp ?: "",
                userId = Analytics.userId(),
                checkoutAmount = eventData.attr1,
            )
        }
        "second_custom_event" -> {
            val eventData = Json.decodeFromString<SecondCustomEventData>(customData!!)
            AnalyticsEvent(
                id = id ?: "",
                name = name ?: "",
                timestamp = timestamp ?: "",
                userId = Analytics.userId(),
                checkoutAmount = eventData.attr2,
            )
        }
        else -> {
            print("unknown event")
            null
        }
    }
}

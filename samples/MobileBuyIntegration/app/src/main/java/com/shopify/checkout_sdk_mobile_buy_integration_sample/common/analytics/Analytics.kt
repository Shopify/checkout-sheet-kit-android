package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics

import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.serialization.json.Json

object Analytics {
    fun userId(): String  {
        // return ID associated with user in analytics system
        return "123"
    }

    fun record(analyticsEvent: AnalyticsEvent) {
        // implement record, e.g. via calling analytics sdk function
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

fun StandardPixelEvent.toAnalyticsEvent(): AnalyticsEvent {
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

package com.shopify.checkoutkit.messages

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class AnalyticsEventDecoder(private val decoder: Json) {
    internal fun decode(decodedMsg: WebToSDKMessage): AnalyticsEvent? {
        return try {
            val rawEvent = decoder.decodeFromString<RawAnalyticsEvent>(decodedMsg.body)
            when (rawEvent.name) {
                "cart_viewed" -> decoder.decodeFromJsonElement<CartViewedEvent>(rawEvent.event)
                else -> {
                    Log.w("CheckoutBridge", "Received unrecognized event")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("CheckoutBridge", "Couldn't decode event ${decodedMsg.body}")
            null
        }
    }
}

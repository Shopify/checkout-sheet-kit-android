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
package com.shopify.checkoutkit.messages

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.shopify.checkoutkit.WebToSdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal class AnalyticsEventDecoder(private val decoder: Json) {
    @Suppress("CyclomaticComplexMethod")
    fun decode(decodedMsg: WebToSdkEvent): AnalyticsEvent? {
        return try {
            val rawEvent = decoder.decodeFromString<RawAnalyticsEvent>(decodedMsg.body)
            when (rawEvent.name) {
                "cart_viewed" -> decoder.decodeFromJsonElement<CartViewed>(rawEvent.event)
                "checkout_address_info_submitted" -> decoder.decodeFromJsonElement<CheckoutAddressInfoSubmitted>(rawEvent.event)
                "checkout_completed" -> decoder.decodeFromJsonElement<CheckoutCompleted>(rawEvent.event)
                "checkout_contact_info_submitted" -> decoder.decodeFromJsonElement<CheckoutContactInfoSubmitted>(rawEvent.event)
                "checkout_shipping_info_submitted" -> decoder.decodeFromJsonElement<CheckoutShippingInfoSubmitted>(rawEvent.event)
                "checkout_started" -> decoder.decodeFromJsonElement<CheckoutStarted>(rawEvent.event)
                "collection_viewed" -> decoder.decodeFromJsonElement<CollectionViewed>(rawEvent.event)
                "page_viewed" -> decoder.decodeFromJsonElement<PageViewed>(rawEvent.event)
                "payment_info_submitted" -> decoder.decodeFromJsonElement<PaymentInfoSubmitted>(rawEvent.event)
                "product_added_to_cart" -> decoder.decodeFromJsonElement<ProductAddedToCart>(rawEvent.event)
                "product_removed_from_cart" -> decoder.decodeFromJsonElement<ProductRemovedFromCart>(rawEvent.event)
                "product_viewed" -> decoder.decodeFromJsonElement<ProductViewed>(rawEvent.event)
                "search_submitted" -> decoder.decodeFromJsonElement<SearchSubmitted>(rawEvent.event)
                else -> {
                    Log.w("CheckoutBridge", "Non standard event received ${rawEvent.name}, decoding to custom event")
                    decoder.decodeFromJsonElement<CustomEvent>(rawEvent.event)
                }
            }
        } catch (e: Exception) {
            Log.e("CheckoutBridge", "Couldn't decode event ${decodedMsg.body}")
            null
        }
    }
}

@Serializable
internal class RawAnalyticsEvent(
    internal val name: String,
    internal val event: JsonObject,
)

public interface AnalyticsEvent {
    /**
     * The ID of the customer event
     */
    public val id: String?

    /**
     * The name of the customer event
     */
    public val name: String?

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    public val timestamp: String?

    public val context: Context?
}

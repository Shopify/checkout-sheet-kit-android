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
import com.shopify.checkoutkit.LogWrapper
import com.shopify.checkoutkit.WebToSdkEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class AnalyticsEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    @Suppress("CyclomaticComplexMethod")
    fun decode(decodedMsg: WebToSdkEvent): AnalyticsEvent? {
        return try {
            val rawEvent = decoder.decodeFromString<RawAnalyticsEvent>(decodedMsg.body)
            when (AnalyticsEventType.fromEventName(rawEvent.name)) {
                AnalyticsEventType.CART_VIEWED ->
                    decoder.decodeFromJsonElement<CartViewed>(rawEvent.event)
                AnalyticsEventType.CHECKOUT_ADDRESS_INFO_SUBMITTED ->
                    decoder.decodeFromJsonElement<CheckoutAddressInfoSubmitted>(rawEvent.event)
                AnalyticsEventType.CHECKOUT_COMPLETED ->
                    decoder.decodeFromJsonElement<CheckoutCompleted>(rawEvent.event)
                AnalyticsEventType.CHECKOUT_CONTACT_INFO_SUBMITTED ->
                    decoder.decodeFromJsonElement<CheckoutContactInfoSubmitted>(rawEvent.event)
                AnalyticsEventType.CHECKOUT_SHIPPING_INFO_SUBMITTED ->
                    decoder.decodeFromJsonElement<CheckoutShippingInfoSubmitted>(rawEvent.event)
                AnalyticsEventType.CHECKOUT_STARTED ->
                    decoder.decodeFromJsonElement<CheckoutStarted>(rawEvent.event)
                AnalyticsEventType.COLLECTION_VIEWED ->
                    decoder.decodeFromJsonElement<CollectionViewed>(rawEvent.event)
                AnalyticsEventType.PAGE_VIEWED ->
                    decoder.decodeFromJsonElement<PageViewed>(rawEvent.event)
                AnalyticsEventType.PAYMENT_INFO_SUBMITTED ->
                    decoder.decodeFromJsonElement<PaymentInfoSubmitted>(rawEvent.event)
                AnalyticsEventType.PRODUCT_ADDED_TO_CART ->
                    decoder.decodeFromJsonElement<ProductAddedToCart>(rawEvent.event)
                AnalyticsEventType.PRODUCT_REMOVED_FROM_CART ->
                    decoder.decodeFromJsonElement<ProductRemovedFromCart>(rawEvent.event)
                AnalyticsEventType.PRODUCT_VIEWED ->
                    decoder.decodeFromJsonElement<ProductViewed>(rawEvent.event)
                AnalyticsEventType.SEARCH_SUBMITTED ->
                    decoder.decodeFromJsonElement<SearchSubmitted>(rawEvent.event)
                null -> {
                    // this could be a standard event we don't yet know about or a custom event
                    if (rawEvent.event.containsKey("customData")) {
                        decoder.decodeFromJsonElement<CustomEvent>(rawEvent.event)
                    } else {
                        log.w("CheckoutBridge", "Unrecognized standard analytics event received '${rawEvent.name}'")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            log.e("CheckoutBridge", "Failed to decode analytics event", e)
            null
        }
    }
}

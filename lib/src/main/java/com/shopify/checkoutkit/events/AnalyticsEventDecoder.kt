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
package com.shopify.checkoutkit.events

import com.shopify.checkoutkit.LogWrapper
import com.shopify.checkoutkit.WebToSdkEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

internal class AnalyticsEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(decodedMsg: WebToSdkEvent): AnalyticsEvent? {
        return try {
            val rawEvent = decoder.decodeFromString<RawAnalyticsEvent>(decodedMsg.body)
            when (EventType.fromTypeName(rawEvent.event["type"]?.jsonPrimitive?.content)) {
                EventType.DOM -> decodeDomEvent(rawEvent.name, rawEvent.event)
                EventType.STANDARD -> decodeStandardEvent(rawEvent.name, rawEvent.event)
                EventType.CUSTOM -> decodeCustomEvent(rawEvent.event)
                else -> return null
            }
        } catch (e: Exception) {
            log.e("CheckoutBridge", "Failed to decode analytics event", e)
            null
        }
    }

    private fun decodeDomEvent(name: String, jsonElement: JsonElement): AnalyticsEvent? {
        return when (DomEventType.fromEventName(name)) {
            DomEventType.DOM_EVENT_CLICKED ->
                decoder.decodeFromJsonElement<DomEventsClicked>(jsonElement)
            DomEventType.DOM_EVENT_FORM_SUBMITTED ->
                decoder.decodeFromJsonElement<DomEventsFormSubmitted>(jsonElement)
            DomEventType.DOM_EVENT_INPUT_BLURRED ->
                decoder.decodeFromJsonElement<DomEventsInputBlurred>(jsonElement)
            DomEventType.DOM_EVENT_INPUT_CHANGED ->
                decoder.decodeFromJsonElement<DomEventsInputChanged>(jsonElement)
            DomEventType.DOM_EVENT_INPUT_FOCUSED ->
                decoder.decodeFromJsonElement<DomEventsInputFocused>(jsonElement)
            null -> {
                log.w("CheckoutBridge", "Unrecognized dom analytics event received '$name'")
                return null
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun decodeStandardEvent(name: String, jsonElement: JsonElement): AnalyticsEvent? {
        return when (StandardAnalyticsEventType.fromEventName(name)) {
            StandardAnalyticsEventType.CART_VIEWED ->
                decoder.decodeFromJsonElement<CartViewed>(jsonElement)
            StandardAnalyticsEventType.CHECKOUT_ADDRESS_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutAddressInfoSubmitted>(jsonElement)
            StandardAnalyticsEventType.CHECKOUT_COMPLETED ->
                decoder.decodeFromJsonElement<CheckoutCompleted>(jsonElement)
            StandardAnalyticsEventType.CHECKOUT_CONTACT_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutContactInfoSubmitted>(jsonElement)
            StandardAnalyticsEventType.CHECKOUT_SHIPPING_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutShippingInfoSubmitted>(jsonElement)
            StandardAnalyticsEventType.CHECKOUT_STARTED ->
                decoder.decodeFromJsonElement<CheckoutStarted>(jsonElement)
            StandardAnalyticsEventType.COLLECTION_VIEWED ->
                decoder.decodeFromJsonElement<CollectionViewed>(jsonElement)
            StandardAnalyticsEventType.PAGE_VIEWED ->
                decoder.decodeFromJsonElement<PageViewed>(jsonElement)
            StandardAnalyticsEventType.PAYMENT_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<PaymentInfoSubmitted>(jsonElement)
            StandardAnalyticsEventType.PRODUCT_ADDED_TO_CART ->
                decoder.decodeFromJsonElement<ProductAddedToCart>(jsonElement)
            StandardAnalyticsEventType.PRODUCT_REMOVED_FROM_CART ->
                decoder.decodeFromJsonElement<ProductRemovedFromCart>(jsonElement)
            StandardAnalyticsEventType.PRODUCT_VIEWED ->
                decoder.decodeFromJsonElement<ProductViewed>(jsonElement)
            StandardAnalyticsEventType.SEARCH_SUBMITTED ->
                decoder.decodeFromJsonElement<SearchSubmitted>(jsonElement)
            null -> {
                log.w("CheckoutBridge", "Unrecognized standard analytics event received '$name'")
                return null
            }
        }
    }

    private fun decodeCustomEvent(jsonElement: JsonElement): CustomEvent {
        return decoder.decodeFromJsonElement<CustomEvent>(jsonElement)
    }
}

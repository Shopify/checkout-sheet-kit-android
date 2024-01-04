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

internal class PixelEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(decodedMsg: WebToSdkEvent): PixelEvent? {
        return try {
            val eventWrapper = decoder.decodeFromString<PixelEventWrapper>(decodedMsg.body)
            when (EventType.fromTypeName(eventWrapper.event["type"]?.jsonPrimitive?.content)) {
                EventType.DOM -> decodeDomEvent(eventWrapper.name, eventWrapper.event)
                EventType.STANDARD -> decodeStandardEvent(eventWrapper.name, eventWrapper.event)
                EventType.CUSTOM -> decodeCustomEvent(eventWrapper.event)
                else -> return null
            }
        } catch (e: Exception) {
            log.e("CheckoutBridge", "Failed to decode pixel event", e)
            null
        }
    }

    private fun decodeDomEvent(name: String, jsonElement: JsonElement): PixelEvent? {
        return when (DomPixelsEventType.fromEventName(name)) {
            DomPixelsEventType.DOM_EVENT_CLICKED ->
                decoder.decodeFromJsonElement<DomEventsClicked>(jsonElement)
            DomPixelsEventType.DOM_EVENT_FORM_SUBMITTED ->
                decoder.decodeFromJsonElement<DomEventsFormSubmitted>(jsonElement)
            DomPixelsEventType.DOM_EVENT_INPUT_BLURRED ->
                decoder.decodeFromJsonElement<DomEventsInputBlurred>(jsonElement)
            DomPixelsEventType.DOM_EVENT_INPUT_CHANGED ->
                decoder.decodeFromJsonElement<DomEventsInputChanged>(jsonElement)
            DomPixelsEventType.DOM_EVENT_INPUT_FOCUSED ->
                decoder.decodeFromJsonElement<DomEventsInputFocused>(jsonElement)
            null -> {
                log.w("CheckoutBridge", "Unrecognized dom pixel event received '$name'")
                return null
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun decodeStandardEvent(name: String, jsonElement: JsonElement): PixelEvent? {
        return when (StandardPixelsEventType.fromEventName(name)) {
            StandardPixelsEventType.CART_VIEWED ->
                decoder.decodeFromJsonElement<CartViewed>(jsonElement)
            StandardPixelsEventType.CHECKOUT_ADDRESS_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutAddressInfoSubmitted>(jsonElement)
            StandardPixelsEventType.CHECKOUT_COMPLETED ->
                decoder.decodeFromJsonElement<CheckoutCompleted>(jsonElement)
            StandardPixelsEventType.CHECKOUT_CONTACT_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutContactInfoSubmitted>(jsonElement)
            StandardPixelsEventType.CHECKOUT_SHIPPING_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<CheckoutShippingInfoSubmitted>(jsonElement)
            StandardPixelsEventType.CHECKOUT_STARTED ->
                decoder.decodeFromJsonElement<CheckoutStarted>(jsonElement)
            StandardPixelsEventType.COLLECTION_VIEWED ->
                decoder.decodeFromJsonElement<CollectionViewed>(jsonElement)
            StandardPixelsEventType.PAGE_VIEWED ->
                decoder.decodeFromJsonElement<PageViewed>(jsonElement)
            StandardPixelsEventType.PAYMENT_INFO_SUBMITTED ->
                decoder.decodeFromJsonElement<PaymentInfoSubmitted>(jsonElement)
            StandardPixelsEventType.PRODUCT_ADDED_TO_CART ->
                decoder.decodeFromJsonElement<ProductAddedToCart>(jsonElement)
            StandardPixelsEventType.PRODUCT_REMOVED_FROM_CART ->
                decoder.decodeFromJsonElement<ProductRemovedFromCart>(jsonElement)
            StandardPixelsEventType.PRODUCT_VIEWED ->
                decoder.decodeFromJsonElement<ProductViewed>(jsonElement)
            StandardPixelsEventType.SEARCH_SUBMITTED ->
                decoder.decodeFromJsonElement<SearchSubmitted>(jsonElement)
            null -> {
                log.w("CheckoutBridge", "Unrecognized standard pixel event received '$name'")
                return null
            }
        }
    }

    private fun decodeCustomEvent(jsonElement: JsonElement): CustomEvent {
        return decoder.decodeFromJsonElement<CustomEvent>(jsonElement)
    }
}

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
package com.shopify.checkoutsheetkit.pixelevents

import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.WebToSdkEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

internal class PixelEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(decodedMsg: WebToSdkEvent): PixelEvent? {
        return try {
            val eventWrapper = decoder.decodeFromString<PixelEventWrapper>(decodedMsg.body)
            val eventType = EventType.fromTypeName(eventWrapper.event["type"]?.jsonPrimitive?.content)
            when {
                eventType == EventType.CUSTOM ->
                    decoder.decodeFromJsonElement<CustomPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.PAGE_VIEWED.value ->
                    decoder.decodeFromJsonElement<PageViewedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.CHECKOUT_STARTED.value ->
                    decoder.decodeFromJsonElement<CheckoutStartedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.CHECKOUT_CONTACT_INFO_SUBMITTED.value ->
                    decoder.decodeFromJsonElement<CheckoutContactInfoSubmittedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.CHECKOUT_ADDRESS_INFO_SUBMITTED.value ->
                    decoder.decodeFromJsonElement<CheckoutAddressInfoSubmittedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.CHECKOUT_SHIPPING_INFO_SUBMITTED.value ->
                    decoder.decodeFromJsonElement<CheckoutShippingInfoSubmittedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.PAYMENT_INFO_SUBMITTED.value ->
                    decoder.decodeFromJsonElement<PaymentInfoSubmittedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.CHECKOUT_COMPLETED.value ->
                    decoder.decodeFromJsonElement<CheckoutCompletedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.ALERT_DISPLAYED.value ->
                    decoder.decodeFromJsonElement<AlertDisplayedPixelEvent>(eventWrapper.event)
                eventWrapper.name == EventName.UI_EXTENSION_ERRORED.value ->
                    decoder.decodeFromJsonElement<UIExtensionErroredPixelEvent>(eventWrapper.event)
                else -> return null
            }
        } catch (e: Exception) {
            log.e("CheckoutBridge", "Failed to decode pixel event", e)
            null
        }
    }

    companion object {
        enum class EventName(val value: String) {
            PAGE_VIEWED("page_viewed"),
            CHECKOUT_STARTED("checkout_started"),
            CHECKOUT_COMPLETED("checkout_completed"),
            PAYMENT_INFO_SUBMITTED("payment_info_submitted"),
            CHECKOUT_ADDRESS_INFO_SUBMITTED("checkout_address_info_submitted"),
            CHECKOUT_CONTACT_INFO_SUBMITTED("checkout_contact_info_submitted"),
            CHECKOUT_SHIPPING_INFO_SUBMITTED("checkout_shipping_info_submitted"),
            ALERT_DISPLAYED("alert_displayed"),
            UI_EXTENSION_ERRORED("ui_extension_errored"),
        }
    }
}

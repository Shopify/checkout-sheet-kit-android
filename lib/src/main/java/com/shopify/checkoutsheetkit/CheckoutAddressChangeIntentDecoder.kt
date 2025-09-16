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

import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.WebToSdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class CheckoutAddressChangeIntentEventData(
    public val addressType: String
)

public class CheckoutAddressChangeIntentEvent internal constructor(
    public val addressType: String,
    private val onResponse: (DeliveryAddressChangePayload) -> Unit,
    private val onCancel: () -> Unit
) : RespondableEvent() {
    
    private var hasResponded = false

    override fun respondWith(result: DeliveryAddressChangePayload) {
        if (hasResponded) return
        hasResponded = true
        onResponse(result)
    }

    override fun cancel() {
        if (hasResponded) return
        hasResponded = true
        onCancel()
    }
}

/**
 * Decoder for checkout address change intent messages using kotlinx serialization.
 */
internal class CheckoutAddressChangeIntentDecoder constructor(
    private val decoder: Json = Json { ignoreUnknownKeys = true }
) {
    private companion object {
        private const val LOG_TAG = "CheckoutAddressChangeIntentDecoder"
    }

    fun decode(
        decodedMsg: WebToSdkEvent,
        onResponse: (DeliveryAddressChangePayload) -> Unit,
        onCancel: () -> Unit
    ): CheckoutAddressChangeIntentEvent? {
        return try {
            val eventData = decoder.decodeFromString<CheckoutAddressChangeIntentEventData>(decodedMsg.body)
            CheckoutAddressChangeIntentEvent(
                addressType = eventData.addressType,
                onResponse = onResponse,
                onCancel = onCancel
            )
        } catch (e: Exception) {
            log.e(LOG_TAG, "Failed to decode AddressChangeIntent event", e)
            null
        }
    }
}

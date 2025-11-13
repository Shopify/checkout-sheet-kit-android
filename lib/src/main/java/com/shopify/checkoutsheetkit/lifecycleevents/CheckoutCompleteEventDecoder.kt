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
package com.shopify.checkoutsheetkit.lifecycleevents

import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.WebToNativeEvent
import kotlinx.serialization.json.Json

internal class CheckoutCompleteEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(decodedMsg: WebToNativeEvent): CheckoutCompleteEvent {
        return try {
            decoder.decodeFromString<CheckoutCompleteEvent>(decodedMsg.body)
        } catch (e: Exception) {
            log.e(
                "CheckoutBridge",
                "Failed to decode checkout.complete event. Body: ${decodedMsg.body.take(MAX_LOG_BODY_LENGTH)}",
                e
            )
            emptyCompleteEvent()
        }
    }

    private companion object {
        /**
         * Maximum characters to include from the request body in error logs.
         *
         * Captures sufficient context for debugging (full order confirmation + cart ID + 2-3 line items)
         * while preventing excessive log output from large carts (10+ line items can exceed 10KB).
         */
        const val MAX_LOG_BODY_LENGTH = 1500
    }
}

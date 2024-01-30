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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import com.shopify.checkoutsheetkit.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class Logger(
    private val logDb: LogDatabase,
    private val coroutineScope: CoroutineScope,
) {
    fun log(pixelEvent: PixelEvent) {
        when (pixelEvent) {
            is StandardPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.STANDARD_PIXEL,
                        message = pixelEvent.name ?: "",
                        standardPixelEvent = pixelEvent,
                    )
                )
            }
            is CustomPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CUSTOM_PIXEL,
                        message = pixelEvent.name ?: "",
                        customPixelEvent = pixelEvent,
                    )
                )
            }
        }
    }

    fun log(message: String) {
        insert(
            LogLine(
                type = LogType.STANDARD,
                message = message,
            )
        )
    }

    fun log(checkoutCompletedEvent: CheckoutCompletedEvent) {
        insert(
            LogLine(
                type = LogType.CHECKOUT_COMPLETED,
                message = "Checkout completed",
                checkoutCompleted = checkoutCompletedEvent,
            )
        )
    }

    fun log(message: String, e: CheckoutException) {
        insert(
            LogLine(
                id = UUID.randomUUID(),
                type = LogType.ERROR,
                createdAt = Date().time,
                message = message,
                errorDetails = ErrorDetails(
                    message = e.message ?: "No message on error",
                    type = "${e::class.java}"
                ),
            )
        )
    }

    private fun insert(logLine: LogLine) = coroutineScope.launch {
        logDb.logDao().insert(logLine)
    }
}

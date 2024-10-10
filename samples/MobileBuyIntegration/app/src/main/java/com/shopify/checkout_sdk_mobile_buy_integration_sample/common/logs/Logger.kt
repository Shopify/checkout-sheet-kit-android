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

import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.AlertDisplayedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutAddressInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutCompletedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutContactInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutShippingInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PageViewedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PaymentInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.UIExtensionErroredPixelEvent
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
            is AlertDisplayedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.ALERT_DISPLAYED_PIXEL,
                        message = pixelEvent.name ?: "",
                        alertDisplayedPixelEvent = pixelEvent
                    )
                )
            }
            is UIExtensionErroredPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.UI_EXTENSION_ERRORED_PIXEL,
                        message = pixelEvent.name ?: "",
                        uiExtensionErroredPixelEvent = pixelEvent
                    )
                )
            }
            is PageViewedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.PAGE_VIEWED_PIXEL,
                        message = pixelEvent.name ?: "",
                        pageViewedPixelEvent = pixelEvent,
                    )
                )
            }
            is CheckoutStartedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CHECKOUT_STARTED_PIXEL,
                        message = pixelEvent.name ?: "",
                        checkoutStartedPixelEvent = pixelEvent,
                    )
                )
            }
            is CheckoutCompletedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CHECKOUT_COMPLETED_PIXEL,
                        message = pixelEvent.name ?: "",
                        checkoutCompletedPixelEvent = pixelEvent,
                    )
                )
            }
            is PaymentInfoSubmittedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.PAYMENT_INFO_SUBMITTED_PIXEL,
                        message = pixelEvent.name ?: "",
                        paymentInfoSubmittedPixelEvent = pixelEvent,
                    )
                )
            }
            is CheckoutAddressInfoSubmittedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CHECKOUT_ADDRESS_INFO_SUBMITTED_PIXEL,
                        message = pixelEvent.name ?: "",
                        checkoutAddressInfoSubmittedPixelEvent = pixelEvent,
                    )
                )
            }
            is CheckoutContactInfoSubmittedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CHECKOUT_CONTACT_INFO_SUBMITTED_PIXEL,
                        message = pixelEvent.name ?: "",
                        checkoutContactInfoSubmittedPixelEvent = pixelEvent,
                    )
                )
            }
            is CheckoutShippingInfoSubmittedPixelEvent -> {
                insert(
                    LogLine(
                        type = LogType.CHECKOUT_SHIPPING_INFO_SUBMITTED_PIXEL,
                        message = pixelEvent.name ?: "",
                        checkoutShippingInfoSubmittedPixelEvent = pixelEvent,
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

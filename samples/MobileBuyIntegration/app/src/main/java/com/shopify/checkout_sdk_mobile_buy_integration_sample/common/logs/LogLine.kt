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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.AlertDisplayedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutAddressInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutCompletedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutContactInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutShippingInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PageViewedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PaymentInfoSubmittedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.UIExtensionErroredPixelEvent
import java.util.Date
import java.util.UUID

@Entity
data class LogLine(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val createdAt: Long = Date().time,
    val message: String,
    val type: LogType,
    @Embedded(prefix = "page_viewed_pixel") val pageViewedPixelEvent: PageViewedPixelEvent? = null,
    @Embedded(prefix = "checkout_started_pixel") val checkoutStartedPixelEvent: CheckoutStartedPixelEvent? = null,
    @Embedded(prefix = "checkout_contact_info_submitted_pixel") val checkoutContactInfoSubmittedPixelEvent: CheckoutContactInfoSubmittedPixelEvent? = null,
    @Embedded(prefix = "checkout_address_info_submitted_pixel") val checkoutAddressInfoSubmittedPixelEvent: CheckoutAddressInfoSubmittedPixelEvent? = null,
    @Embedded(prefix = "checkout_shipping_info_submitted_pixel") val checkoutShippingInfoSubmittedPixelEvent: CheckoutShippingInfoSubmittedPixelEvent? = null,
    @Embedded(prefix = "payment_info_submitted_pixel") val paymentInfoSubmittedPixelEvent: PaymentInfoSubmittedPixelEvent? = null,
    @Embedded(prefix = "checkout_completed_pixel") val checkoutCompletedPixelEvent: CheckoutCompletedPixelEvent? = null,
    @Embedded(prefix = "alert_displayed_pixel") val alertDisplayedPixelEvent: AlertDisplayedPixelEvent? = null,
    @Embedded(prefix = "ui_extension_errored_pixel") val uiExtensionErroredPixelEvent: UIExtensionErroredPixelEvent? = null,
    @Embedded(prefix = "custom_pixel") val customPixelEvent: CustomPixelEvent? = null,
    @Embedded(prefix = "error_details") val errorDetails: ErrorDetails? = null,
    @Embedded(prefix = "checkout_completed") val checkoutCompleted: CheckoutCompletedEvent? = null,
)

enum class LogType {
    STANDARD,
    ERROR,
    ALERT_DISPLAYED_PIXEL,
    UI_EXTENSION_ERRORED_PIXEL,
    CUSTOM_PIXEL,
    PAGE_VIEWED_PIXEL,
    CHECKOUT_STARTED_PIXEL,
    CHECKOUT_CONTACT_INFO_SUBMITTED_PIXEL,
    CHECKOUT_ADDRESS_INFO_SUBMITTED_PIXEL,
    CHECKOUT_SHIPPING_INFO_SUBMITTED_PIXEL,
    PAYMENT_INFO_SUBMITTED_PIXEL,
    CHECKOUT_COMPLETED_PIXEL,
    CHECKOUT_COMPLETED
}

data class ErrorDetails(
    val type: String?,
    val message: String,
)

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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogLine
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun LogDetailModal(
    logLine: LogLine?,
    onDismissRequest: () -> Unit,
    prettyJson: Json = Json { prettyPrint = true; prettyPrintIndent = "  " }
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (logLine?.type) {
                    LogType.STANDARD -> LogDetails("Checkout Lifecycle Event", logLine.message, Modifier.fillMaxWidth())
                    LogType.ERROR -> LogDetails("Checkout Error", "${logLine.errorDetails}", Modifier.fillMaxWidth())
                    LogType.ALERT_DISPLAYED_PIXEL -> PixelEventDetails(logLine.alertDisplayedPixelEvent, prettyJson)
                    LogType.UI_EXTENSION_ERRORED_PIXEL -> PixelEventDetails(logLine.uiExtensionErroredPixelEvent, prettyJson)
                    LogType.PAGE_VIEWED_PIXEL -> PixelEventDetails(logLine.pageViewedPixelEvent, prettyJson)
                    LogType.CHECKOUT_STARTED_PIXEL -> PixelEventDetails(logLine.checkoutStartedPixelEvent, prettyJson)
                    LogType.CHECKOUT_CONTACT_INFO_SUBMITTED_PIXEL -> PixelEventDetails(logLine.checkoutContactInfoSubmittedPixelEvent, prettyJson)
                    LogType.CHECKOUT_ADDRESS_INFO_SUBMITTED_PIXEL -> PixelEventDetails(logLine.checkoutAddressInfoSubmittedPixelEvent, prettyJson)
                    LogType.CHECKOUT_SHIPPING_INFO_SUBMITTED_PIXEL -> PixelEventDetails(logLine.checkoutShippingInfoSubmittedPixelEvent, prettyJson)
                    LogType.PAYMENT_INFO_SUBMITTED_PIXEL -> PixelEventDetails(logLine.paymentInfoSubmittedPixelEvent, prettyJson)
                    LogType.CHECKOUT_COMPLETED_PIXEL -> PixelEventDetails(logLine.checkoutCompletedPixelEvent, prettyJson)
                    LogType.CUSTOM_PIXEL -> PixelEventDetails(logLine.customPixelEvent, prettyJson)
                    LogType.CHECKOUT_COMPLETED -> CheckoutCompletedDetails(logLine.checkoutCompleted, prettyJson)
                    else -> Text("Unknown log type ${logLine?.type}")
                }
            }
        }
    }
}

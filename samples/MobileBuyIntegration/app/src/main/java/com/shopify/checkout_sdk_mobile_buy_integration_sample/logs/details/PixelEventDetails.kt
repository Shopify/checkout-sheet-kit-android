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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.ROW_COLOR
import com.shopify.checkoutsheetkit.pixelevents.Context
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEventData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun PixelEventDetails(
    event: PixelEvent?,
    prettyJson: Json,
    color: Color = ROW_COLOR,
) {
    LogDetails(header = "Event Name", message = event?.name ?: "", color = color)
    LogDetails(header = "Timestamp", message = event?.timestamp ?: "")
    LogDetails(header = "ID", message = event?.id ?: "", color = color)
    LogDetails(header = "Type", message = event?.type?.name?.lowercase() ?: "")

    when (event) {
        is StandardPixelEvent -> {
            LogDetails(
                header = "Data",
                message = prettyJson.encodeDataToString<StandardPixelEventData>(event.data),
                color = color,
            )
            LogDetails(header = "Context", message = prettyJson.encodeDataToString<Context>(event.context))
        }
        is CustomPixelEvent -> {
            LogDetails(
                header = "Custom Data",
                message = event.customData ?: "",
                color = color,
            )
            LogDetails(header = "Context", message = prettyJson.encodeDataToString<Context>(event.context))
        }
        else -> {}
    }
}

private inline fun <reified T> Json.encodeDataToString(el: T?, default: String = "n/a"): String {
    if (el == null) return default
    return encodeToString(el)
}

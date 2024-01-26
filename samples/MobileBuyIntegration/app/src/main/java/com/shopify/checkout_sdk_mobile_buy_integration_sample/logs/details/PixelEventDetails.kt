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

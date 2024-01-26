package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class Logger(private val logDb: LogDatabase) {
    fun log(pixelEvent: PixelEvent) {
        when (pixelEvent) {
            is StandardPixelEvent -> {
                insert(
                    LogLine(
                        id = UUID.randomUUID(),
                        type = LogType.STANDARD_PIXEL,
                        createdAt = Date().time,
                        message = "Pixel event - ${pixelEvent.name}",
                        standardPixelEvent = pixelEvent,
                    )
                )
            }
            is CustomPixelEvent -> {
                insert(
                    LogLine(
                        id = UUID.randomUUID(),
                        type = LogType.CUSTOM_PIXEL,
                        createdAt = Date().time,
                        message = "Pixel event - ${pixelEvent.name}",
                        customPixelEvent = pixelEvent,
                    )
                )
            }
        }
    }

    fun log(message: String) {
        insert(
            LogLine(
                id = UUID.randomUUID(),
                type = LogType.STANDARD,
                createdAt = Date().time,
                message = message,
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun insert(logLine: LogLine) = GlobalScope.launch(Dispatchers.IO) {
        logDb.logDao().insert(logLine)
    }
}

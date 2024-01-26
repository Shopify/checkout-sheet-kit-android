package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.shopify.checkoutsheetkit.pixelevents.Context
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEventData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Entity
data class LogLine(
    @PrimaryKey val id: UUID,
    val createdAt: Long,
    val message: String,
    val type: LogType,
    @Embedded(prefix = "standard_pixel") val standardPixelEvent: StandardPixelEvent? = null,
    @Embedded(prefix = "custom_pixel") val customPixelEvent: CustomPixelEvent? = null,
    @Embedded(prefix = "error_details") val errorDetails: ErrorDetails? = null,
)

enum class LogType {
    STANDARD, ERROR, CUSTOM_PIXEL, STANDARD_PIXEL
}

data class ErrorDetails(
    val type: String?,
    val message: String,
)

class Converters {
    @TypeConverter
    fun standardPixelEventDataToString(value: StandardPixelEventData): String {
        return Json.encodeToString<StandardPixelEventData>(value)
    }

    @TypeConverter
    fun stringToStandardPixelEventData(value: String): StandardPixelEventData {
        return Json.decodeFromString<StandardPixelEventData>(value)
    }

    @TypeConverter
    fun contextToString(value: Context): String {
        return Json.encodeToString<Context>(value)
    }

    @TypeConverter
    fun stringToContext(value: String): Context {
        return Json.decodeFromString<Context>(value)
    }
}

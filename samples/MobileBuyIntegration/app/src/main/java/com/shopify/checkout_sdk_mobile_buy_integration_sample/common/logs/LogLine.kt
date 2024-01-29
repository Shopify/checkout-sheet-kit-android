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

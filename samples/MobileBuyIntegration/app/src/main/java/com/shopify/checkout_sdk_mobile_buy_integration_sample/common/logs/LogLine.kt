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
import com.shopify.checkoutsheetkit.events.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.pixelevents.Context
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEventData
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

@Entity
data class LogLine(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val createdAt: Long = Date().time,
    val message: String,
    val type: LogType,
    @Embedded(prefix = "standard_pixel") val standardPixelEvent: StandardPixelEvent? = null,
    @Embedded(prefix = "custom_pixel") val customPixelEvent: CustomPixelEvent? = null,
    @Embedded(prefix = "error_details") val errorDetails: ErrorDetails? = null,
    val checkoutCompleted: CheckoutCompleteEvent? = null,
)

enum class LogType {
    STANDARD, ERROR, CUSTOM_PIXEL, STANDARD_PIXEL, CHECKOUT_COMPLETED
}

data class ErrorDetails(
    val type: String?,
    val message: String,
)

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    @TypeConverter
    fun standardPixelEventDataToString(value: StandardPixelEventData?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun stringToStandardPixelEventData(value: String?): StandardPixelEventData? {
        return decodeOrNull(value)
    }

    @TypeConverter
    fun contextToString(value: Context?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun stringToContext(value: String?): Context? {
        return decodeOrNull(value)
    }

    @TypeConverter
    fun checkoutCompletedToString(value: CheckoutCompleteEvent?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun stringToCheckoutCompleted(value: String?): CheckoutCompleteEvent? {
        return decodeOrNull(value)
    }

    private inline fun <reified T> decodeOrNull(value: String?): T? {
        if (value.isNullOrBlank()) {
            return null
        }
        return runCatching {
            json.decodeFromString<T>(value)
        }.getOrNull()
    }
}

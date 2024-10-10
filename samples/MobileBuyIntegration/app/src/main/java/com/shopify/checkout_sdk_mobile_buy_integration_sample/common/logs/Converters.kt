package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import androidx.room.TypeConverter
import com.shopify.checkoutsheetkit.lifecycleevents.OrderDetails
import com.shopify.checkoutsheetkit.pixelevents.AlertDisplayedPixelEventData
import com.shopify.checkoutsheetkit.pixelevents.Checkout
import com.shopify.checkoutsheetkit.pixelevents.CheckoutPixelEventData
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.Context
import com.shopify.checkoutsheetkit.pixelevents.PageViewedPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.UIExtensionErroredPixelEventData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun checkoutPixelEventToString(value: CheckoutPixelEventData): String {
        return Json.encodeToString<CheckoutPixelEventData>(value)
    }
    @TypeConverter
    fun stringToCheckoutPixelEventData(value: String): CheckoutPixelEventData {
        return Json.decodeFromString<CheckoutPixelEventData>(value)
    }

    @TypeConverter
    fun alertDisplayedPixelEventDataToString(value: AlertDisplayedPixelEventData): String {
        return Json.encodeToString<AlertDisplayedPixelEventData>(value)
    }

    @TypeConverter
    fun stringToAlertDisplayedPixelEventData(value: String): AlertDisplayedPixelEventData {
        return Json.decodeFromString<AlertDisplayedPixelEventData>(value)
    }

    @TypeConverter
    fun uiExtensionErroredPixelEventDataToString(value: UIExtensionErroredPixelEventData): String {
        return Json.encodeToString<UIExtensionErroredPixelEventData>(value)
    }

    @TypeConverter
    fun stringToUIExtensionErroredPixelEventData(value: String): UIExtensionErroredPixelEventData {
        return Json.decodeFromString<UIExtensionErroredPixelEventData>(value)
    }

    @TypeConverter
    fun contextToString(value: Context): String {
        return Json.encodeToString<Context>(value)
    }

    @TypeConverter
    fun stringToContext(value: String): Context {
        return Json.decodeFromString<Context>(value)
    }

    @TypeConverter
    fun orderDetailsToString(value: OrderDetails): String {
        return Json.encodeToString<OrderDetails>(value)
    }

    @TypeConverter
    fun stringToOrderDetails(value: String): OrderDetails {
        return Json.decodeFromString<OrderDetails>(value)
    }
}

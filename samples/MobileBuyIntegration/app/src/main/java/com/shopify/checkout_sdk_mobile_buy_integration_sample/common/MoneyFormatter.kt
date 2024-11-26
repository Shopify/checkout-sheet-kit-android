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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

import android.content.Context
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import java.util.Locale

fun toDisplayText(context: Context, currencyCode: String, price: Double, locale: Locale, includeSuffix: Boolean): String {
    if (price == 0.0) {
        return context.resources.getString(R.string.free)
    }

    return price.toTwoDecimalPlaceString(context, locale, currencyCode, includeSuffix)
}

fun toDisplayText(
    context: Context,
    fromPrice: Double,
    fromCurrencyCode: String,
    toPrice: Double,
    toCurrencyCode: String,
    locale: Locale,
    includeSuffix: Boolean
): String {
    if (fromPrice == toPrice && fromCurrencyCode == toCurrencyCode) {
        return toDisplayText(context, toCurrencyCode, toPrice, locale, includeSuffix)
    }

    val fromPriceString = fromPrice.toTwoDecimalPlaceString(context, locale, fromCurrencyCode, includeSuffix)
    val toPriceString = toPrice.toTwoDecimalPlaceString(context, locale, toCurrencyCode, includeSuffix)

    return "$fromPriceString - $toPriceString"
}

private fun Double.toTwoDecimalPlaceString(
    context: Context,
    locale: Locale,
    currencyCode: String,
    includeSuffix: Boolean,
): String {
    if (this == 0.0) {
        return context.resources.getString(R.string.free)
    } else {
        val displayText = "${currencyCode.toSymbol()}${this.toTwoDecimalString(locale)}"
        if (includeSuffix) {
            return "$displayText $currencyCode"
        }
        return displayText
    }
}

private fun String.toSymbol(): String {
    return when (this) {
        "GBP" -> "£"
        "USD", "CAD" -> "$"
        "EUR" -> "€"
        else -> {
            "$this "
        }
    }
}

private fun Double.toTwoDecimalString(locale: Locale) =
    String.format(locale, "%,.2f", this)

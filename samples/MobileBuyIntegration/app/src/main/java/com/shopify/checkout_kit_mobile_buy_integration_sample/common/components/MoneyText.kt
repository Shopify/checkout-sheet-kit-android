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
package com.shopify.checkout_kit_mobile_buy_integration_sample.common.components

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.shopify.checkout_kit_mobile_buy_integration_sample.R
import java.util.Locale

@Composable
fun MoneyText(
    currency: String,
    price: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    includeSuffix: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales.get(0)

    Text(
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        text = toDisplayText(
            LocalContext.current,
            currency,
            price,
            locale,
            includeSuffix,
        )
    )
}

@Composable
fun MoneyRangeText(
    fromPrice: Double,
    fromCurrencyCode: String,
    toPrice: Double,
    toCurrencyCode: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    includeSuffix: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales.get(0)

    Text(
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        text = toDisplayText(
            LocalContext.current,
            fromPrice,
            fromCurrencyCode,
            toPrice,
            toCurrencyCode,
            locale,
            includeSuffix,
        )
    )
}

private fun toDisplayText(context: Context, currencyCode: String, price: Double, locale: Locale, includeSuffix: Boolean): String {
    if (price == 0.0) {
        return context.resources.getString(R.string.free)
    }

    return price.toTwoDecimalPlaceString(context, locale, currencyCode, includeSuffix)
}

private fun toDisplayText(
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

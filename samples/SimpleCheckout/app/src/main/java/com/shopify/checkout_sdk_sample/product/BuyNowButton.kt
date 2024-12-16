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
package com.shopify.checkout_sdk_sample.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import java.util.Locale

@Composable
fun BuyNowButton(
    price: Double,
    currency: String,
    loading: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(.7f),
            enabled = !loading,
            onClick = onClick,
        ) {
            Box {
                if (loading) {
                    CircularProgressIndicator(
                        Modifier
                            .size(26.dp)
                            .offset(x = -(32.dp), y = 6.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "Buy Now",
                    )
                    Text(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        text = toDisplayText(currency, price, locale)
                    )
                }
            }
        }
    }
}

fun toDisplayText(currencyCode: String, price: Double, locale: Locale?): String {
    return "${currencyCode.toSymbol()}${price.toTwoDecimalString(locale)}"
}

private fun String.toSymbol(): String {
    return when (this) {
        "GBP" -> "£"
        "USD" -> "$"
        "EUR" -> "€"
        else -> {
            "$this "
        }
    }
}

private fun Double.toTwoDecimalString(locale: Locale?) = String.format(locale, "%,.2f", this)

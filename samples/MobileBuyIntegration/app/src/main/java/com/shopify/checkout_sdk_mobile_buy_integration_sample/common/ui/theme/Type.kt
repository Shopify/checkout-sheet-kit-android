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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R

val cooperBTFontFamily = FontFamily(
    Font(R.font.cooper_bt_normal_200, FontWeight.Light),
    Font(R.font.cooper_bt_normal_500, FontWeight.Normal),
    Font(R.font.cooper_bt_normal_700, FontWeight.Medium),
    Font(R.font.cooper_bt_normal_900, FontWeight.Bold),
    Font(R.font.cooper_bt_italic_200, FontWeight.Light, FontStyle.Italic),
    Font(R.font.cooper_bt_italic_500, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.cooper_bt_italic_700, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.cooper_bt_italic_900, FontWeight.Bold, FontStyle.Italic),
)

val typography = Typography(
    titleLarge = TextStyle(
        fontFamily = cooperBTFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 52.sp,
        textAlign = TextAlign.Center,
    ),
    titleMedium = TextStyle(
        fontFamily = cooperBTFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Normal,
    ),
    titleSmall = TextStyle(
        fontFamily = cooperBTFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = cooperBTFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = cooperBTFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)

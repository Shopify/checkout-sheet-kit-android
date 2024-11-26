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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3

@Composable
fun LogOverviewHeader(modifier: Modifier) {
    Row(modifier) {
        Header3(
            text = "Date",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            modifier = Modifier.weight(Logs.DATE_COLUMN_WEIGHT),
        )
        Header3(
            text = "Type",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            modifier = Modifier.weight(Logs.MESSAGE_COLUMN_WEIGHT),
        )
    }
}

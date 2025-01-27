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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantOptionDetails

@Composable
fun OptionSelector(
    availableOptions: Map<String, List<ProductVariantOptionDetails>>,
    selectedOptions: Map<String, String>,
    onSelected: (name: String, value: String) -> Unit,
) {
    availableOptions.forEach { (optionName, optionValues) ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            BodySmall(optionName)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                optionValues.forEach { optionDetails ->
                    val isSelected = selectedOptions[optionName] == optionDetails.name

                    OutlinedButton(
                        onClick = { onSelected(optionName, optionDetails.name) },
                        enabled = optionDetails.availableForSale,
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                        } else {
                            ButtonDefaults.outlinedButtonColors(containerColor = Color.Unspecified)
                        }
                    ) {
                        BodyMedium(
                            optionDetails.name,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                            textDecoration = if (optionDetails.availableForSale) TextDecoration.None else TextDecoration.LineThrough,
                        )
                    }
                }
            }
        }
    }
}

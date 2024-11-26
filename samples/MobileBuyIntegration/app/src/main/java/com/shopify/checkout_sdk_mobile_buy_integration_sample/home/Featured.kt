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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeight
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeightLg
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.CollectionProduct
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProduct

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Featured(
    featured: List<UIProduct>,
    imageHeight: Dp = defaultProductImageHeight,
    imageHeightLg: Dp = defaultProductImageHeightLg,
    verticalPadding: Dp = 30.dp,
    verticalSpacing: Dp = 30.dp,
    horizontalSpacing: Dp = 5.dp,
    onProductClick: (id: String) -> Unit,
) {
    if (featured.isEmpty()) {
        Text("No products configured for this collection")
    } else {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 15.dp, end = 15.dp, top = 30.dp)
                .fillMaxSize()
        ) {

            Header3(
                resourceId = R.string.featured_title,
                modifier = Modifier,
                color = MaterialTheme.colorScheme.onPrimary
            )

            FlowRow(
                maxItemsInEachRow = 2,
                maxLines = 4,
                modifier = Modifier.padding(vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                featured.forEach { featuredProduct ->
                    CollectionProduct(
                        product = featuredProduct,
                        imageHeight = imageHeight,
                        imageHeightLg = imageHeightLg,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyText
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeight
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeightLg
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.largeScreenBreakpoint
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product

@Composable
fun CollectionProduct(
    product: Product,
    onProductClick: (id: String) -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    imageHeight: Dp = defaultProductImageHeight,
    imageHeightLg: Dp = defaultProductImageHeightLg,
) {
    BoxWithConstraints {
        val largeScreen = maxWidth >= largeScreenBreakpoint
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
            .fillMaxWidth(.49f)
            .clickable {
                onProductClick(product.id)
            }) {

            RemoteImage(
                url = product.image?.url,
                altText = product.image?.altText ?: stringResource(id = R.string.featured_default_alt_text),
                modifier = Modifier
                    .height(if (largeScreen) imageHeightLg else imageHeight)
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            MoneyText(
                currency = product.priceRange.maxVariantPrice.currencyCode,
                price = product.priceRange.maxVariantPrice.amount,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

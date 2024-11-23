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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage

@Composable
fun Collections(
    collections: List<Storefront.Collection>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        Modifier
            .fillMaxHeight()
            .padding(horizontal = 15.dp)
    ) {
        Header2(
            resourceId = R.string.collections_title,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 20.dp),
        )

        if (collections.isEmpty()) {
            Text(stringResource(id = R.string.collections_no_collections_configured))
        } else {
            collections.forEach { collection ->
                Collection(
                    handle = collection.handle,
                    title = collection.title,
                    image = collection.image,
                    modifier = modifier,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun Collection(
    handle: String,
    title: String,
    image: Storefront.Image,
    onClick: (String) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = Modifier
        .padding(bottom = 20.dp)
        .clickable {
            onClick(handle)
        }) {
        RemoteImage(
            url = image.url,
            altText = image.altText ?: stringResource(id = R.string.collection_img_alt_default),
            modifier = modifier
                .defaultMinSize(minWidth = 345.dp, minHeight = 345.dp)
                .fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Header3(
                text = title,
                modifier = Modifier.padding(top = 10.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.collection_cta_content_description),
                modifier = Modifier.padding(start = 5.dp, top = 10.dp)
            )
        }
    }
}

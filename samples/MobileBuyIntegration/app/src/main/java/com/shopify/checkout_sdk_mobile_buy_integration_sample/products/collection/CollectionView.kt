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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionView(
    navController: NavController,
    collectionHandle: String,
    collectionViewModel: CollectionViewModel = koinViewModel(),
) {
    LaunchedEffect(key1 = true) {
        collectionViewModel.fetchCollection(collectionHandle)
    }

    val collectionUIState = collectionViewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (collectionUIState) {
            is CollectionUIState.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is CollectionUIState.Error -> {
                Text(collectionUIState.error)
            }

            is CollectionUIState.Loaded -> {
                Column(
                    Modifier
                        .padding(start = 15.dp, end = 15.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val collection = collectionUIState.collection
                    Header2(text = collection.title)
                    BodyMedium(text = collection.description)
                    RemoteImage(url = collection.image.url, altText = collection.image.altText ?: "", modifier = Modifier)

                    FlowRow(
                        maxItemsInEachRow = 2,
                        maxLines = 4,
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(30.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        collection.products.nodes.forEach { collectionProduct ->
                            CollectionProduct(
                                product = collectionProduct,
                                imageHeight = 250.dp,
                                textColor = MaterialTheme.colorScheme.onBackground,
                                onProductClick = { productId -> collectionViewModel.productSelected(navController, productId) }
                            )
                        }
                    }
                }
            }
        }
    }

}

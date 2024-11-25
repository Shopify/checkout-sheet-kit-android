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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyText
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeight
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.defaultProductImageHeightLg
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.horizontalPadding
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.largeScreenBreakpoint
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.verticalPadding
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProduct
import com.shopify.graphql.support.ID
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductsView(
    navController: NavController,
    productsViewModel: ProductsViewModel = koinViewModel(),
) {
    LaunchedEffect(key1 = true) {
        productsViewModel.fetchProducts()
    }

    val productsUIState = productsViewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (productsUIState) {
            is ProductsUIState.Loading -> {
                ProgressIndicator()
            }

            is ProductsUIState.Error -> {
                Text(productsUIState.error)
            }

            is ProductsUIState.Loaded -> {
                Column(
                    Modifier
                        .padding(horizontal = horizontalPadding)
                        .fillMaxSize()
                ) {
                    BoxWithConstraints {
                        val largeScreen = maxWidth >= largeScreenBreakpoint


                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(30.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                                Header2(
                                    modifier = Modifier.padding(top = verticalPadding),
                                    text = stringResource(id = R.string.products_header)
                                )
                            }

                            items(productsUIState.products.size) { i ->
                                Product(
                                    product = productsUIState.products[i],
                                    imageHeight = if (largeScreen) defaultProductImageHeightLg else defaultProductImageHeight,
                                    onProductClick = { productId ->
                                        productsViewModel.productClicked(navController, productId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun Product(
    product: UIProduct,
    imageHeight: Dp,
    onProductClick: (id: ID) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
        .wrapContentWidth()
        .clickable {
            onProductClick(product.id)
        }) {
        RemoteImage(
            url = product.image.url,
            altText = product.image.altText,
            modifier = Modifier
                .height(imageHeight)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
        Text(product.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        MoneyText(
            currency = product.variants.first().currencyName,
            price = product.variants.first().price.toDouble(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }

}

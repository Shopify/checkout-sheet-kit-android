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
package com.shopify.checkout_kit_mobile_buy_integration_sample.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_kit_mobile_buy_integration_sample.products.collection.ProductCollections
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeView(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel()
) {

    LaunchedEffect(key1 = true) {
        homeViewModel.fetchHomePageData()
    }

    val homeUiState = homeViewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (homeUiState == HomeUIState.Loading) {
            ProgressIndicator()
        }

        Hero(onClickShopAll = { homeViewModel.shopAll(navController) })

        when (homeUiState) {
            is HomeUIState.Loading -> {
                // Do nothing, ProgressIndicator appears above hero
            }

            is HomeUIState.Error -> {
                Text(homeUiState.error)
            }

            is HomeUIState.Loaded -> {
                ProductCollections(
                    productCollections = homeUiState.productCollections,
                    onClick = { collectionHandle -> homeViewModel.productCollectionSelected(navController, collectionHandle) }
                )
                Featured(homeUiState.productCollections.firstOrNull()?.products ?: emptyList()) { productId: ID ->
                    homeViewModel.productSelected(navController, productId)
                }
            }
        }
    }
}

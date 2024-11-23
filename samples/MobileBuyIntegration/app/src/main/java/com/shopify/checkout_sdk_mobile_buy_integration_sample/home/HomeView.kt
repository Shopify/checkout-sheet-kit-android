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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.graphql.support.ID
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder

@Composable
fun HomeView(
    navController: NavHostController,
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
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Hero(onClickShopAll = {
            navController.navigate(Screen.Products.route)
        })

        when (homeUiState) {
            is HomeUIState.Loading -> {
                // Do nothing, Linear Progress Indicator appears above hero
            }

            is HomeUIState.Error -> {
                Text(homeUiState.error)
            }

            is HomeUIState.Loaded -> {
                Collections(
                    collections = homeUiState.collections,
                    onClick = { collectionHandle ->
                        navController.navigate(Screen.Collection.route(collectionHandle))
                    }
                )
                Featured(homeUiState.collections.firstOrNull()?.products?.nodes ?: emptyList()) { productId: ID ->
                    navController.navigate(Screen.Product.route.replace("{productId}", URLEncoder.encode(productId.toString(), "UTF-8")))
                }
            }
        }
    }
}

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

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network.ProductCollectionsStorefrontApiClient
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class HomeViewModel(
    private val client: ProductCollectionsStorefrontApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    fun fetchHomePageData() {
        Timber.i("Fetching home page data")
        client.fetchProductCollections(numCollections = NUM_COLLECTIONS, numProducts = NUM_PRODUCTS_PER_COLLECTION,
            { success ->
                val productCollections = success.data?.collections?.nodes
                Timber.i("Home page data fetched, retrieved ${productCollections?.size ?: 0} product collections")
                _uiState.value = HomeUIState.Loaded(
                    productCollections = productCollections ?: emptyList(),
                )
            },
            { failure ->
                Timber.e("Failed to fetch product collections $failure")
                _uiState.value = HomeUIState.Error(failure.message ?: "Unknown")
            }
        )
    }

    fun shopAll(navController: NavController) {
        Timber.i("Shop all clicked, navigating to products")
        navController.navigate(Screen.Products.route)
    }

    fun productCollectionSelected(navController: NavController, collectionHandle: String) {
        Timber.i("Product collection selected, navigating to $collectionHandle")
        navController.navigate(Screen.ProductCollection.route(collectionHandle))
    }

    fun productSelected(navController: NavController, productId: ID) {
        Timber.i("Product selected $productId, navigating to product page")
        navController.navigate(Screen.Product.route(productId.toString()))
    }

    companion object {
        private const val NUM_COLLECTIONS = 2
        private const val NUM_PRODUCTS_PER_COLLECTION = 8
    }
}

sealed class HomeUIState {
    data object Loading : HomeUIState()
    data class Error(val error: String) : HomeUIState()
    data class Loaded(
        val productCollections: List<Storefront.Collection>,
    ) : HomeUIState()
}

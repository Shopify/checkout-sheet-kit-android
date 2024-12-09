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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.ProductCollection
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.ProductCollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URLEncoder

class ProductCollectionViewModel(
    private val productCollectionRepository: ProductCollectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductCollectionUIState>(ProductCollectionUIState.Loading)
    val uiState: StateFlow<ProductCollectionUIState> = _uiState.asStateFlow()

    fun fetchCollection(handle: String) = viewModelScope.launch {
        Timber.i("Fetching collection with handle: $handle")
        try {
            val collection = productCollectionRepository.getProductCollection(handle, numberOfProducts = 10)

            Timber.i("Fetching collection complete")
            _uiState.value = ProductCollectionUIState.Loaded(productCollection = collection)
        } catch (e: Exception) {
            Timber.e("Fetching collection failed $e")
            SnackbarController.sendEvent(SnackbarEvent(R.string.collection_failed_to_load))
            _uiState.value = ProductCollectionUIState.Error("Failed to fetch collection")
        }
    }

    fun productSelected(navController: NavController, productId: ID) {
        Timber.i("Product $productId selected, navigation to product page")
        val encodedId = URLEncoder.encode(productId.id, "UTF-8")
        navController.navigate(Screen.Product.route.replace("{productId}", encodedId))
    }
}

sealed class ProductCollectionUIState {
    data object Loading : ProductCollectionUIState()
    data class Error(val error: String) : ProductCollectionUIState()
    data class Loaded(val productCollection: ProductCollection) : ProductCollectionUIState()
}

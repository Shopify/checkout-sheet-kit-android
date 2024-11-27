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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.Collection
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URLEncoder

class CollectionViewModel(
    private val collectionRepository: CollectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState.asStateFlow()

    fun fetchCollection(handle: String) {
        Timber.i("Fetching collection with handle: $handle")
        viewModelScope.launch {
            collectionRepository.getCollection(handle, numberOfProducts = 10)
                .catch { exception ->
                    Timber.e("Fetching collection failed $exception")
                    _uiState.value = CollectionUIState.Error("Failed to fetch collection")
                }
                .collect { collection ->
                    Timber.i("Fetching collection complete")
                    _uiState.value = CollectionUIState.Loaded(collection = collection)
                }
        }

    }

    fun productSelected(navController: NavController, productId: String) {
        Timber.i("Product $productId selected, navigation to product page")
        val encodedId = URLEncoder.encode(productId, "UTF-8")
        navController.navigate(Screen.Product.route.replace("{productId}", encodedId))
    }
}

sealed class CollectionUIState {
    data object Loading : CollectionUIState()
    data class Error(val error: String) : CollectionUIState()
    data class Loaded(val collection: Collection) : CollectionUIState()
}

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
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val client: StorefrontClient) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    fun fetchHomePageData() {
        client.fetchHomePageData(numCollections = NUM_COLLECTIONS, numProducts = NUM_PRODUCTS_PER_COLLECTION,
            { success ->
                val collections = success.data?.collections?.nodes
                _uiState.value = HomeUIState.Loaded(
                    collections = collections ?: emptyList(),
                )
            },
            { failure ->
                _uiState.value = HomeUIState.Error(failure.message ?: "Unknown")
            }
        )
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
        val collections: List<Storefront.Collection>,
    ) : HomeUIState()
}

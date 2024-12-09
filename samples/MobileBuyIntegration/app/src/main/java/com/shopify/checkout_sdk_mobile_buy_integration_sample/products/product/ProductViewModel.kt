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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductRepository
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProductViewModel(
    private val cartViewModel: CartViewModel,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    fun setAddQuantityAmount(quantity: Int) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Loaded) {
            Timber.i("Updating addQuantityAmount to $quantity")
            _uiState.value = currentState.copy(addQuantityAmount = quantity)
        }
    }

    fun addToCart() {
        val state = _uiState.value
        if (state is ProductUIState.Loaded && state.product.variants != null && state.product.selectedVariant != null) {
            val selectedVariantId = state.product.variants[state.product.selectedVariant].id
            val quantity = state.addQuantityAmount
            setIsAddingToCart(true)
            cartViewModel.addToCart(selectedVariantId, quantity) {
                setIsAddingToCart(false)
            }
        }
    }

    fun fetchProduct(productId: ID) = viewModelScope.launch {
        Timber.i("Fetching product with id $productId")
        try {
            val product = productRepository.getProduct(productId)
            Timber.i("Fetching product complete $product")
            _uiState.value = ProductUIState.Loaded(
                product = product,
                isAddingToCart = false,
                addQuantityAmount = 1
            )
        } catch (e: Exception) {
            Timber.e("Fetching product failed $e")
            _uiState.value = ProductUIState.Error(e.message ?: "Unknown error")
        }
    }

    private fun setIsAddingToCart(value: Boolean) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Loaded) {
            Timber.i("isAddingToCart - $value")
            _uiState.value = currentState.copy(isAddingToCart = value)
        }
    }
}

sealed class ProductUIState {
    data object Loading : ProductUIState()
    data class Error(val error: String) : ProductUIState()
    data class Loaded(val product: Product, val isAddingToCart: Boolean, val addQuantityAmount: Int) : ProductUIState()
}

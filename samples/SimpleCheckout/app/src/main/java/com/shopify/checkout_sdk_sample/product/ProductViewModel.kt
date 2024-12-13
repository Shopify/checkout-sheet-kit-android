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
package com.shopify.checkout_sdk_sample.product

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_sample.data.CartRepository
import com.shopify.checkout_sdk_sample.data.Product
import com.shopify.checkout_sdk_sample.data.ProductRepository
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()
    private var eventProcessor: DefaultCheckoutEventProcessor? = null

    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    private val _checkoutState = MutableStateFlow<CurrentCheckoutState?>(null)
    val checkoutState = _checkoutState.asStateFlow()

    /**
     * Fetch potential products to be displayed and display a random product from the list
     */
    fun fetchProducts() = viewModelScope.launch {
        val products = productRepository.find(count = 30, variantCount = 1)
        if (products.isNotEmpty()) {
            _uiState.value = ProductUIState.Loaded(product = products.random(), isAddingToCart = false)
        } else {
            _uiState.value = ProductUIState.Error("Failed to fetch products")
        }
    }

    /**
     * Sets the event processor that handles events that may occur during checkout
     * onCheckoutStarted, onCheckoutCompleted, onCheckoutCanceled etc.
     */
    fun setEventProcessor(activity: ComponentActivity) {
        eventProcessor = object : DefaultCheckoutEventProcessor(activity) {
            override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
                checkoutCompleted()
            }

            override fun onCheckoutFailed(error: CheckoutException) {
                checkoutFailed(error)
            }

            override fun onCheckoutCanceled() {
                checkoutCanceled()
            }
        }
    }

    /**
     * Creates a cart with the selected product variant and quantity 1, and presents it to the buyer
     */
    fun checkout(activity: ComponentActivity, variantId: String) = viewModelScope.launch {
        when (val currentState = _uiState.value) {
            is ProductUIState.Loaded -> {
                _uiState.value = currentState.copy(isAddingToCart = true)
                val cart = cartRepository.create(variantId)
                val processor = eventProcessor
                if (cart != null && processor != null) {
                    Handler(Looper.getMainLooper()).post {
                        ShopifyCheckoutSheetKit.present(cart.checkoutUrl.toString(), activity, processor)
                    }
                }
                _uiState.value = currentState.copy(isAddingToCart = false)
            }
            else -> {
                Log.e("ProductViewModel", "checkout() called in error or loading state")
            }
        }
    }

    fun clearCheckoutState() {
        _checkoutState.value = null
    }

    fun checkoutCompleted() {
        _checkoutState.value = CurrentCheckoutState.COMPLETE
    }

    fun checkoutFailed(error: CheckoutException) {
        Log.e("ProductViewModel", "Error occurred during checkout", error)
        _checkoutState.value = CurrentCheckoutState.ERROR
    }

    fun checkoutCanceled() {
        _checkoutState.value = CurrentCheckoutState.CANCELLED
    }
}

sealed class ProductUIState {
    data object Loading : ProductUIState()
    data class Error(val error: String) : ProductUIState()
    data class Loaded(val product: Product, val isAddingToCart: Boolean) : ProductUIState()
}

enum class CurrentCheckoutState {
    CANCELLED, ERROR, COMPLETE
}

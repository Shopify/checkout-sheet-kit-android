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
package com.shopify.checkout_sdk_sample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkoutsheetkit.CheckoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    private val _checkoutState = MutableStateFlow<CurrentCheckoutState?>(null)
    val checkoutState = _checkoutState.asStateFlow()

    fun createCart(variantId: String, callback: (MutationRoot) -> Unit) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Product) {
            _uiState.value = currentState.copy(isAddingToCart = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            client.createCart(variantId, successCallback = { result ->
                callback.invoke(result)
                if (currentState is ProductUIState.Product) {
                    _uiState.value = currentState.copy(isAddingToCart = false)
                }
            })
        }
    }

    private val client = StorefrontClient()

    private fun buildProducts(products: Connection<Product>): List<UIProduct> {
        return products.nodes.map { product ->
            val variants = product.variants
            val firstVariant = variants.nodes.first()
            UIProduct(
                title = product.title,
                vendor = product.vendor,
                description = product.description,
                image = if (product.featuredImage == null) UIProductImage() else UIProductImage(
                    width = product.featuredImage.width,
                    height = product.featuredImage.height,
                    url = product.featuredImage.url,
                    altText = product.featuredImage.altText ?: "Product image",
                ),
                variants = mutableListOf(
                    UIProductVariant(
                        id = firstVariant.id,
                        price = firstVariant.price.amount,
                        currencyName = firstVariant.price.currencyCode,
                    )
                )
            )
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchProducts()
        }
    }

    private fun fetchProducts() {
        client.fetchFirstNProducts(
            numProducts = 30,
            numVariants = 1,
            successCallback = {
                val products = it.products
                val uiProducts = buildProducts(products)
                _uiState.value =
                    ProductUIState.Product(product = uiProducts.random(), isAddingToCart = false)
            },
            failureCallback = {
                _uiState.value = ProductUIState.Error(it.message ?: "Unknown error")
            }
        )
    }

    fun clearCheckoutState() {
        _checkoutState.value = null
    }

    fun checkoutCompleted() {
        _checkoutState.value = CurrentCheckoutState.COMPLETE
    }

    fun checkoutFailed(error: CheckoutException, isRecoverable: Boolean) {
        Log.e("ProductViewModel", "Error occurred during checkout (isRecoverable $isRecoverable)", error)
        _checkoutState.value = CurrentCheckoutState.ERROR
    }

    fun checkoutCanceled() {
        _checkoutState.value = CurrentCheckoutState.CANCELLED
    }
}

sealed class ProductUIState {
    data object Loading : ProductUIState()
    data class Error(val error: String) : ProductUIState()
    data class Product(val product: UIProduct, val isAddingToCart: Boolean) : ProductUIState()
}

data class UIProduct(
    val title: String = "",
    val vendor: String = "",
    val description: String = "",
    val image: UIProductImage = UIProductImage(),
    val selectedVariant: Int = 0,
    val variants: MutableList<UIProductVariant> = mutableListOf(UIProductVariant())
)

data class UIProductVariant(
    val price: String = "",
    val currencyName: String = "",
    val id: String = "",
)

data class UIProductImage(
    val width: Int = 0,
    val height: Int = 0,
    val altText: String = "",
    val url: String = "",
)

enum class CurrentCheckoutState {
    CANCELLED, ERROR, COMPLETE
}

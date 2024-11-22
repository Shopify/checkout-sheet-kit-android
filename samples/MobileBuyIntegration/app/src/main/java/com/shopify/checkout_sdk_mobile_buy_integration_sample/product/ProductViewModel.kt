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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.product

import androidx.lifecycle.ViewModel
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class ProductViewModel(private val client: StorefrontClient) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    fun setAddQuantityAmount(quantity: Int) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Product) {
            Timber.i("Updating state in setAddQuantityAmount(), setting quantity=$quantity")
            _uiState.value = currentState.copy(addQuantityAmount = quantity)
        }
    }

    fun setIsAddingToCart(value: Boolean) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Product) {
            Timber.i("Updating state in setIsAddingToCart(), setting isAddingToCart to $value")
            _uiState.value = currentState.copy(isAddingToCart = value)
        }
    }

    fun fetchProduct(productId: ID) {
        if (_uiState.value is ProductUIState.Loading) {
            client.fetchProduct(
                productId = productId,
                numVariants = 1,
                successCallback = {
                    val product = it.data?.product as Storefront.Product
                    val uiProduct = buildProduct(product)
                    Timber.i("Fetched product, setting in state $product")
                    _uiState.value =
                        ProductUIState.Product(product = uiProduct, isAddingToCart = false, addQuantityAmount = 1)
                },
                failureCallback = {
                    _uiState.value = ProductUIState.Error(it.message ?: "Unknown error")
                }
            )
        }
    }

    private fun buildProduct(product: Storefront.Product): UIProduct {
        val variants = product.variants as Storefront.ProductVariantConnection
        val firstVariant = variants.nodes.first()
        return UIProduct(
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
                    currencyName = firstVariant.price.currencyCode.name,
                )
            )
        )
    }
}

sealed class ProductUIState {
    data object Loading : ProductUIState()
    data class Error(val error: String) : ProductUIState()
    data class Product(val product: UIProduct, val isAddingToCart: Boolean, val addQuantityAmount: Int) : ProductUIState()
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
    val id: ID = ID(""),
)

data class UIProductImage(
    val width: Int = 0,
    val height: Int = 0,
    val altText: String = "",
    val url: String = "",
)

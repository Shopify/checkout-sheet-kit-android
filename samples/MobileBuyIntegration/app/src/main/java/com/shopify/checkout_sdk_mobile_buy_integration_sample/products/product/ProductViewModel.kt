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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariant
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantOptionDetails
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantSelectedOption
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID as CommonID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProductViewModel(
    private val cartViewModel: CartViewModel,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val preferencesManager: PreferencesManager,
    private val customerRepository: CustomerRepository,
) : ViewModel() {
    private var demoBuyerIdentityEnabled = false

    init {
        // Track buyer identity setting for buy now carts
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect {
                demoBuyerIdentityEnabled = it.buyerIdentityDemoEnabled
            }
        }
    }
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
        if (state is ProductUIState.Loaded) {
            val quantity = state.addQuantityAmount
            setIsAddingToCart(true)
            cartViewModel.addToCart(state.selectedVariant.id, quantity) {
                setIsAddingToCart(false)
            }
        }
    }

    fun buyNow(onCartCreated: (String) -> Unit) {
        val state = _uiState.value
        if (state is ProductUIState.Loaded) {
            val variantId = state.selectedVariant.id
            val quantity = state.addQuantityAmount
            setIsAddingToCart(true)
            viewModelScope.launch {
                try {
                    val checkoutUrl = createBuyNowCart(variantId, quantity)
                    setIsAddingToCart(false)
                    onCartCreated(checkoutUrl)
                } catch (e: Exception) {
                    Timber.e("Failed to create buy now cart: $e")
                    setIsAddingToCart(false)
                }
            }
        }
    }

    private suspend fun createBuyNowCart(variantId: CommonID, quantity: Int): String {
        Timber.i("Creating buy now cart for variant: $variantId with quantity: $quantity")
        val customerAccessToken = customerRepository.getCustomerAccessToken()?.accessToken

        val tempCart = cartRepository.createCart(
            variantId = variantId,
            quantity = quantity,
            demoBuyerIdentityEnabled = demoBuyerIdentityEnabled,
            customerAccessToken = customerAccessToken,
        )

        Timber.i("Buy now cart created with URL: ${tempCart.checkoutUrl}")
        return tempCart.checkoutUrl
    }

    fun fetchProduct(productId: ID) = viewModelScope.launch {
        Timber.i("Fetching product with id $productId")
        try {
            val product = productRepository.getProduct(productId)
            Timber.i("Fetching product complete $product")
            val selectedVariant = product.variants.first()
            _uiState.value = ProductUIState.Loaded(
                product = product,
                selectedVariant = selectedVariant,
                availableOptions = buildAvailableOptions(product, selectedVariant),
                isAddingToCart = false,
                addQuantityAmount = 1
            )
        } catch (e: Exception) {
            Timber.e("Fetching product failed $e")
            _uiState.value = ProductUIState.Error(e.message ?: "Unknown error")
        }
    }

    // Select a new variant option (e.g. size = large)
    fun updateSelectedOption(name: String, value: String) {
        val state = _uiState.value
        if (state is ProductUIState.Loaded) {
            val matchingVariant = state.product.variants.first { variant ->
                variant.selectedOptions.containsAll(newOptions(state.selectedVariant, name, value))
            }
            matchingVariant.let {
                _uiState.value = state.copy(
                    selectedVariant = it,
                    availableOptions = buildAvailableOptions(
                        product = state.product,
                        selectedVariant = it
                    )
                )
            }
        }
    }

    // Returns variant options for the product, and whether the option is available for sale (when combined with other options on the
    // currently selected variant) e.g. { "size": [{"large", true}, {"medium", false}], "color": [{"red", true}, {"blue", false}]}
    private fun buildAvailableOptions(
        product: Product,
        selectedVariant: ProductVariant
    ): Map<String, List<ProductVariantOptionDetails>> {
        // Only return available options if more than one option exists
        if (product.variants.size == 1) {
            return emptyMap()
        }

        val options = product.variants
            .flatMap { it.selectedOptions }
            .distinctBy { it.value }

        return options.associateBy(
            { it.name },
            { selectedOption ->
                options.filter { it.name == selectedOption.name }.map { option ->
                    ProductVariantOptionDetails(
                        name = option.value,
                        availableForSale = product.variants.find {
                            it.selectedOptions.containsAll(
                                newOptions(selectedVariant, selectedOption.name, option.value)
                            )
                        }?.availableForSale ?: false,
                    )
                }
            })
    }

    // Modifies the options for the selected variant (e.g: [size: large, color: red]) by replacing one with a new option (e.g. color: blue)
// to return e.g. [size: large, color: blue]
    private fun newOptions(
        selectedVariant: ProductVariant,
        name: String,
        value: String
    ): List<ProductVariantSelectedOption> =
        selectedVariant.selectedOptions
            .filter { it.name != name }
            .plus(ProductVariantSelectedOption(name, value))

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
    data class Loaded(
        val product: Product,
        val selectedVariant: ProductVariant,
        val availableOptions: Map<String, List<ProductVariantOptionDetails>>,
        val isAddingToCart: Boolean,
        val addQuantityAmount: Int
    ) : ProductUIState()
}

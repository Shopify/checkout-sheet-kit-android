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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.ProductConnection
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProduct
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProductImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.UIProductVariant
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class ProductsViewModel(
    private val client: StorefrontClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUIState>(ProductsUIState.Loading)
    val uiState: StateFlow<ProductsUIState> = _uiState.asStateFlow()

    // TODO pagination
    fun fetchProducts() {
        Timber.i("Fetching products")
        client.fetchProducts(
            numProducts = 20,
            numVariants = 1,
            successCallback = {
                Timber.i("Fetching products complete")
                val productConnection = it.data?.products as ProductConnection
                val uiProducts = buildProducts(productConnection)
                Timber.i("Fetched products")
                _uiState.value =
                    ProductsUIState.Loaded(products = uiProducts)
            },
            failureCallback = { error ->
                Timber.e("Fetching products failed $error")
                _uiState.value = ProductsUIState.Error(error.message ?: "Unknown error")
            }
        )
    }

    fun productClicked(navController: NavController, productId: ID) {
        Timber.i("Navigation to product description page for $productId")
        navController.navigate(Screen.Product.route(productId.toString()))
    }

    private fun buildProducts(productConnection: ProductConnection): List<UIProduct> {
        Timber.i("Mapping ${productConnection.nodes.size} fetched products")
        return productConnection.nodes.map { product ->
            val variants = product.variants as Storefront.ProductVariantConnection
            val firstVariant = variants.nodes.first()
            UIProduct(
                id = product.id,
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
}

sealed class ProductsUIState {
    data object Loading : ProductsUIState()
    data class Error(val error: String) : ProductsUIState()
    data class Loaded(val products: List<UIProduct>) : ProductsUIState()
}

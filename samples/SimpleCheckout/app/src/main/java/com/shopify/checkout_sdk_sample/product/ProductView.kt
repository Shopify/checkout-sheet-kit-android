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

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopify.checkout_sdk_sample.R

@Composable
fun ProductView(
    productViewModel: ProductViewModel = viewModel()
) {

    val activity = LocalContext.current as ComponentActivity

    LaunchedEffect(key1 = true) {
        productViewModel.setEventProcessor(activity)
        productViewModel.fetchProducts()
    }

    val productUIState = productViewModel.uiState.collectAsState().value
    val checkoutState = productViewModel.checkoutState.collectAsState().value

    fun showToast(stringResourceId: Int) = Toast.makeText(activity, activity.getString(stringResourceId), Toast.LENGTH_SHORT).show()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (productUIState) {
            is ProductUIState.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is ProductUIState.Error -> {
                Text(productUIState.error)
            }

            is ProductUIState.Loaded -> {
                val product = productUIState.product
                val selectedVariant = product.variants[product.selectedVariant]
                Column {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f, false)
                    ) {
                        if (productUIState.product.image?.url != null) {
                            ProductImage(
                                url = product.image!!.url,
                                altText = product.image.altText ?: "Product image",
                                modifier = Modifier
                                    .fillMaxHeight(0.5f)
                                    .fillMaxWidth()
                            )
                        }

                        ProductInfo(
                            title = product.title,
                            vendor = product.vendor ?: "",
                            description = product.description ?: ""
                        )
                    }
                    Column {
                        BuyNowButton(
                            price = selectedVariant.price.toDouble(),
                            currency = selectedVariant.currencyName,
                            loading = productUIState.isAddingToCart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(20.dp)
                        ) {
                            productViewModel.clearCheckoutState()
                            productViewModel.checkout(activity, selectedVariant.id)
                        }
                    }
                }
            }
        }

        when (checkoutState) {
            CurrentCheckoutState.ERROR -> showToast(R.string.checkout_failed)
            CurrentCheckoutState.COMPLETE -> showToast(R.string.checkout_complete)
            CurrentCheckoutState.CANCELLED -> showToast(R.string.checkout_canceled)
            else -> {
                // nothing to do
            }
        }
    }
}

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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyText
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.QuantitySelector
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.horizontalPadding
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.largeScreenBreakpoint
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.verticalPadding
import com.shopify.graphql.support.ID
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun ProductView(
    productId: String,
    productViewModel: ProductViewModel = koinViewModel(),
) {

    LaunchedEffect(key1 = true) {
        productViewModel.fetchProduct(ID(productId))
    }

    when (val productUIState = productViewModel.uiState.collectAsState().value) {
        is ProductUIState.Loading -> {
            Timber.i("Product loading showing progress indicator")
            ProgressIndicator()
        }

        is ProductUIState.Error -> {
            Timber.i("Product loading failed showing error")
            Text(productUIState.error)
        }

        is ProductUIState.Loaded -> {
            if (productUIState.isAddingToCart) {
                Timber.i("Showing progress indicator")
                ProgressIndicator()
            }

            val product = productUIState.product
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(vertical = verticalPadding)) {
                    BoxWithConstraints(
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        val modifier = Modifier
                            .wrapContentHeight()
                            .padding(horizontal = 10.dp)

                        RemoteImage(
                            url = product.image?.url,
                            altText = product.image?.altText ?: stringResource(id = R.string.product_alt_text_default),
                            modifier = if (maxWidth < largeScreenBreakpoint) {
                                modifier.fillMaxWidth()
                            } else {
                                modifier.fillMaxWidth(.7f)
                            }
                        )
                    }

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Header2(
                            text = product.title
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            // TODO deal with a variable amount of variants
                            val variant = product.variants!!.first()
                            MoneyText(variant.currencyName, variant.price.toDouble())
                            BodySmall(
                                stringResource(id = R.string.product_taxes_included)
                            )
                        }

                        QuantitySelector(enabled = true, quantity = productUIState.addQuantityAmount) { quantity ->
                            productViewModel.setAddQuantityAmount(quantity)
                        }

                        AddToCartButton(
                            loading = productUIState.isAddingToCart,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            productViewModel.addToCart()
                        }

                        BodyMedium(
                            text = product.description ?: "",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}

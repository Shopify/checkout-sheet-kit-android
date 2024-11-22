package com.shopify.checkout_sdk_mobile_buy_integration_sample.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyAmount
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.product.UIProduct
import com.shopify.graphql.support.ID
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder

@Composable
fun ProductsView(
    navController: NavController,
    productsViewModel: ProductsViewModel = koinViewModel(),
) {
    val verticalPadding: Dp = 30.dp
    val verticalSpacing: Dp = 30.dp
    val horizontalSpacing: Dp = 5.dp

    productsViewModel.fetchProducts()
    val productsUIState = productsViewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (productsUIState) {
            is ProductsUIState.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is ProductsUIState.Error -> {
                Text(productsUIState.error)
            }

            is ProductsUIState.Products -> {
                Column(
                    Modifier
                        .padding(start = 15.dp, end = 15.dp)
                        .fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
                    ) {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            Header2(
                                modifier = Modifier.padding(top = 20.dp),
                                text = stringResource(id = R.string.products_header)
                            )
                        }

                        items(productsUIState.products.size) { i ->
                            Product(
                                product = productsUIState.products[i],
                                imageHeight = 250.dp,
                                onProductClick = { productId ->
                                    navController.navigate(
                                        Screen.Product.route.replace(
                                            "{productId}",
                                            URLEncoder.encode(productId.toString(), "UTF-8")
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Product(
    product: UIProduct,
    imageHeight: Dp,
    onProductClick: (id: ID) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
        .wrapContentWidth()
        .clickable {
            onProductClick(product.id)
        }) {
        RemoteImage(
            url = product.image.url,
            altText = product.image.altText,
            modifier = Modifier
                .height(imageHeight)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
        Text(product.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        MoneyAmount(
            currency = product.variants.first().currencyName,
            price = product.variants.first().price.toDouble(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

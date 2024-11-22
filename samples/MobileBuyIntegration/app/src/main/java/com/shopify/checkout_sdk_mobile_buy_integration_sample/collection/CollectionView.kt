package com.shopify.checkout_sdk_mobile_buy_integration_sample.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.product.FeaturedProduct
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionView(
    navController: NavController,
    collectionHandle: String,
    collectionViewModel: CollectionViewModel = koinViewModel(),
) {
    collectionViewModel.fetchCollection(collectionHandle)
    val collectionUIState = collectionViewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (collectionUIState) {
            is CollectionUIState.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is CollectionUIState.Error -> {
                Text(collectionUIState.error)
            }

            is CollectionUIState.Loaded -> {
                Column(
                    Modifier
                        .padding(start = 15.dp, end = 15.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val collection = collectionUIState.collection
                    Header2(text = collection.title)
                    BodyMedium(text = collection.description)
                    // TODO alt
                    RemoteImage(url = collection.image.url, altText = collection.image.altText ?: "", modifier = Modifier)

                    FlowRow(
                        maxItemsInEachRow = 2,
                        maxLines = 4,
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(30.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        collection.products.nodes.forEach { collectionProduct ->
                            FeaturedProduct(
                                product = collectionProduct,
                                imageHeight = 250.dp,
                                textColor = MaterialTheme.colorScheme.onBackground,
                                onProductClick = { productId ->
                                    val encodedId = URLEncoder.encode(productId.toString(), "UTF-8")
                                    navController.navigate(Screen.Product.route.replace("{productId}", encodedId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}

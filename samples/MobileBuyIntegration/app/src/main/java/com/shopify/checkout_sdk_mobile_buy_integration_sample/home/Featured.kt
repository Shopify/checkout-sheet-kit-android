package com.shopify.checkout_sdk_mobile_buy_integration_sample.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_sdk_mobile_buy_integration_sample.product.FeaturedProduct
import com.shopify.graphql.support.ID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Featured(
    featured: List<Storefront.Product>,
    imageHeight: Dp = 250.dp,
    verticalPadding: Dp = 30.dp,
    verticalSpacing: Dp = 30.dp,
    horizontalSpacing: Dp = 5.dp,
    onProductClick: (id: ID) -> Unit,
) {
    if (featured.isEmpty()) {
        Text("No products configured for this collection")
    } else {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 15.dp, end = 15.dp, top = 30.dp)
                .fillMaxSize()
        ) {

            Header3(
                resourceId = R.string.featured_title,
                modifier = Modifier,
                color = MaterialTheme.colorScheme.onPrimary
            )

            FlowRow(
                maxItemsInEachRow = 2,
                maxLines = 4,
                modifier = Modifier.padding(vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                featured.forEach { featuredProduct ->
                    FeaturedProduct(
                        product = featuredProduct,
                        imageHeight = imageHeight,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

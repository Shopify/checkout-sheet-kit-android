package com.shopify.checkout_sdk_mobile_buy_integration_sample.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.MoneyAmount
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.graphql.support.ID

@Composable
fun FeaturedProduct(
    product: Storefront.Product,
    imageHeight: Dp,
    onProductClick: (id: ID) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
        .wrapContentWidth()
        .clickable {
            onProductClick(product.id)
        }) {
        RemoteImage(
            url = product.featuredImage.url,
            altText = product.featuredImage.altText ?: stringResource(id = R.string.featured_default_alt_text),
            modifier = Modifier
                .height(imageHeight)
                .fillMaxWidth(.49f)
                .align(Alignment.CenterHorizontally)
        )
        Text(product.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
        MoneyAmount(
            currency = product.priceRange.maxVariantPrice.currencyCode.name,
            price = product.priceRange.maxVariantPrice.amount.toDouble(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

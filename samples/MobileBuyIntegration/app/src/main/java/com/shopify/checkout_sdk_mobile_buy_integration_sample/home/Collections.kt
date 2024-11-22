package com.shopify.checkout_sdk_mobile_buy_integration_sample.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import timber.log.Timber

@Composable
fun Collections(
    collections: List<Storefront.Collection>,
    modifier: Modifier = Modifier,
) {
    Column(
        Modifier
            .fillMaxHeight()
            .padding(horizontal = 15.dp)
    ) {
        Header2(
            resourceId = R.string.collections_title,
            modifier = Modifier.padding(vertical = 20.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (collections.isEmpty()) {
            Text("")
        } else {
            collections.forEach { collection ->
                Collection(
                    title = collection.title,
                    image = collection.image,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun Collection(
    title: String,
    image: Storefront.Image,
    modifier: Modifier,
) {
    Column(modifier = Modifier
        .padding(bottom = 20.dp)
        .clickable {
            Timber.i("Collection clicked $title")
        }) {
        RemoteImage(
            url = image.url,
            altText = image.altText ?: stringResource(id = R.string.collection_img_alt_default),
            modifier = modifier
                .defaultMinSize(minWidth = 345.dp, minHeight = 345.dp)
                .fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Header3(
                text = title,
                modifier = Modifier.padding(top = 10.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.collection_cta_content_description),
                modifier = Modifier.padding(start = 5.dp, top = 10.dp)
            )
        }
    }
}

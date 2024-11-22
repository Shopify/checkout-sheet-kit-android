package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale

@Composable
fun RemoteImage(
    url: String,
    altText: String,
    modifier: Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .scale(Scale.FILL)
            .crossfade(true)
            .build(),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        contentDescription = altText,
        modifier = modifier
    )
}

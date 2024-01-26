package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.ROW_COLOR

@Composable
fun LogOverviewHeader(vararg header: Header) {
    Row(
        Modifier
            .background(ROW_COLOR)
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        header.map {
            Text(text = it.text, modifier = Modifier.weight(it.weight))
        }
    }
}

data class Header(
    val text: String,
    val weight: Float,
)

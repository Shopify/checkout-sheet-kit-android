package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    enabled: Boolean,
    quantity: Int,
    setQuantity: (Int) -> Unit
) {
    Row(Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)) {
        TextButton(
            modifier = Modifier.width(40.dp),
            enabled = enabled,
            onClick = { setQuantity(quantity - 1) }) {
            Text("-")
        }
        Text(
            text = "$quantity",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        TextButton(
            modifier = Modifier.width(40.dp),
            enabled = enabled,
            onClick = { setQuantity(quantity + 1) }
        ) {
            Text("+")
        }
    }
}

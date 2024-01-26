package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.OVERVIEW_FONT_SIZE

@Composable
fun LogDetails(header: String, message: String, color: Color = Color.White) {
    Row(Modifier.fillMaxWidth().background(color = color)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(header, fontWeight = FontWeight.Medium, fontSize = OVERVIEW_FONT_SIZE)
            Text(message, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

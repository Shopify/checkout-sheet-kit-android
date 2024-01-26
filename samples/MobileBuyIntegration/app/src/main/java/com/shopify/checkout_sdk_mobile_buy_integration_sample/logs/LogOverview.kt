package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.DATE_FORMAT
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.OVERVIEW_FONT_SIZE
import java.util.Date

@Composable
fun LogOverviewRow(logDate: LogDate, logMessage: LogMessage, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(color)) {
        val formatted = DateFormat.format(DATE_FORMAT, logDate.date)

        Text(
            text = "$formatted",
            fontSize = OVERVIEW_FONT_SIZE,
            modifier = Modifier.weight(logDate.weight).fillMaxHeight().align(Alignment.CenterVertically)
        )

        TextButton(onClick, Modifier.weight(logMessage.weight).fillMaxWidth().wrapContentHeight()) {
            Text(
                text = logMessage.message,
                fontSize = OVERVIEW_FONT_SIZE,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class LogDate(
    val date: Date,
    val weight: Float,
)

data class LogMessage(
    val message: String,
    val weight: Float,
)

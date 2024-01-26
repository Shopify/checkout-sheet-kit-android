package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogLine
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun LogDetailModal(
    logLine: LogLine?,
    onDismissRequest: () -> Unit,
    prettyJson: Json = Json { prettyPrint = true; prettyPrintIndent = "  " }
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (logLine?.type) {
                    LogType.STANDARD -> LogDetails("Checkout Lifecycle Event", logLine.message)
                    LogType.ERROR -> LogDetails("Checkout Error", "${logLine.errorDetails}")
                    LogType.STANDARD_PIXEL -> PixelEventDetails(logLine.standardPixelEvent, prettyJson)
                    LogType.CUSTOM_PIXEL -> PixelEventDetails(logLine.customPixelEvent, prettyJson)
                    else -> Text("Unknown log type ${logLine?.type}")
                }
            }
        }
    }
}

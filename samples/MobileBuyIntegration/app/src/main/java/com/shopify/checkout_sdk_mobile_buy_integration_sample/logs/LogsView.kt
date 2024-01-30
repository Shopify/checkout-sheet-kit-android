/*
 * MIT License
 * 
 * Copyright 2023-present, Shopify Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.AppBarState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogLine
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.DATE_COLUMN_WEIGHT
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.Logs.MESSAGE_COLUMN_WEIGHT
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.details.LogDetailModal
import java.util.Date

@Composable
fun LogsView(
    logsViewModel: LogsViewModel,
    setAppBarState: (AppBarState) -> Unit,
) {
    val logDetailsDialogOpen = remember { mutableStateOf(false) }
    val logDetails = remember { mutableStateOf<LogLine?>(null) }

    LaunchedEffect(key1 = true) {
        setAppBarState(
            AppBarState(
                title = "Logs",
                actions = {
                    IconButton(onClick = { logsViewModel.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear logs",
                        )
                    }
                }
            )
        )
        logsViewModel.readLogs(last = 50)
    }

    if (logDetailsDialogOpen.value) {
        LogDetailModal(logLine = logDetails.value, {
            logDetails.value = null
            logDetailsDialogOpen.value = false
        })
    }

    when (val logState = logsViewModel.logState.collectAsState().value) {
        is LogState.Loading -> {
            Text("Logs loading")
        }
        is LogState.Populated -> {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(PaddingValues(top = 0.dp, end = 8.dp, bottom = 12.dp, start = 8.dp))
            ) {
                item {
                    LogOverviewHeader(
                        Header("Date", DATE_COLUMN_WEIGHT),
                        Header("Type", MESSAGE_COLUMN_WEIGHT)
                    )
                }
                itemsIndexed(logState.logs) {  index, line ->
                    LogOverviewRow(
                        logDate = LogDate(date = Date(line.createdAt), weight = DATE_COLUMN_WEIGHT),
                        logMessage = LogMessage(message = line.message, weight = MESSAGE_COLUMN_WEIGHT),
                        color = index.rowColor()
                    ) {
                        logDetails.value = line
                        logDetailsDialogOpen.value = true
                    }
                }
            }
        }
    }
}

private fun Int.rowColor(): Color {
    return if (this % 2 == 0) Color.White
    else Logs.ROW_COLOR
}

object Logs {
    const val DATE_FORMAT = "dd/MM/yy HH:mm:ss"
    const val DATE_COLUMN_WEIGHT = 0.25f
    const val MESSAGE_COLUMN_WEIGHT = 0.75f
    val OVERVIEW_FONT_SIZE = 12.sp
    val ROW_COLOR = Color.hsl(185f, 0.43f, 0.95f)
}

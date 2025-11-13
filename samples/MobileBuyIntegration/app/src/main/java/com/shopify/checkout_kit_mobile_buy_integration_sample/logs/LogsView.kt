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
package com.shopify.checkout_kit_mobile_buy_integration_sample.logs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopify.checkout_kit_mobile_buy_integration_sample.R
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ui.theme.horizontalPadding
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ui.theme.verticalPadding
import com.shopify.checkout_kit_mobile_buy_integration_sample.logs.details.LogDetailModal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogsView(logsViewModel: LogsViewModel) {
    val logDetailsDialogOpen = remember { mutableStateOf(false) }
    val logDetails = remember { mutableStateOf<PrettyLog?>(null) }

    LaunchedEffect(key1 = true) {
        logsViewModel.readLogs(last = 100)
    }

    if (logDetailsDialogOpen.value) {
        LogDetailModal(logLine = logDetails.value?.data, onDismissRequest = {
            logDetails.value = null
            logDetailsDialogOpen.value = false
        })
    }

    when (val logState = logsViewModel.logState.collectAsState().value) {
        is LogState.Loading -> {
            Text("Logs loading")
        }

        is LogState.Populated -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {

                Button(shape = RectangleShape, onClick = {
                    logsViewModel.clear()
                }) {
                    BodyMedium(
                        text = stringResource(id = R.string.delete_logs),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    stickyHeader {
                        LogOverviewHeader(
                            Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(logState.logs) { index, line ->
                        LogOverview(
                            log = line,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            onClick = {
                                logDetails.value = line
                                logDetailsDialogOpen.value = true
                            }
                        )
                    }
                }
            }
        }
    }
}

object Logs {
    const val DATE_FORMAT = "dd/MM/yy HH:mm:ss"
    const val DATE_COLUMN_WEIGHT = 0.25f
    const val MESSAGE_COLUMN_WEIGHT = 0.75f
}

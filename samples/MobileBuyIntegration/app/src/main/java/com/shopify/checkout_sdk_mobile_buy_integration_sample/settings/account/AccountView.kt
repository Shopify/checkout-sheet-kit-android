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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.ProgressIndicator
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.RemoteImage
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountView(
    navController: NavHostController,
    viewModel: AccountViewModel = koinViewModel(),
) {

    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(key1 = true) {
        viewModel.loadCustomer()
    }

    when (uiState) {
        is UIState.Loading -> {
            ProgressIndicator()
        }

        is UIState.Error -> {
            BodyMedium(stringResource(id = R.string.customer_failed_to_load))
        }

        is UIState.Loaded -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 15.dp)
            ) {
                val customer = uiState.customer

                Row {
                    Column {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .fillMaxWidth()
                        ) {

                            Header2(
                                text = customer.displayName,
                                modifier = Modifier.padding(end = 10.dp),
                            )

                            RemoteImage(
                                url = customer.imageUrl,
                                altText = stringResource(id = R.string.customer_image_alt),
                                modifier = Modifier
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            customer.emailAddress?.let {
                                CustomerDetailRow(fieldNameResource = R.string.customer_email, value = it.emailAddress)
                            }

                            customer.phoneNumber?.let {
                                CustomerDetailRow(fieldNameResource = R.string.customer_phone, value = it.phoneNumber)
                            }
                        }
                    }
                }

                customer.defaultAddress?.let {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Header3(
                            text = stringResource(id = R.string.customer_default_addr),
                            modifier = Modifier.padding(bottom = 20.dp),
                        )

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_first_name,
                            value = it.firstName ?: "",
                        )

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_last_name,
                            value = it.lastName ?: "",
                        )

                        it.phoneNumber?.let { phoneNumber ->
                            CustomerDetailRow(
                                fieldNameResource = R.string.customer_default_addr_phone,
                                value = phoneNumber
                            )
                        }

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_address1,
                            value = it.address1 ?: ""
                        )

                        it.address2?.let { address2 ->
                            CustomerDetailRow(
                                fieldNameResource = R.string.customer_default_addr_address2,
                                value = address2,
                            )
                        }

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_city,
                            value = it.city ?: "",
                        )

                        it.province?.let { province ->
                            CustomerDetailRow(
                                fieldNameResource = R.string.customer_default_addr_province,
                                value = province,
                            )
                        }

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_zip,
                            value = it.zip ?: "",
                        )

                        CustomerDetailRow(
                            fieldNameResource = R.string.customer_default_addr_country,
                            value = it.country ?: "",
                        )
                    }
                }

                Button(onClick = { navController.navigate(Screen.Settings.route) }, shape = RectangleShape) {
                    BodyMedium(
                        stringResource(id = R.string.customer_back_to_settings),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailRow(fieldNameResource: Int, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
        BodyMedium(
            text = stringResource(id = fieldNameResource),
            modifier = Modifier.weight(1f),
        )

        BodyMedium(
            text = value,
            modifier = Modifier.weight(2f),
        )
    }
}

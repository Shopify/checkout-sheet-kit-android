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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.checkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkoutsheetkit.CheckoutPaymentMethodChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CardBrand
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrumentDisplayInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrumentInput
import com.shopify.checkoutsheetkit.lifecycleevents.ExpiryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartMailingAddressInput
import com.shopify.checkoutsheetkit.rpc.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.events.CheckoutPaymentMethodChangeStart
import kotlinx.coroutines.launch

data class PaymentOption(
    val label: String,
    val paymentInstrument: CartPaymentInstrumentInput,
)

@Composable
fun PaymentSelectionScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
) {
    val eventStore = LocalCheckoutEventStore.current
    val event = remember(eventId) {
        eventStore.getEvent(eventId) as? CheckoutPaymentMethodChangeStart
    }
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        onNavigateBack()
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val paymentOptions = remember {
        listOf(
            PaymentOption(
                label = "Visa ending in 4242",
                paymentInstrument = CartPaymentInstrumentInput(
                    externalReference = "visa-4242",
                    display = CartPaymentInstrumentDisplayInput(
                        last4 = "4242",
                        brand = CardBrand.VISA,
                        cardHolderName = "John Doe",
                        expiry = ExpiryInput(month = 12, year = 2025)
                    ),
                    billingAddress = CartMailingAddressInput(
                        firstName = "John",
                        lastName = "Doe",
                        address1 = "150 5th Avenue",
                        city = "New York",
                        countryCode = "US",
                        provinceCode = "NY",
                        zip = "10011"
                    )
                )
            ),
            PaymentOption(
                label = "Mastercard ending in 5555",
                paymentInstrument = CartPaymentInstrumentInput(
                    externalReference = "mc-5555",
                    display = CartPaymentInstrumentDisplayInput(
                        last4 = "5555",
                        brand = CardBrand.MASTERCARD,
                        cardHolderName = "Jane Smith",
                        expiry = ExpiryInput(month = 6, year = 2026)
                    ),
                    billingAddress = CartMailingAddressInput(
                        firstName = "Jane",
                        lastName = "Smith",
                        address1 = "89 Haight Street",
                        address2 = "Apt 2B",
                        city = "San Francisco",
                        countryCode = "US",
                        provinceCode = "CA",
                        zip = "94117"
                    )
                )
            ),
            PaymentOption(
                label = "American Express ending in 0005",
                paymentInstrument = CartPaymentInstrumentInput(
                    externalReference = "amex-0005",
                    display = CartPaymentInstrumentDisplayInput(
                        last4 = "0005",
                        brand = CardBrand.AMERICAN_EXPRESS,
                        cardHolderName = "Alex Johnson",
                        expiry = ExpiryInput(month = 3, year = 2027)
                    ),
                    billingAddress = CartMailingAddressInput(
                        firstName = "Alex",
                        lastName = "Johnson",
                        address1 = "456 Oak Ave",
                        city = "Chicago",
                        countryCode = "US",
                        provinceCode = "IL",
                        zip = "60601"
                    )
                )
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Header2(
            text = "Select a payment method",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(paymentOptions) { index, option ->
                PaymentOptionItem(
                    option = option,
                    isSelected = index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                errorMessage = null

                coroutineScope.launch {
                    val paymentInstrument = paymentOptions[selectedIndex].paymentInstrument

                    val cartInput = CartInput(
                        paymentInstruments = listOf(paymentInstrument)
                    )

                    val response = CheckoutPaymentMethodChangeStartResponsePayload(cart = cartInput)

                    try {
                        event?.respondWith(response)
                        eventStore.removeEvent(eventId)
                        onNavigateBack()
                    } catch (e: CheckoutEventResponseException.ValidationFailed) {
                        errorMessage = "Validation failed: ${e.message}"
                    } catch (e: CheckoutEventResponseException.DecodingFailed) {
                        errorMessage = "Decoding failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp)
        ) {
            Text("Use Selected Payment Method")
        }
    }
}

@Composable
private fun PaymentOptionItem(
    option: PaymentOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${option.paymentInstrument.display.cardHolderName} - Expires ${option.paymentInstrument.display.expiry.month}/${option.paymentInstrument.display.expiry.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

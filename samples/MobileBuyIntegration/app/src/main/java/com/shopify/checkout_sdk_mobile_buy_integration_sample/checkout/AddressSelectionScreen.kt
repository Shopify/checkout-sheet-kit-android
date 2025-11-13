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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header2
import com.shopify.checkoutsheetkit.CartDelivery
import com.shopify.checkoutsheetkit.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.DeliveryAddressChangePayload
import kotlinx.coroutines.launch

/**
 * Address option for selection.
 */
data class AddressOption(
    val label: String,
    val address: CartDeliveryAddressInput,
)

/**
 * Address selection screen that works within checkout navigation.
 * Retrieves the event from CheckoutEventStore and responds when address is selected.
 */
@Composable
fun AddressSelectionScreen(
    eventId: String,
    onDismiss: () -> Unit,
) {
    val eventStore = LocalCheckoutEventStore.current
    val event = remember(eventId) {
        eventStore.getEvent(eventId) as? com.shopify.checkoutsheetkit.CheckoutAddressChangeRequestedEvent
    }
    val coroutineScope = rememberCoroutineScope()

    var selectedIndex by remember { mutableIntStateOf(0) }
    val addressOptions = remember {
        listOf(
            AddressOption(
                label = "Default",
                address = CartDeliveryAddressInput(
                    firstName = "John",
                    lastName = "Smith",
                    address1 = "150 5th Avenue",
                    address2 = null,
                    city = "New York",
                    countryCode = "US",
                    phone = "+12125551234",
                    provinceCode = "NY",
                    zip = "10011",
                )
            ),
            AddressOption(
                label = "West Coast Address",
                address = CartDeliveryAddressInput(
                    firstName = "Evelyn",
                    lastName = "Hartley",
                    address1 = "89 Haight Street",
                    address2 = "Apt 2B",
                    city = "San Francisco",
                    countryCode = "US",
                    phone = "+14159876543",
                    provinceCode = "CA",
                    zip = "94117",
                )
            ),
            AddressOption(
                label = "Invalid Address",
                address = CartDeliveryAddressInput(
                    firstName = "Invalid",
                    lastName = "User",
                    address1 = "123 Fake Street",
                    address2 = null,
                    city = "Austin",
                    countryCode = "US",
                    phone = "+15125551234",
                    provinceCode = "TX",
                    zip = "00000",
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
            text = "Select an address",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(addressOptions) { index, option ->
                AddressOptionItem(
                    option = option,
                    isSelected = index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val selectedAddress = addressOptions[selectedIndex].address
                    val response = DeliveryAddressChangePayload(
                        delivery = CartDelivery(
                            addresses = listOf(
                                CartSelectableAddressInput(address = selectedAddress)
                            )
                        )
                    )

                    // Respond to the event
                    event?.respondWith(response)

                    // Clean up event from store
                    eventStore.removeEvent(eventId)

                    // Dismiss the address screen
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp)
        ) {
            Text("Use Selected Address")
        }
    }
}

@Composable
private fun AddressOptionItem(
    option: AddressOption,
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
                text = "${option.address.city}, ${option.address.provinceCode ?: option.address.countryCode} ${option.address.zip}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

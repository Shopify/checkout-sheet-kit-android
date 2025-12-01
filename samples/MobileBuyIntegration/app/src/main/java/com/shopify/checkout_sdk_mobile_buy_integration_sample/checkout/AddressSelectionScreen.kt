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
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartInput
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
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
    onNavigateBack: () -> Unit,
) {
    val eventStore = LocalCheckoutEventStore.current
    val event = remember(eventId) {
        eventStore.getEvent<CheckoutAddressChangeStartEvent>(eventId)
    }
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        onNavigateBack()
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                label = "❌ Invalid - SDK validation (3-letter country code)",
                address = CartDeliveryAddressInput(
                    firstName = "Test",
                    lastName = "Invalid",
                    address1 = "123 Error Street",
                    address2 = null,
                    city = "Austin",
                    countryCode = "USA", // Invalid: SDK validates country code must be exactly 2 characters
                    phone = "+15125551234",
                    provinceCode = "TX",
                    zip = "78701",
                )
            ),
            AddressOption(
                label = "❌ Invalid - Backend validation (postcode invalid for country)",
                address = CartDeliveryAddressInput(
                    firstName = "Test",
                    lastName = "User",
                    address1 = "456 Nowhere Lane",
                    address2 = null,
                    city = "Portland",
                    countryCode = "US",
                    phone = "+15035551234",
                    provinceCode = "OR",
                    zip = "SA35HP",
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

        // Error message display
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
                // Clear previous error
                errorMessage = null

                coroutineScope.launch {
                    val address = addressOptions[selectedIndex].address

                    val cartInput = CartInput(
                        delivery = CartDeliveryInput(
                            addresses = listOf(
                                CartSelectableAddressInput(
                                    address = address,
                                    selected = true
                                )
                            )
                        )
                    )

                    val response = CheckoutAddressChangeStartResponsePayload(cart = cartInput)

                    try {
                        // Respond to the event - validation happens here
                        event?.respondWith(response)

                        // Clean up event from store
                        eventStore.removeEvent(eventId)

                        // Navigate back to checkout
                        onNavigateBack()
                    } catch (e: CheckoutEventResponseException.DecodingFailed) {
                        // Show decoding error to user
                        errorMessage = "Decoding failed: ${e.message}"
                    }
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

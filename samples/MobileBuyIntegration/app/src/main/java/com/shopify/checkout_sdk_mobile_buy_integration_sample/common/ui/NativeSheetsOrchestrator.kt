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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.sheets.AddressSelectionBottomSheet
import com.shopify.checkoutsheetkit.CartDelivery
import com.shopify.checkoutsheetkit.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.CheckoutAddressChangeRequestedEvent
import com.shopify.checkoutsheetkit.DeliveryAddressChangePayload

/**
 * Represents the different types of native sheets that can be displayed during checkout.
 */
sealed class NativeSheet {
    data class Address(val event: CheckoutAddressChangeRequestedEvent) : NativeSheet()
    // Future: data class Payment(val event: CheckoutPaymentMethodChangeRequestedEvent) : NativeSheet()
}

/**
 * Orchestrates native sheets displayed during checkout (address picker, payment methods, etc.)
 * Watches for checkout events and launches the appropriate native sheet components.
 * Ensures only one sheet is displayed at a time.
 */
@Composable
fun NativeSheetsOrchestrator() {
    when (val sheet = NativeSheetState.currentSheet) {
        is NativeSheet.Address -> AddressSheet(
            event = sheet.event,
            onDismiss = { NativeSheetState.dismiss() }
        )
        null -> { /* No sheet showing */ }
    }
}

/**
 * Native sheet for address selection.
 * Presents a bottom sheet with address options and responds to the checkout with the selected address.
 */
@Composable
fun AddressSheet(
    event: CheckoutAddressChangeRequestedEvent,
    onDismiss: () -> Unit
) {
    AddressSelectionBottomSheet(
        onAddressSelected = { selectedAddress ->
            event.respondWith(
                DeliveryAddressChangePayload(
                    delivery = CartDelivery(
                        addresses = listOf(
                            CartSelectableAddressInput(
                                address = selectedAddress
                            )
                        )
                    )
                )
            )
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

/**
 * Shared state holder for native sheets.
 * Ensures only one sheet is displayed at a time.
 * Used to bridge between the event callback and Compose UI.
 */
object NativeSheetState {
    var currentSheet by mutableStateOf<NativeSheet?>(null)
        private set

    fun show(sheet: NativeSheet) {
        currentSheet = sheet
    }

    fun dismiss() {
        currentSheet = null
    }
}
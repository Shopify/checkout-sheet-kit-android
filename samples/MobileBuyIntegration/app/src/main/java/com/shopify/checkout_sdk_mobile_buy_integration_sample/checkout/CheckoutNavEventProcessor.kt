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

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import timber.log.Timber
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.lifecycleevents.CartPayment
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrument
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentMethod
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutSubmitStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.RemoteTokenPaymentCredential
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Event processor that uses Compose Navigation instead of Activity Result API.
 * Navigates within the checkout navigation graph to keep WebView alive.
 */
@OptIn(DelicateCoroutinesApi::class)
class CheckoutNavEventProcessor(
    private val mainNavController: NavController,
    private val checkoutNavController: NavController,
    private val context: Context,
    private val eventStore: CheckoutEventStore,
    private val logger: Logger,
) : DefaultCheckoutEventProcessor(context) {

    override fun onStart(event: CheckoutStartEvent) {
        Timber.d("Checkout start: $event")
        logger.log(event)
    }

    override fun onComplete(event: CheckoutCompleteEvent) {
        Timber.d("Checkout complete: $event")
        logger.log(event)
    }

    override fun onFail(error: CheckoutException) {
        Timber.e(error, "Checkout failed: $error")

        // No fallback in inline flow - navigate back and show error
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(
                context,
                context.getText(R.string.checkout_error),
                Toast.LENGTH_SHORT
            ).show()

            // Navigate back to product
            mainNavController.popBackStack()
        }
    }

    override fun onCancel() {
        Timber.d("Checkout canceled")
    }

    override fun onAddressChangeStart(event: CheckoutAddressChangeStartEvent) {
        Timber.d("Address change start: $event")

        // Store event and get ID
        val eventId = eventStore.storeEvent(event)

        // Navigate to address screen within checkout nav graph
        GlobalScope.launch(Dispatchers.Main) {
            checkoutNavController.navigate(CheckoutScreen.Address.route(eventId))
        }
    }

    override fun onSubmitStart(event: CheckoutSubmitStartEvent) {
        Timber.d("Submit start $event")
        val updatedCart = event.cart.copy(
            payment = CartPayment(
                methods = listOf(
                    CartPaymentMethod(
                        instruments = listOf(
                            CartPaymentInstrument(
                                externalReferenceId = "submit-payment-123",
                                credentials = listOf(
                                    RemoteTokenPaymentCredential(
                                        token = "my-token",
                                        tokenType = "test",
                                        tokenHandler = "test-provider"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        event.respondWith(
            payload = CheckoutSubmitStartResponsePayload(cart = updatedCart)
        )
    }

    override fun onPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStartEvent) {
        super.onPaymentMethodChangeStart(event)
        Timber.d("Payment method change start: $event")

        // Store event and get ID
        val eventId = eventStore.storeEvent(event)

        // Navigate to payment screen within checkout nav graph
        GlobalScope.launch(Dispatchers.Main) {
            checkoutNavController.navigate(CheckoutScreen.Payment.route(eventId))
        }
    }
}

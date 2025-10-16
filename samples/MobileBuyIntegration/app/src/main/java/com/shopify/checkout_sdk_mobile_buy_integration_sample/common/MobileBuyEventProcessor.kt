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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

import android.content.Context
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.MainActivity
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.Analytics
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.toAnalyticsEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkoutsheetkit.CartDelivery
import com.shopify.checkoutsheetkit.CartDeliveryAddressInput
import com.shopify.checkoutsheetkit.CartSelectableAddressInput
import com.shopify.checkoutsheetkit.CheckoutAddressChangeRequestedEvent
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.DeliveryAddressChangePayload
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
import com.shopify.checkoutsheetkit.pixelevents.Delivery
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class MobileBuyEventProcessor(
    private val cartViewModel: CartViewModel,
    private val navController: NavController,
    private val logger: Logger,
    private val context: Context
) : DefaultCheckoutEventProcessor(context) {
    override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
        logger.log(checkoutCompletedEvent)

        cartViewModel.clearCart()
        GlobalScope.launch(Dispatchers.Main) {
            navController.popBackStack(Screen.Product.route, false)
        }
    }

    override fun onCheckoutFailed(error: CheckoutException) {
        logger.log("Checkout failed", error)

        if (!error.isRecoverable) {
            cartViewModel.clearCart()
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getText(R.string.checkout_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCheckoutCanceled() {
        // optionally respond to checkout being canceled/closed
        logger.log("Checkout canceled")
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        return (context as MainActivity).onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onAddressChangeRequested(event: CheckoutAddressChangeRequestedEvent) {
        event.respondWith(DeliveryAddressChangePayload(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddressInput(
                        address = CartDeliveryAddressInput(
                            firstName = "Bob",
                            lastName = "Jones",
                            address1 = "44 Sunningdale Ave",
                            city = "Swansea",
                            countryCode = "GB",
                            zip = "SA35HP",
                        )
                    )
                )
            )
        ))
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    ): Boolean {
        return (context as MainActivity).onShowFileChooser(filePathCallback, fileChooserParams)
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        logger.log(event)

        // handle pixel events (e.g. transform, augment, and process), e.g.
        val analyticsEvent = when (event) {
            is StandardPixelEvent -> event.toAnalyticsEvent()
            is CustomPixelEvent -> event.toAnalyticsEvent()
        }

        analyticsEvent?.let {
            Analytics.record(analyticsEvent)
        }
    }
}

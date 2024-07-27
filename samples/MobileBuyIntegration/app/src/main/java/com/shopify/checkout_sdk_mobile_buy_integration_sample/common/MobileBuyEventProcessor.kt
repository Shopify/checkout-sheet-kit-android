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

import android.Manifest
import android.content.Context
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.Analytics
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.analytics.toAnalyticsEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.getActivity
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.permissions.Permissions
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.CustomPixelEvent
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
): DefaultCheckoutEventProcessor(context) {
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

    override fun onPermissionRequest(permissionRequest: PermissionRequest) {
        logger.log("Permission requested for ${permissionRequest.resources}")
        context.getActivity()?.let { activity ->
            if (Permissions.hasPermission(activity, permissionRequest)) {
                permissionRequest.grant(permissionRequest.resources)
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    Permissions.PERMISSION_REQUEST_CODE,
                )
                permissionRequest.deny()
            }
        }
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

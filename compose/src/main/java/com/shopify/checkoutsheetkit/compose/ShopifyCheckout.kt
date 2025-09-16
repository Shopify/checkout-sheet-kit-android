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
package com.shopify.checkoutsheetkit.compose

import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.CheckoutSheetKitDialog
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent

internal typealias PresentCheckout = (String, ComponentActivity, DefaultCheckoutEventProcessor) -> CheckoutSheetKitDialog?

/**
 * A Composable that provides a simple way to present Shopify checkout in your Compose UI.
 * This composable manages the checkout dialog state and provides idiomatic Compose callbacks
 * for checkout events.
 *
 * @param url The URL of the checkout to be presented
 * @param onComplete Called when checkout is successfully completed
 * @param onCancel Called when checkout is canceled by the user or dismissed programmatically
 * @param onFail Called when checkout fails with an error
 * @param onLinkClicked Called when a link is clicked that should open externally (optional, uses default behavior if null)
 * @param onPixelEvent Called when a web pixel event is emitted (optional)
 * @param onPermissionRequest Called when permissions are requested (optional)
 * @param onShowFileChooser Called when file chooser should be shown (optional)
 * @param onGeolocationPermissionRequest Called when geolocation permission is requested (optional)
 * @param onGeolocationPermissionHide Called when geolocation permission prompt should be hidden (optional)
 */
@Composable
public fun ShopifyCheckout(
    url: String,
    onComplete: (CheckoutCompletedEvent) -> Unit,
    onCancel: () -> Unit,
    onFail: (CheckoutException) -> Unit,
    onLinkClicked: ((Uri) -> Unit)? = null,
    onPixelEvent: ((PixelEvent) -> Unit)? = null,
    onPermissionRequest: ((PermissionRequest) -> Unit)? = null,
    onShowFileChooser: ((WebView, ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)? = null,
    onGeolocationPermissionRequest: ((String, GeolocationPermissions.Callback) -> Unit)? = null,
    onGeolocationPermissionHide: (() -> Unit)? = null,
    @VisibleForTesting
    presentCheckout: PresentCheckout = ShopifyCheckoutSheetKit::present,
) {
    val activity = LocalActivity.current as ComponentActivity
    var dialog by remember { mutableStateOf<CheckoutSheetKitDialog?>(null) }

    LaunchedEffect(url) {
        // If URL changed while visible, dismiss old dialog first
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
        
        val eventProcessor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = onComplete,
            checkoutCanceledCallback = onCancel,
            checkoutFailedCallback = onFail,
            linkClickedCallback = onLinkClicked,
            webPixelEventCallback = onPixelEvent,
            permissionRequestCallback = onPermissionRequest,
            fileChooserCallback = onShowFileChooser,
            geolocationRequestCallback = onGeolocationPermissionRequest,
            geolocationHideCallback = onGeolocationPermissionHide,
        )

        dialog = presentCheckout(url, activity, eventProcessor)
    }

    DisposableEffect(Unit) {
        onDispose {
            dialog?.dismiss()
            dialog = null
        }
    }
}

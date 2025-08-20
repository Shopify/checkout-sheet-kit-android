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

import android.content.Context
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent

/**
 * Internal event processor that bridges between Compose callbacks and the CheckoutEventProcessor interface.
 * This allows the CheckoutView composable to use idiomatic lambda parameters instead of requiring
 * clients to implement the CheckoutEventProcessor interface.
 */
internal class ComposeCheckoutEventProcessor(
    context: Context,
    private val checkoutCompletedCallback: (CheckoutCompletedEvent) -> Unit,
    private val checkoutCanceledCallback: () -> Unit,
    private val checkoutFailedCallback: (CheckoutException) -> Unit,
    private val linkClickedCallback: ((Uri) -> Unit)?,
    private val webPixelEventCallback: ((PixelEvent) -> Unit)?,
    private val permissionRequestCallback: ((PermissionRequest) -> Unit)?,
    private val fileChooserCallback: ((WebView, ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)?,
    private val geolocationRequestCallback: ((String, GeolocationPermissions.Callback) -> Unit)?,
    private val geolocationHideCallback: (() -> Unit)?,
) : DefaultCheckoutEventProcessor(context) {

    override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
        checkoutCompletedCallback(checkoutCompletedEvent)
    }

    override fun onCheckoutFailed(error: CheckoutException) {
        checkoutFailedCallback(error)
    }

    override fun onCheckoutCanceled() {
        checkoutCanceledCallback()
    }

    override fun onCheckoutLinkClicked(uri: Uri) {
        linkClickedCallback?.invoke(uri) ?: super.onCheckoutLinkClicked(uri)
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        webPixelEventCallback?.invoke(event) ?: super.onWebPixelEvent(event)
    }

    override fun onPermissionRequest(permissionRequest: PermissionRequest) {
        permissionRequestCallback?.invoke(permissionRequest) ?: super.onPermissionRequest(permissionRequest)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    ): Boolean {
        return fileChooserCallback?.invoke(webView, filePathCallback, fileChooserParams)
            ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        geolocationRequestCallback?.invoke(origin, callback)
            ?: super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onGeolocationPermissionsHidePrompt() {
        geolocationHideCallback?.invoke() ?: super.onGeolocationPermissionsHidePrompt()
    }
}

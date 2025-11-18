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
package com.shopify.checkoutsheetkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import com.shopify.checkoutsheetkit.rpcevents.AddressChangeRequested

/**
 * Interface to implement to allow responding to lifecycle events in checkout.
 * We'd strongly recommend extending DefaultCheckoutEventProcessor where possible
 */
public interface CheckoutEventProcessor {
    /**
     * Event representing the successful completion of a checkout.
     */
    public fun onCheckoutCompleted(checkoutCompleteEvent: CheckoutCompleteEvent)

    /**
     * Event representing an error that occurred during checkout. This can be used to display
     * error messages for example.
     *
     * @param error - the CheckoutErrorException that occurred
     * @see Exception
     */
    public fun onCheckoutFailed(error: CheckoutException)

    /**
     * Event representing the cancellation/closing of checkout by the buyer
     */
    public fun onCheckoutCanceled()

    /**
     * Event indicating that a link has been clicked within checkout that should be opened outside
     * of the WebView, e.g. in a system browser or email client. Protocols can be http/https/mailto/tel
     */
    public fun onCheckoutLinkClicked(uri: Uri)

    /**
     * A permission has been requested by the web chrome client, e.g. to access the camera
     */
    public fun onPermissionRequest(permissionRequest: PermissionRequest)

    /**
     * Web Pixel event emitted from checkout, that can be optionally transformed, enhanced (e.g. with user and session identifiers),
     * and processed
     */
    public fun onWebPixelEvent(event: PixelEvent)

    /**
     * Called when the client should show a file chooser. This is called to handle HTML forms with 'file' input type, in response to the
     * user pressing the "Select File" button. To cancel the request, call filePathCallback.onReceiveValue(null) and return true.
     */
    public fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    ): Boolean

    /**
     * Called when the client should show a location permissions prompt. For example when using 'Use my location' for
     * pickup points in checkout
     */
    public fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback)

    /**
     * Called when the client should hide the location permissions prompt, e.g. if th request is cancelled
     */
    public fun onGeolocationPermissionsHidePrompt()

    /**
     * Called when checkout requests that the buyer change their delivery address.
     *
     * By default the request is cancelled. Override to present custom UI and provide a response
     * via [AddressChangeRequested.respondWith] (or cancel explicitly).
     */
    public fun onAddressChangeRequested(event: AddressChangeRequested)
}

internal class NoopEventProcessor : CheckoutEventProcessor {
    override fun onCheckoutCompleted(checkoutCompleteEvent: CheckoutCompleteEvent) {/* noop */
    }

    override fun onCheckoutFailed(error: CheckoutException) {/* noop */
    }

    override fun onCheckoutCanceled() {/* noop */
    }

    override fun onCheckoutLinkClicked(uri: Uri) {/* noop */
    }

    override fun onWebPixelEvent(event: PixelEvent) {/* noop */
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    ): Boolean {
        return false
    }

    override fun onPermissionRequest(permissionRequest: PermissionRequest) {/* noop */
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {/* noop */
    }

    override fun onGeolocationPermissionsHidePrompt() {/* noop */
    }

    override fun onAddressChangeRequested(event: AddressChangeRequested) {/* noop */
    }
}

/**
 * An abstract class that provides a default implementation of the CheckoutEventProcessor interface
 * for handling checkout events and interacting with the Android operating system.
 * @param context from which we will launch intents.
 */
public abstract class DefaultCheckoutEventProcessor @JvmOverloads constructor(
    private val context: Context,
    private val log: LogWrapper = LogWrapper(),
) : CheckoutEventProcessor {

    override fun onCheckoutLinkClicked(uri: Uri) {
        when (uri.scheme) {
            "tel" -> context.launchPhoneApp(uri.schemeSpecificPart)
            "mailto" -> context.launchEmailApp(uri.schemeSpecificPart)
            "https", "http" -> context.launchBrowser(uri)
            else -> context.tryLaunchDeepLink(uri)
        }
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        // no-op, override to implement
    }

    override fun onPermissionRequest(permissionRequest: PermissionRequest) {
        // no-op override to implement
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    ): Boolean {
        return false
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        // no-op override to implement
    }

    override fun onGeolocationPermissionsHidePrompt() {
        // no-op override to implement
    }

    private fun Context.launchEmailApp(to: String) {
        log.d(LOG_TAG, "Attempting to launch email app.")
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        startActivity(intent)
    }

    private fun Context.launchBrowser(uri: Uri) {
        log.d(LOG_TAG, "Attempting to launch browser for $uri.")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        startActivity(intent)
    }

    private fun Context.launchPhoneApp(phone: String) {
        log.d(LOG_TAG, "Attempting to launch phone app.")
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun Context.tryLaunchDeepLink(uri: Uri) {
        log.d(LOG_TAG, "Attempting to launch deep link for uri $uri.")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
            startActivity(intent)
        } else {
            log.w(TAG, "Unrecognized scheme for link clicked in checkout '$uri'")
        }
    }

    private companion object {
        private const val LOG_TAG = "DefaultCheckoutEventProcessor"
        private const val TAG = "DefaultCheckoutEventProcessor"
    }
}

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
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutSubmitStart
import com.shopify.checkoutsheetkit.rpc.events.PaymentMethodChangeStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutPaymentMethodChangeStart

/**
 * Interface to implement to allow responding to lifecycle events in checkout.
 * We'd strongly recommend extending DefaultCheckoutEventProcessor where possible
 */
public interface CheckoutEventProcessor {
    /**
     * Event triggered when checkout starts.
     * Provides the initial cart state at the beginning of the checkout flow.
     */
    public fun onStart(checkoutStartEvent: CheckoutStartEvent)

    /**
     * Event representing the successful completion of a checkout.
     */
    public fun onComplete(checkoutCompleteEvent: CheckoutCompleteEvent)

    /**
     * Event representing an error that occurred during checkout. This can be used to display
     * error messages for example.
     *
     * @param error - the CheckoutErrorException that occurred
     * @see Exception
     */
    public fun onFail(error: CheckoutException)

    /**
     * Event representing the cancellation/closing of checkout by the buyer
     */
    public fun onCancel()

    /**
     * Event indicating that a link has been clicked within checkout that should be opened outside
     * of the WebView, e.g. in a system browser or email client. Protocols can be http/https/mailto/tel
     */
    public fun onLinkClick(uri: Uri)

    /**
     * A permission has been requested by the web chrome client, e.g. to access the camera
     */
    public fun onPermissionRequest(permissionRequest: PermissionRequest)

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
     * Called when native address selection is configured for the authenticated app and a customer
     * indicates they want to start an address change.
     *
     * By default the request is cancelled. Override to present custom UI and provide a response
     * via [CheckoutAddressChangeStart.respondWith] or cancel explicitly.
     */
    public fun onAddressChangeStart(event: CheckoutAddressChangeStart)

    /**
     * Called when native payment delegation is configured for the authenticated app and the buyer
     * attempts to submit the checkout.
     *
     * Override to provide payment tokens, update cart data, or handle custom submission logic.
     * Provide a response via [CheckoutSubmitStart.respondWith].
     */
    public fun onSubmitStart(event: CheckoutSubmitStart)

    /**
     * Called when checkout requests that the buyer change their payment method.
     *
     * By default the request is cancelled. Override to present custom UI and provide a response
     * via [CheckoutPaymentMethodChangeStart.respondWith] (or cancel explicitly).
     */
    public fun onCheckoutPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStart)
}

internal class NoopEventProcessor : CheckoutEventProcessor {
    override fun onStart(checkoutStartEvent: CheckoutStartEvent) {/* noop */
    }

    override fun onComplete(checkoutCompleteEvent: CheckoutCompleteEvent) {/* noop */
    }

    override fun onFail(error: CheckoutException) {/* noop */
    }

    override fun onCancel() {/* noop */
    }

    override fun onLinkClick(uri: Uri) {/* noop */
    }

    override fun onAddressChangeStart(event: CheckoutAddressChangeStart) {/* noop */
    }

    override fun onSubmitStart(event: CheckoutSubmitStart) {/* noop */
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

    override fun onCheckoutAddressChangeStart(event: CheckoutAddressChangeStart) {/* noop */
    }

    override fun onCheckoutPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStart) {/* noop */
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

    override fun onStart(checkoutStartEvent: CheckoutStartEvent) {
        // no-op, override to implement
    }

    override fun onLinkClick(uri: Uri) {
        when (uri.scheme) {
            "tel" -> context.launchPhoneApp(uri.schemeSpecificPart)
            "mailto" -> context.launchEmailApp(uri.schemeSpecificPart)
            "https", "http" -> context.launchBrowser(uri)
            else -> context.tryLaunchDeepLink(uri)
        }
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

    override fun onAddressChangeStart(event: CheckoutAddressChangeStart) {
        // no-op override to implement
    }

    override fun onSubmitStart(event: CheckoutSubmitStart) {
        // no-op override to implement
    }

    override fun onPaymentMethodChangeStart(event: PaymentMethodChangeStart) {
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

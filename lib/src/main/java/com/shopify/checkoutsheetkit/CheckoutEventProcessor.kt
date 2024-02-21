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

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import kotlinx.serialization.Serializable

/**
 * Superclass for the Shopify Checkout Sheet Kit exceptions
 */
@Serializable
public abstract class CheckoutException(public val errorDescription: String) : Exception(errorDescription)

/**
 * Issued when an internal error occurs within Shopify Checkout Sheet Kit.
 * In event of an error you could use the stacktrace to inform you of how to proceed,
 * if the issue persists, it is recommended to open a bug report in https://github.com/Shopify/checkout-sheet-kit-android
 */
public class CheckoutSdkError(errorMsg: String) : CheckoutException(errorMsg)

/**
 * Issued when checkout has encountered a unrecoverable error (for example server side error).
 * if the issue persists, it is recommended to open a bug report in https://github.com/Shopify/checkout-sheet-kit-android
 */
public class CheckoutUnavailableException : CheckoutException("Checkout is currently unavailable due to an internal error")

/**
 * Issued when checkout is no longer available and will no longer be available with the checkout URL supplied.
 * This may happen when the user has paused on checkout for a long period (hours) and
 * then attempted to proceed again with the same checkout URL.
 * In event of checkoutExpired, a new checkout URL will need to be generated.
 */
public class CheckoutExpiredException : CheckoutException(
    "Checkout is no longer available with the provided token. Please generate a new checkout URL"
)

/**
 * Issued when the provided checkout URL results in an error related to shop being on checkout.liquid.
 * The SDK only supports stores migrated for extensibility.
 */
public class CheckoutLiquidNotMigratedException : CheckoutException(
    "The checkout URL provided has resulted in an error. The store is still using checkout.liquid, whereas the checkout SDK only " +
            "supports checkout with extensibility."
)

/**
 * Interface to implement to allow responding to lifecycle events in checkout.
 * We'd strongly recommend extending DefaultCheckoutEventProcessor where possible
 */
public interface CheckoutEventProcessor {
    /**
     * Event representing the successful completion of a checkout.
     */
    public fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent)

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
     * Web Pixel event emitted from checkout, that can be optionally transformed, enhanced (e.g. with user and session identifiers),
     * and processed
     */
    public fun onWebPixelEvent(event: PixelEvent)
}

internal class NoopEventProcessor : CheckoutEventProcessor {
    override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {/* noop */
    }

    override fun onCheckoutFailed(error: CheckoutException) {/* noop */
    }

    override fun onCheckoutCanceled() {/* noop */
    }

    override fun onCheckoutLinkClicked(uri: Uri) {/* noop */
    }

    override fun onWebPixelEvent(event: PixelEvent) {/* noop */
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
            else -> log.w(TAG, "Unrecognized scheme for link clicked in checkout '$uri'")
        }
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        // no-op, override to implement
    }

    private fun Context.launchEmailApp(to: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        startActivity(intent)
    }

    private fun Context.launchBrowser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        startActivity(intent)
    }

    private fun Context.launchPhoneApp(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }

    private companion object {
        private const val TAG = "DefaultCheckoutEventProcessor"
    }
}

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

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Entrypoint to the library, allows configuring, preloading, and presenting Shopify checkouts.
 */
public object ShopifyCheckoutSheetKit {

    internal val configuration = Configuration()

    /**
     * Returns the current version of ShopifyCheckoutSheetKit.

     * @return the current version
     */
    public const val version: String = BuildConfig.SDK_VERSION

    /**
     * Returns the currently applied ShopifyCheckoutSheetKit configuration.
     * Note: configuration changes should be made through the configure function.
     *
     * @return the currently applied configuration
     * @see ShopifyCheckoutSheetKit.configure(ConfigurationUpdater)
     */
    @JvmStatic
    public fun getConfiguration(): Configuration {
        return configuration.copy()
    }

    /**
     * Allows configuring ShopifyCheckoutSheetKit.
     *
     * Current configuration options are for enabling and disabling preloading, and for setting the checkout color scheme.
     * Kotlin example:
     * {@code ShopifyCheckoutSheetKit.configure { it.preloading = Preloading(enabled = enabled) }}
     *
     * @param setter a function that modifies the configuration object
     * @see Configuration
     */
    @JvmStatic
    public fun configure(setter: ConfigurationUpdater) {
        setter.configure(configuration)
        CheckoutWebView.clearCache()
    }

    /**
     * Invalidate WebViews cached due to preload calls
     */
    @JvmStatic
    public fun invalidate() {
        CheckoutWebView.markCacheEntryStale()
    }

    /**
     * Preloads a Shopify checkout in the background.
     *
     * Preloading checkout is fully optional, but allows reducing the time taken between calling
     * {@link ShopifyCheckoutSheetKit#present(String, ComponentActivity, CheckoutEventProcessor)} and having a fully interactive checkout.
     * Note: Preload must be called on all cart changes to avoid stale checkouts being presented.
     * Preloaded checkouts also have a TTL of 5 minutes, after checkout will be re-loaded on calling present.
     *
     * @param checkoutUrl The URL of the checkout to be loaded, this can be obtained via the Storefront API
     * @param context The context the checkout is being presented from
     */
    @JvmStatic
    public fun preload(checkoutUrl: String, context: ComponentActivity) {
        if (!configuration.preloading.enabled) return

        val cacheEntry = CheckoutWebView.cacheEntry
        if (cacheEntry?.view != null && cacheEntry.view.isInViewHierarchy()) {
            if (cacheEntry.key != checkoutUrl) {
                CheckoutWebView.markCacheEntryStale()
            }

            cacheEntry.view.loadCheckout(checkoutUrl, false)
        } else {
            CheckoutWebView.markCacheEntryStale()
            CheckoutWebView.cacheableCheckoutView(
                url = checkoutUrl,
                activity = context,
                isPreload = true,
            )
        }
    }

    /**
     * Presents a Shopify checkout within a Dialog
     *
     * @param checkoutUrl The URL of the checkout to be presented, this can be obtained via the Storefront API
     * @param context The context the checkout is being presented from
     * @param checkoutEventProcessor provides callbacks to allow clients to listen for and respond to checkout lifecycle events such as
     * (failure, completion, cancellation, external link clicks).
     * @return An instance of [CheckoutSheetKitDialog] if the dialog was successfully created and displayed.
     */
    @JvmStatic
    public fun <T: DefaultCheckoutEventProcessor> present(
        checkoutUrl: String,
        context: ComponentActivity,
        checkoutEventProcessor: T
    ): CheckoutSheetKitDialog? {
        if (context.isDestroyed || context.isFinishing) {
            return null
        }
        val dialog = CheckoutDialog(checkoutUrl, checkoutEventProcessor, context)
        context.lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                dialog.dismiss()
                super.onDestroy(owner)
            }
        })
        dialog.start(context)
        return CheckoutSheetKitDialog { dialog.dismiss() }
    }
}

/**
 * A checkout sheet dialog. Use the [dismiss] method to dismiss the presented dialog
 */
@FunctionalInterface
public fun interface CheckoutSheetKitDialog {
    /**
     * Dismisses the checkout sheet dialog.
     */
    public fun dismiss()
}

private fun CheckoutWebView.isInViewHierarchy(): Boolean {
    return this.parent != null
}

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
package com.shopify.checkoutkit

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Entrypoint to the library. Allows configuring, preloading, and presenting Shopify checkouts.
 */
public object ShopifyCheckoutKit {

    internal val configuration = Configuration()

    /**
     * Returns the current version of ShopifyCheckoutKit.

     * @return the current version
     */
    public const val version: String = BuildConfig.SDK_VERSION

    /**
     * Returns the currently applied ShopifyKit configuration.
     * Note: configuration changes should be made through the configure function.
     *
     * @return the currently applied configuration
     * @see ShopifyCheckoutKit.configure(ConfigurationUpdater)
     */
    @JvmStatic
    public fun getConfiguration(): Configuration {
        return configuration.copy()
    }

    /**
     * Allows configuring ShopifyCheckoutKit.
     *
     * Current configuration options are for enabling and disabling preloading, and for setting the checkout color scheme.
     * Kotlin example:
     * {@code ShopifyCheckoutKit.configure { it.preloading = Preloading(enabled = enabled) }}
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
     * Preloads a Shopify checkout in the background.
     *
     * Preloading checkout is fully optional, but allows reducing the time taken between calling
     * {@link ShopifyCheckoutKit#present(String, ComponentActivity, CheckoutEventProcessor)} and having a fully interactive checkout.
     * Note: Preload must be called on all cart changes to avoid stale checkouts being presented.
     * Preloaded checkouts also have a TTL of 5 minutes, after checkout will be re-loaded on calling present.
     *
     * @param checkoutUrl The URL of the checkout to be loaded, this can be obtained via the Storefront API
     * @param context The context the checkout is being presented from
     */
    @JvmStatic
    public fun preload(checkoutUrl: String, context: ComponentActivity) {
        if (!configuration.preloading.enabled) return
        CheckoutWebView.clearCache()
        val checkoutWebView = CheckoutWebView.cacheableCheckoutView(checkoutUrl, context)
        checkoutWebView.loadCheckout(checkoutUrl)
    }

    /**
     * Presents a Shopify checkout within a Dialog
     *
     * @param checkoutUrl The URL of the checkout to be presented, this can be obtained via the Storefront API
     * @param context The context the checkout is being presented from
     * @param checkoutEventProcessor provides callbacks to allow clients to listen for and respond to checkout lifecycle events such as
     * (failure, completion, cancellation, external link clicks).
     */
    @JvmStatic
    public fun present(
        checkoutUrl: String,
        context: ComponentActivity,
        checkoutEventProcessor: CheckoutEventProcessor
    ) {
        if (context.isDestroyed || context.isFinishing) {
            return
        }
        val dialog = CheckoutDialog(checkoutUrl, checkoutEventProcessor, context)
        context.lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                dialog.dismiss()
                super.onDestroy(owner)
            }
        })
        dialog.start(context)
    }
}

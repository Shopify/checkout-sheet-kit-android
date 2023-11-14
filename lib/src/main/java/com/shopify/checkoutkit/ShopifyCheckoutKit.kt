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

public object ShopifyCheckoutKit {

    internal var configuration = Configuration()

    /**
     * The version of the `ShopifyCheckoutKit` library.
     */
    public const val version: String = BuildConfig.SDK_VERSION

    /**
     * A convenience function for configuring the `ShopifyCheckoutKit` library.
     */
    @JvmStatic
    public fun configure(setter: ConfigurationUpdater) {
        setter.configure(configuration)
        CheckoutWebView.clearCache()
    }

    /**
     * prefetches the checkout page to preload it, giving the end user a feeling of a more reactive application
     * should be called on all cart changes to refresh. Contains a TTL of 5 minutes and then reloads presentCheckout
     */
    @JvmStatic
    public fun preload(checkoutUrl: String, context: ComponentActivity) {
        if (!configuration.preloading.enabled) return
        CheckoutWebView.clearCache()
        val checkoutWebView = CheckoutWebView.cacheableCheckoutView(checkoutUrl, context)
        checkoutWebView.loadCheckout(checkoutUrl)
    }

    /**
     * show shopify checkout and return a checkout intent that starts the checkout activity
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

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
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log

/**
 * Advanced checkout controller that provides navigation support for custom address
 * and payment screens during the checkout process.
 * 
 * This is an opt-in alternative to [ShopifyCheckoutSheetKit.present] that allows
 * clients to provide custom UI for address and payment selection while maintaining
 * library-controlled navigation and WebView state.
 */
public class ShopifyCheckoutController(
    private val checkoutUrl: String,
    private val checkoutEventProcessor: CheckoutEventProcessor
) {
    /**
     * Factory function to create a screen for address selection.
     * Called when the checkout requires address information.
     * 
     * Currently only supports [CheckoutScreen.FragmentScreen].
     * 
     * @param event The address change event containing type information and response methods
     * @return A [CheckoutScreen] instance defining the UI to present
     */
    public var addressScreen: ((CheckoutAddressChangeIntentEvent) -> CheckoutScreen)? = null

    private var dialog: CheckoutControllerDialog? = null
    private var navigationManager: CheckoutNavigationManager? = null

    /**
     * Present the checkout with navigation support.
     * 
     * @param activity The ComponentActivity context for presentation
     * @return [CheckoutSheetKitDialog] for controlling the presented checkout, 
     *         or `null` if the activity is destroyed/finishing and checkout cannot be presented
     */
    public fun present(activity: ComponentActivity): CheckoutSheetKitDialog? {
        log.d(LOG_TAG, "Present called with checkoutUrl $checkoutUrl.")
        if (activity.isDestroyed || activity.isFinishing) {
            log.d(LOG_TAG, "Context is destroyed or finishing, returning null.")
            return null
        }

        navigationManager = CheckoutNavigationManager()

        dialog = CheckoutControllerDialog(
            checkoutUrl = checkoutUrl,
            checkoutEventProcessor = checkoutEventProcessor,
            context = activity,
            navigationManager = navigationManager!!,
            controller = this
        )

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                log.d(LOG_TAG, "Context is being destroyed, dismissing dialog.")
                dialog?.dismiss()
                super.onDestroy(owner)
            }
        })

        log.d(LOG_TAG, "Starting CheckoutControllerDialog.")
        dialog?.start(activity)
        return CheckoutSheetKitDialog { dialog?.dismiss() }
    }

    internal fun handleAddressChangeIntent(event: CheckoutAddressChangeIntentEvent) {
        log.d(LOG_TAG, "Handling address change intent with type: ${event.addressType}")
        
        val screenProvider = addressScreen
        if (screenProvider == null) {
            log.w(LOG_TAG, "No addressScreen provider configured, falling back to default behavior")
            event.cancel()
            return
        }

        try {
            val screen = screenProvider(event)
            // Dispatch to main thread since JavaScript bridge calls happen on background thread
            Handler(Looper.getMainLooper()).post {
                val navigationSuccess = navigationManager?.navigateToCustomScreen(screen) ?: false
                if (!navigationSuccess) {
                    log.e(LOG_TAG, "Navigation to custom screen failed")
                    event.cancel()
                }
            }
        } catch (e: Exception) {
            log.e(LOG_TAG, "Failed to create address screen", e)
            event.cancel()
        }
    }

    private companion object {
        private const val LOG_TAG = "ShopifyCheckoutController"
    }
}

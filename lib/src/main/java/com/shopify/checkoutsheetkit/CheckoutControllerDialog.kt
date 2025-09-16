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
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import android.widget.FrameLayout
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log

/**
 * Extended checkout dialog that supports navigation to custom screens.
 * Reuses CheckoutDialog functionality while adding navigation capabilities.
 */
internal class CheckoutControllerDialog(
    checkoutUrl: String,
    checkoutEventProcessor: CheckoutEventProcessor,
    context: Context,
    private val navigationManager: CheckoutNavigationManager,
    private val controller: ShopifyCheckoutController
) : CheckoutDialog(checkoutUrl, checkoutEventProcessor, context) {

    override fun start(context: ComponentActivity) {
        log.d(LOG_TAG, "CheckoutControllerDialog start called.")
        
        // Use the enhanced layout with navigation container
        setContentView(R.layout.dialog_checkout_controller)
        
        // Call parent setup which will create the basic event processor
        setupDialog(context)
        
        // Now replace the event processor with our custom one
        replaceEventProcessor()
        
        // Setup navigation after parent setup is complete
        setupNavigation()
    }

    private fun replaceEventProcessor() {
        val webView = getCurrentCheckoutWebView()
        if (webView != null) {
            log.d(LOG_TAG, "Replacing event processor with controller-enabled version")
            
            val customEventProcessor = object : CheckoutWebViewEventProcessor(
                eventProcessor = checkoutEventProcessor,
                toggleHeader = ::toggleHeader,
                closeCheckoutDialogWithError = ::closeCheckoutDialogWithError,
                setProgressBarVisibility = ::setProgressBarVisibility,
                updateProgressBarPercentage = ::updateProgressBarPercentage,
                addressChangeHandler = { event ->
                    log.d(LOG_TAG, "Address change intent received, type: ${event.addressType}")
                    // Set the current event in navigation manager for close button handling
                    navigationManager.setCurrentEvent(event)
                    controller.handleAddressChangeIntent(event)
                }
            ) {
                override fun onAddressResponseComplete() {
                    log.d(LOG_TAG, "Address response complete, navigating back to checkout")
                    navigationManager.navigateBackToCheckout()
                }

                override fun onAddressCancelled() {
                    log.d(LOG_TAG, "Address cancelled, navigating back to checkout") 
                    navigationManager.navigateBackToCheckout()
                }
            }
            
            // Replace the event processor that was set by parent
            webView.setEventProcessor(customEventProcessor)
        } else {
            log.e(LOG_TAG, "Could not replace event processor - WebView not found")
        }
    }

    private fun setupNavigation() {
        val webViewContainer = findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        val navigationContainer = findViewById<FrameLayout>(R.id.checkoutNavigationContainer)

        log.d(LOG_TAG, "Setting up navigation - webViewContainer: $webViewContainer, navigationContainer: $navigationContainer")

        if (navigationContainer == null) {
            log.e(LOG_TAG, "Navigation container not found! Check if dialog_checkout_controller.xml is properly inflated.")
            return
        }

        // Get the checkout WebView that was created by parent
        val checkoutWebView = getCurrentCheckoutWebView()
        
        if (checkoutWebView != null) {
            navigationManager.initialize(
                webViewContainer = webViewContainer,
                navigationContainer = navigationContainer,
                checkoutWebView = checkoutWebView,
                onTitleChanged = { newTitle ->
                    log.d(LOG_TAG, "Updating dialog title to: $newTitle")
                    // Update the toolbar title directly instead of dialog title
                    val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.checkoutSdkHeader)
                    toolbar?.title = newTitle ?: ""
                },
                getCurrentTitle = {
                    val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.checkoutSdkHeader)
                    toolbar?.title?.toString()
                }
            )
            log.d(LOG_TAG, "Navigation setup completed")
        } else {
            log.e(LOG_TAG, "Failed to setup navigation - no WebView found")
        }
    }

    companion object {
        private const val LOG_TAG = "CheckoutControllerDialog"
    }
}

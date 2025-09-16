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

import android.view.View
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import java.lang.Boolean.TRUE

/**
 * Manages navigation between the checkout WebView and custom screens.
 * Handles view transitions while preserving the WebView's bridge connection.
 */
internal class CheckoutNavigationManager {
    private var webViewContainer: View? = null
    private var navigationContainer: FrameLayout? = null
    private var checkoutWebView: BaseWebView? = null
    private var currentFragment: Fragment? = null
    private var isWebViewPaused = false
    private var currentEvent: CheckoutAddressChangeIntentEvent? = null
    private var onTitleChanged: ((String?) -> Unit)? = null
    private var originalTitle: String? = null
    private var getCurrentTitle: (() -> String?)? = null

    /**
     * Initialize the navigation manager with the required components.
     */
    fun initialize(
        webViewContainer: View,
        navigationContainer: FrameLayout,
        checkoutWebView: BaseWebView,
        onTitleChanged: ((String?) -> Unit)? = null,
        getCurrentTitle: (() -> String?)? = null
    ) {
        this.webViewContainer = webViewContainer
        this.navigationContainer = navigationContainer
        this.checkoutWebView = checkoutWebView
        this.onTitleChanged = onTitleChanged
        this.getCurrentTitle = getCurrentTitle
        
        // Ensure navigation container is initially hidden
        navigationContainer.visibility = View.GONE
        log.d(LOG_TAG, "Navigation manager initialized")
    }

    /**
     * Navigate to a custom screen, hiding the WebView while preserving its state.
     * @return true if navigation was successful, false if it failed
     */
    fun navigateToCustomScreen(screen: CheckoutScreen): Boolean {
        return when (screen) {
            is CheckoutScreen.FragmentScreen -> {
                navigateToFragment(screen.fragment, screen.config)
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment, config: CheckoutScreenConfig): Boolean {
        val container = navigationContainer
        if (container == null) {
            log.e(LOG_TAG, "Navigation container not initialized - cannot navigate to fragment")
            return false
        }

        log.d(LOG_TAG, "Navigating to fragment: ${fragment::class.java.simpleName}")

        try {
            // Apply UI configuration
            applyScreenConfig(config)

            // Pause WebView to preserve resources while maintaining bridge connection
            pauseWebView()

            // Store current fragment for cleanup later
            currentFragment = fragment

            // Hide WebView container and show navigation container
            webViewContainer?.visibility = View.GONE
            container.visibility = View.VISIBLE

            // Create fragment view directly without FragmentManager
            val fragmentView = fragment.onCreateView(
                android.view.LayoutInflater.from(container.context),
                container,
                null
            )
            
            if (fragmentView != null) {
                container.removeAllViews()
                container.addView(fragmentView)
                fragment.onViewCreated(fragmentView, null)
                log.d(LOG_TAG, "Fragment navigation completed successfully")
                return true
            } else {
                log.e(LOG_TAG, "Fragment.onCreateView returned null - navigation failed")
                currentFragment = null
                return false
            }
        } catch (e: Exception) {
            log.e(LOG_TAG, "Fragment navigation failed with exception", e)
            currentFragment = null
            // Restore WebView visibility on error
            container.visibility = View.GONE
            webViewContainer?.visibility = View.VISIBLE
            resumeWebView()
            return false
        }
    }

    /**
     * Navigate back to the checkout WebView from a custom screen.
     */
    fun navigateBackToCheckout() {
        val container = navigationContainer ?: return

        log.d(LOG_TAG, "Navigating back to checkout WebView")

        // Restore original title if it was changed
        restoreOriginalTitle()

        // Hide navigation container and show WebView container
        container.visibility = View.GONE
        webViewContainer?.visibility = View.VISIBLE

        // Clear the container view and reset current fragment
        container.removeAllViews()
        currentFragment = null

        // Resume WebView
        resumeWebView()
    }


    /**
     * Handle cancellation from custom screen.
     */
    fun handleCancel() {
        log.d(LOG_TAG, "Handling cancel from custom screen")
        navigateBackToCheckout()
    }

    private fun pauseWebView() {
        if (!isWebViewPaused) {
            checkoutWebView?.onPause()
            isWebViewPaused = TRUE
            log.d(LOG_TAG, "WebView paused")
        }
    }

    private fun resumeWebView() {
        if (isWebViewPaused) {
            checkoutWebView?.onResume()
            isWebViewPaused = false
            log.d(LOG_TAG, "WebView resumed")
        }
    }

    private fun applyScreenConfig(config: CheckoutScreenConfig) {
        log.d(LOG_TAG, "Applying screen config: ${config.title}")
        config.title?.let { newTitle ->
            // Store original title if we haven't already
            if (originalTitle == null) {
                originalTitle = getCurrentTitle()
                log.d(LOG_TAG, "Stored original title: $originalTitle")
            }
            log.d(LOG_TAG, "Calling onTitleChanged with: $newTitle")
            onTitleChanged?.invoke(newTitle)
            log.d(LOG_TAG, "Title change callback completed")
        } ?: run {
            log.d(LOG_TAG, "No title in config, skipping title change")
        }
    }

    private fun restoreOriginalTitle() {
        originalTitle?.let { title ->
            onTitleChanged?.invoke(title)
            log.d(LOG_TAG, "Restored original title: $title")
            originalTitle = null
        }
    }

    private fun getCurrentTitle(): String? {
        return getCurrentTitle?.invoke()
    }

    /**
     * Set the current event for close button handling.
     */
    fun setCurrentEvent(event: CheckoutAddressChangeIntentEvent?) {
        this.currentEvent = event
    }

    /**
     * Handle close button press - cancels the current event.
     */
    fun handleClosePressed() {
        currentEvent?.cancel() ?: run {
            log.d(LOG_TAG, "Close pressed but no current event to cancel")
            navigateBackToCheckout()
        }
    }

    companion object {
        private const val LOG_TAG = "CheckoutNavigationManager"
    }
}

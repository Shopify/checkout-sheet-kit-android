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

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log

internal class CheckoutDialog(
    private val checkoutUrl: String,
    private val checkoutEventProcessor: CheckoutEventProcessor,
    context: Context,
    private val options: CheckoutOptions,
) : Dialog(context) {

    fun start(context: ComponentActivity) {
        log.d(LOG_TAG, "Dialog start called.")
        setContentView(R.layout.dialog_checkout)
        window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        // Although this flag is deprecated in newest targets, it's properly
        // addressing the keyboard focus on the WebView within the dialog.
        // The non-deprecated alternative (insets listener) does notify about
        // keyboard insets when visible, but it is not adjusting the pan
        // properly into the fields. To be investigated further.
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        log.d(LOG_TAG, "Finding or creating new WebView.")
        val checkoutWebView = CheckoutWebView.cacheableCheckoutView(
            checkoutUrl,
            context,
            options = options,
        )

        checkoutWebView.onResume()
        log.d(LOG_TAG, "Setting event processor on WebView.")
        checkoutWebView.setEventProcessor(eventProcessor())

        val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
        log.d(LOG_TAG, "Configured colorScheme $colorScheme")
        findViewById<Toolbar>(R.id.checkoutSdkHeader).apply {
            log.d(LOG_TAG, "Applying configured header colors and inflating menu.")
            setBackgroundColor(colorScheme.headerBackgroundColor())
            setTitleTextColor(colorScheme.headerFontColor())
            inflateMenu(R.menu.checkout_menu)
            menu.findItem(R.id.checkoutSdkCloseBtn).apply { setupCloseButton(colorScheme) }
        }

        findViewById<ProgressBar>(R.id.progressBar).apply {
            log.d(LOG_TAG, "Setting progress tint.")
            progressTintList = ColorStateList.valueOf(colorScheme.progressIndicatorColor())
            if (checkoutWebView.hasFinishedLoading()) {
                log.d(LOG_TAG, "Page has finished loading, hiding progress bar.")
                this.visibility = INVISIBLE
            }
        }

        addWebViewToContainer(colorScheme, checkoutWebView)
        setOnCancelListener {
            log.d(LOG_TAG, "Cancel listener invoked, invoking onCheckoutCanceled.")
            CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
            checkoutEventProcessor.onCancel()
        }

        setOnDismissListener {
            log.d(LOG_TAG, "Dismiss listener invoked.")
            removeWebViewFromContainer()
        }

        setOnShowListener {
            log.d(LOG_TAG, "On show listener invoked, calling WebView notifyPresented.")
//            checkoutWebView?.notifyPresented()
        }

        log.d(LOG_TAG, "Showing dialog.")
        show()
    }

    private fun MenuItem.setupCloseButton(colorScheme: ColorScheme) {
        val customCloseIcon = colorScheme.closeIcon(context.isDarkTheme())
        if (customCloseIcon != null) {
            log.d(LOG_TAG, "Setting custom menu item drawable.")
            this.icon = AppCompatResources.getDrawable(context, customCloseIcon.id)
        } else {
            val customTint = colorScheme.closeIconTint(context.isDarkTheme())
            val icon = this.icon
            if (customTint != null && icon != null) {
                log.d(LOG_TAG, "Setting menu item tint.")
                val wrappedDrawable = DrawableCompat.wrap(icon)
                DrawableCompat.setTint(wrappedDrawable.mutate(), customTint.getValue(context))
            }
        }

        setOnMenuItemClickListener {
            log.d(LOG_TAG, "Menu click cancel invoked.")
            cancel()
            true
        }
    }

    private fun removeWebViewFromContainer() {
        findViewById<RelativeLayout>(R.id.checkoutSdkContainer).apply {
            this.children.firstOrNull { it is WebView }?.let { webView ->
                log.d(LOG_TAG, "Removing WebView from container.")
                this.removeView(webView)
            }
        }
    }

    private fun addWebViewToContainer(
        colorScheme: ColorScheme,
        checkoutWebView: BaseWebView,
    ) {
        findViewById<RelativeLayout>(R.id.checkoutSdkContainer).apply {
            log.d(LOG_TAG, "Found parent view, setting its colors and layout params.")
            setBackgroundColor(colorScheme.webViewBackgroundColor())
            val layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            layoutParams.addRule(RelativeLayout.BELOW, R.id.progressBar)
            checkoutWebView.removeFromParent()
            log.d(LOG_TAG, "Adding WebView to parent view.")
            addView(checkoutWebView, layoutParams)
        }
    }

    private fun toggleHeader(modalVisible: Boolean) {
        Handler(Looper.getMainLooper()).post {
            log.d(LOG_TAG, "Toggling header based on modal visibility state. Modal visible: $modalVisible.")
            findViewById<Toolbar>(R.id.checkoutSdkHeader).visibility = if (modalVisible) GONE else VISIBLE
            findViewById<ProgressBar>(R.id.progressBar).visibility = if (modalVisible) GONE else INVISIBLE
        }
    }

    private fun updateProgressBarPercentage(percentage: Int) {
        log.d(LOG_TAG, "Updating progress bar percentage, $percentage.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            findViewById<ProgressBar>(R.id.progressBar).setProgress(percentage, true)
        } else {
            findViewById<ProgressBar>(R.id.progressBar).progress = percentage
        }
    }

    private fun setProgressBarVisibility(visibility: Int) {
        log.d(LOG_TAG, "Setting progress bar visibility $visibility.")
        findViewById<ProgressBar>(R.id.progressBar).visibility = visibility
    }

    internal fun closeCheckoutDialogWithError(exception: CheckoutException) {
        log.d(LOG_TAG, "Closing dialog with error, marking cache entry stale, calling onCheckoutFailed.")
        CheckoutWebView.markCacheEntryStale()
        checkoutEventProcessor.onFail(exception)

        val isOneTimeUseUrl = this.checkoutUrl.isOneTimeUse()
        val shouldRecover = ShopifyCheckoutSheetKit.configuration.errorRecovery.shouldRecoverFromError(exception)

        log.d(LOG_TAG, "One time use checkout URL?: $isOneTimeUseUrl, should recover?: $shouldRecover.")
        if (!isOneTimeUseUrl && shouldRecover) {
            log.d(LOG_TAG, "Attempting to recover from error.")
            attemptToRecoverFromError(exception)
        } else {
            log.d(LOG_TAG, "Not attempting to recover, dismissing sheet.")
            dismiss()
        }
    }

    internal fun attemptToRecoverFromError(exception: CheckoutException): Boolean {
        removeWebViewFromContainer()

        log.d(LOG_TAG, "Invoking pre-recovery actions.")
        ShopifyCheckoutSheetKit.configuration.errorRecovery.preRecoveryActions(exception, checkoutUrl)

        log.d(LOG_TAG, "Adding fallback WebView to container.")
        addWebViewToContainer(
            ShopifyCheckoutSheetKit.configuration.colorScheme,
            FallbackWebView(context).apply {
                setEventProcessor(eventProcessor())
                loadCheckout(checkoutUrl, options)
            }
        )
        return true
    }

    private fun eventProcessor(): CheckoutWebViewEventProcessor {
        return CheckoutWebViewEventProcessor(
            eventProcessor = checkoutEventProcessor,
            toggleHeader = ::toggleHeader,
            closeCheckoutDialogWithError = ::closeCheckoutDialogWithError,
            setProgressBarVisibility = ::setProgressBarVisibility,
            updateProgressBarPercentage = ::updateProgressBarPercentage,
        )
    }

    @ColorInt
    private fun ColorScheme.headerBackgroundColor() =
        this.headerBackgroundColor(context.isDarkTheme()).getValue(context)

    @ColorInt
    private fun ColorScheme.webViewBackgroundColor() =
        this.webViewBackgroundColor(context.isDarkTheme()).getValue(context)

    @ColorInt
    private fun ColorScheme.headerFontColor() =
        this.headerFontColor(context.isDarkTheme()).getValue(context)

    @ColorInt
    private fun ColorScheme.progressIndicatorColor() =
        this.progressIndicatorColor(context.isDarkTheme()).getValue(context)

    private fun Context.isDarkTheme() =
        resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES

    companion object {
        private const val LOG_TAG = "CheckoutDialog"
    }
}

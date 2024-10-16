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
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children

internal class CheckoutDialog(
    private val checkoutUrl: String,
    private val checkoutEventProcessor: CheckoutEventProcessor,
    context: Context,
) : Dialog(context) {

    fun start(context: ComponentActivity) {
        setContentView(R.layout.dialog_checkout)
        window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Although this flag is deprecated in newest targets, it's properly
        // addressing the keyboard focus on the WebView within the dialog.
        // The non-deprecated alternative (insets listener) does notify about
        // keyboard insets when visible, but it is not adjusting the pan
        // properly into the fields. To be investigated further.
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val checkoutWebView = CheckoutWebView.cacheableCheckoutView(
            checkoutUrl,
            context,
        )

        checkoutWebView.onResume()
        checkoutWebView.setEventProcessor(eventProcessor())

        val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
        val header = findViewById<Toolbar>(R.id.checkoutSdkHeader)

        header.apply {
            setBackgroundColor(colorScheme.headerBackgroundColor())
            setTitleTextColor(colorScheme.headerFontColor())
            inflateMenu(R.menu.checkout_menu)
        }

        findViewById<ProgressBar>(R.id.progressBar).apply {
            progressTintList = ColorStateList.valueOf(colorScheme.progressIndicatorColor())
            if (checkoutWebView.hasFinishedLoading()) {
                this.visibility = INVISIBLE
            }
        }

        addWebViewToContainer(colorScheme, checkoutWebView)
        setOnCancelListener {
            CheckoutWebViewContainer.retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
            checkoutEventProcessor.onCheckoutCanceled()
        }

        setOnDismissListener {
            removeWebViewFromContainer()
        }

        header.setOnMenuItemClickListener {
            cancel()
            true
        }

        setOnShowListener {
            checkoutWebView.notifyPresented()
        }

        show()
    }

    private fun removeWebViewFromContainer() {
        findViewById<RelativeLayout>(R.id.checkoutSdkContainer).apply {
            this.children.firstOrNull { it is WebView }?.let { webView ->
                this.removeView(webView)
            }
        }
    }

    private fun addWebViewToContainer(
        colorScheme: ColorScheme,
        checkoutWebView: BaseWebView,
    ) {
        findViewById<RelativeLayout>(R.id.checkoutSdkContainer).apply {
            setBackgroundColor(colorScheme.webViewBackgroundColor())
            val layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            layoutParams.addRule(RelativeLayout.BELOW, R.id.progressBar)
            checkoutWebView.removeFromParent()
            addView(checkoutWebView, layoutParams)
        }
    }

    private fun toggleHeader(modalVisible: Boolean) {
        Handler(Looper.getMainLooper()).post {
            findViewById<Toolbar>(R.id.checkoutSdkHeader).visibility = if (modalVisible) GONE else VISIBLE
            findViewById<ProgressBar>(R.id.progressBar).visibility = if (modalVisible) GONE else INVISIBLE
        }
    }

    private fun updateProgressBarPercentage(percentage: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            findViewById<ProgressBar>(R.id.progressBar).setProgress(percentage, true)
        } else {
            findViewById<ProgressBar>(R.id.progressBar).progress = percentage
        }
    }

    private fun setProgressBarVisibility(visibility: Int) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = visibility
    }

    internal fun closeCheckoutDialogWithError(exception: CheckoutException) {
        checkoutEventProcessor.onCheckoutFailed(exception)
        if (!this.checkoutUrl.isOneTimeUse() &&
            ShopifyCheckoutSheetKit.configuration.errorRecovery.shouldRecoverFromError(exception)) {
            attemptToRecoverFromError(exception)
        } else {
            dismiss()
        }
    }

    internal fun attemptToRecoverFromError(exception: CheckoutException): Boolean {
        removeWebViewFromContainer()

        ShopifyCheckoutSheetKit.configuration.errorRecovery.preRecoveryActions(exception, checkoutUrl)

        addWebViewToContainer(
            ShopifyCheckoutSheetKit.configuration.colorScheme,
            FallbackWebView(context).apply {
                setEventProcessor(eventProcessor())
                loadUrl(checkoutUrl)
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
}

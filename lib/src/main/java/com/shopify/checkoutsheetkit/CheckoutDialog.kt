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
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
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

        checkoutWebView.setEventProcessor(
            CheckoutWebViewEventProcessor(
                eventProcessor = checkoutEventProcessor,
                toggleHeader = ::toggleHeader,
                closeCheckoutDialog = ::cancel,
                hideProgressBar = ::hideProgressBar,
            )
        )

        val colorScheme = ShopifyCheckoutSheetKit.configuration.colorScheme
        val header = findViewById<Toolbar>(R.id.checkoutSdkHeader)

        header.apply {
            setBackgroundColor(colorScheme.headerBackgroundColor())
            setTitleTextColor(colorScheme.headerFontColor())
            inflateMenu(R.menu.checkout_menu)
        }

        findViewById<FrameLayout>(R.id.checkoutSdkLoadingSpinner).apply {
            setBackgroundColor(colorScheme.webViewBackgroundColor())
        }

        findViewById<ProgressBar>(R.id.checkoutSdkLoadingSpinnerProgressBar).apply {
            indeterminateDrawable.setTint(colorScheme.loadingSpinnerColor())
            indeterminateDrawable.colorFilter = PorterDuffColorFilter(
                colorScheme.loadingSpinnerColor(), PorterDuff.Mode.SRC_ATOP
            )
        }

        findViewById<FrameLayout>(R.id.checkoutSdkContainer).apply {
            setBackgroundColor(colorScheme.webViewBackgroundColor())
            if (checkoutWebView.hasFinishedLoading()) {
                this.visibility = VISIBLE
            }
        }.addView(checkoutWebView)

        setOnCancelListener {
            checkoutEventProcessor.onCheckoutCanceled()
            checkoutWebView.parent?.let {
                (checkoutWebView.parent as ViewGroup).removeView(checkoutWebView)
            }
        }

        header.setOnMenuItemClickListener {
            cancel()
            true
        }

        setOnShowListener {
            checkoutWebView.notifyPresented()
            scrollTop()
            if (checkoutWebView.hasFinishedLoading()) {
                hideProgressBar()
            }
        }

        show()
    }

    private fun toggleHeader(modalVisible: Boolean) {
        Handler(Looper.getMainLooper()).post {
            val visibility = if (modalVisible) GONE else VISIBLE
            findViewById<Toolbar>(R.id.checkoutSdkHeader).visibility = visibility
        }
    }

    private fun scrollTop() {
        findViewById<CheckoutWebViewContainer>(R.id.checkoutSdkContainer).apply {
            children.firstOrNull()?.scrollY = 0
        }
    }

    private fun hideProgressBar() {
        findViewById<FrameLayout>(R.id.checkoutSdkLoadingSpinner).visibility = GONE
        findViewById<CheckoutWebViewContainer>(R.id.checkoutSdkContainer).visibility = VISIBLE
        scrollTop()
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
    private fun ColorScheme.loadingSpinnerColor() =
        this.loadingSpinnerColor(context.isDarkTheme()).getValue(context)

    private fun Context.isDarkTheme() =
        resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

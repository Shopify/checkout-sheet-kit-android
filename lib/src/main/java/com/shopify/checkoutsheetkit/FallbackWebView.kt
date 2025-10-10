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
import android.net.Uri
import android.util.AttributeSet
import android.webkit.WebView
import androidx.core.net.toUri
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.lifecycleevents.emptyCompletedEvent

internal class FallbackWebView(context: Context, attributeSet: AttributeSet? = null) :
    BaseWebView(context, attributeSet) {

    override val recoverErrors = false

    init {
        log.d(LOG_TAG, "Initializing fallback web view.")
        webViewClient = FallbackWebViewClient()
    }

    private var checkoutEventProcessor = CheckoutWebViewEventProcessor(NoopEventProcessor())

    fun setEventProcessor(processor: CheckoutWebViewEventProcessor) {
        log.d(LOG_TAG, "Setting event processor $processor.")
        this.checkoutEventProcessor = processor
    }

    override fun getEventProcessor(): CheckoutWebViewEventProcessor {
        return checkoutEventProcessor
    }

    inner class FallbackWebViewClient : BaseWebView.BaseWebViewClient() {

        private val typRegex = Regex(pattern = "^(thank[-_]+you)$", option = RegexOption.IGNORE_CASE)

        init {
            if (BuildConfig.DEBUG) {
                log.d(LOG_TAG, "Setting web contents debugging enabled.")
                setWebContentsDebuggingEnabled(true)
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            log.d(LOG_TAG, "onPageFinished called.")
            getEventProcessor().onCheckoutViewLoadComplete()

            val uri = url.toUri()
            if (isConfirmation(uri)) {
                log.d(LOG_TAG, "Finished page has confirmationUrl. Emitting minimal checkout completed event.")
                getEventProcessor().onCheckoutViewComplete(
                    emptyCompletedEvent(id = getOrderIdFromQueryString(uri))
                )
            }
        }

        private fun getOrderIdFromQueryString(uri: Uri): String? = uri.getQueryParameter("order_id")
        private fun isConfirmation(uri: Uri) = uri.pathSegments.any { pathSegment -> typRegex.matches(pathSegment) }
    }

    companion object {
        private const val LOG_TAG = "FallbackWebView"
    }
}

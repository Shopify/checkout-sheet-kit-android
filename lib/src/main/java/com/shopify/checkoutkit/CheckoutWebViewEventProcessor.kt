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

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.shopify.checkoutkit.events.AnalyticsEvent

/**
 * Event processor that can handle events internally, delegate to the CheckoutEventProcessor
 * passed into ShopifyCheckoutKit.present(), or preprocess arguments and then delegate
 */
internal class CheckoutWebViewEventProcessor(
    private val eventProcessor: CheckoutEventProcessor,
    private val toggleHeader: (Boolean) -> Unit = {},
    private val closeCheckoutDialog: () -> Unit = {},
    private val hideProgressBar: () -> Unit = {},
) {
    fun onCheckoutViewComplete() {
        eventProcessor.onCheckoutCompleted()
    }

    fun onCheckoutViewModalToggled(modalVisible: Boolean) {
        onMainThread {
            toggleHeader(modalVisible)
        }
    }

    fun onCheckoutViewLinkClicked(uri: Uri) {
        eventProcessor.onCheckoutLinkClicked(uri)
    }

    fun onCheckoutViewFailedWithError(error: CheckoutException) {
        eventProcessor.onCheckoutFailed(error)
        onMainThread {
            closeCheckoutDialog()
        }
    }

    fun onCheckoutViewLoadComplete() {
        onMainThread {
            hideProgressBar()
        }
    }

    fun onAnalyticsEvent(event: AnalyticsEvent) {
        eventProcessor.onAnalyticsEvent(event)
    }

    private fun onMainThread(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            block()
        }
    }
}

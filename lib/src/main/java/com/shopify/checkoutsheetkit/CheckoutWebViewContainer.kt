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
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

internal class CheckoutWebViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    // Clear the cache whenever the WebView is removed from it's container
    // We should only clear the cache and destroy the WebView after it's been removed from it's parent
    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        if (child is CheckoutWebView) {
           if (retainCacheEntry == RetainCacheEntry.IF_NOT_STALE && CheckoutWebView.cacheEntry?.isStale == true) {
               CheckoutWebView.clearCache()
           }
        }

        if (child is FallbackWebView) {
            child.destroy()
        }

        retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
    }

    companion object {
        internal var retainCacheEntry = RetainCacheEntry.IF_NOT_STALE
    }
}

internal enum class RetainCacheEntry {
    /**
     * Retain a WebView in the cache if it is not stale
     */
    IF_NOT_STALE,
    /**
     * Always retain the WebView in the cache, regardless of whether it is stale or not
     */
    YES,
}

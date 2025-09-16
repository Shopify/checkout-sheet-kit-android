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

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * UI configuration for custom checkout screens.
 * 
 * @param title The title to display in the toolbar (null keeps current title)
 * @param titleRes String resource ID for the title (takes precedence over title if both provided)
 */
public data class CheckoutScreenConfig(
    val title: String? = null,
    @StringRes val titleRes: Int? = null
) {
    public companion object {
        /**
         * Create a config with a string resource title.
         * 
         * @param titleRes String resource ID for the title
         * @return CheckoutScreenConfig with the specified title resource
         */
        @JvmStatic
        public fun withTitle(@StringRes titleRes: Int): CheckoutScreenConfig {
            return CheckoutScreenConfig(titleRes = titleRes)
        }
        
        /**
         * Create a config with a direct string title.
         * 
         * @param title Direct string for the title
         * @return CheckoutScreenConfig with the specified title string
         */
        @JvmStatic
        public fun withTitle(title: String): CheckoutScreenConfig {
            return CheckoutScreenConfig(title = title)
        }
    }
}

/**
 * Represents different types of screens that can be presented during checkout for
 * custom address or payment selection.
 * 
 * Note: This sealed class is designed for future extensibility. Additional screen types
 * (Activity, Composable) will be added in future versions.
 */
public sealed class CheckoutScreen {
    /**
     * A screen implemented as an Android Fragment.
     * 
     * @param view The fragment to be presented
     * @param config UI configuration for this screen
     */
    public data class Fragment(
        val view: androidx.fragment.app.Fragment, 
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()
}
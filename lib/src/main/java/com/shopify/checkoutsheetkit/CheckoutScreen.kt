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

import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * UI configuration for custom checkout screens.
 * 
 * @param title The title to display in the toolbar (null keeps current title)
 */
public data class CheckoutScreenConfig(
    val title: String? = null
)

/**
 * Represents different types of screens that can be presented during checkout for
 * custom address or payment selection.
 */
public sealed class CheckoutScreen {
    /**
     * A screen implemented as an Android Fragment.
     * 
     * @param fragment The fragment to be presented
     * @param config UI configuration for this screen
     */
    public data class FragmentScreen(
        val fragment: Fragment, 
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()

    /**
     * A screen implemented as an Android Activity.
     * Currently not supported - will be added in a future version.
     * 
     * @param intent The intent to launch the activity
     * @param config UI configuration for this screen
     */
    public data class ActivityScreen(
        val intent: Intent,
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()

    /**
     * A screen implemented using Jetpack Compose.
     * Currently not supported - will be added in a future version.
     */
    public data class ComposableScreen(
        val content: Any, // Placeholder for @Composable function type
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()
}
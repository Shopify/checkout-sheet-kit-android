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

/**
 * Configuration for Shopify Checkout Sheet Kit.
 *
 * Allows:
 * - Enabling/disabling preloading,
 * - Specifying the colorScheme that should be used for checkout,
 * - Drawing the sheet edge-to-edge, behind the system bars.
 */
public data class Configuration internal constructor(
    var colorScheme: ColorScheme = ColorScheme.Automatic(),
    var preloading: Preloading = Preloading(),
    var errorRecovery: ErrorRecovery = object : ErrorRecovery {},
    var platform: Platform? = null,
    var logLevel: LogLevel = LogLevel.WARN,
    var edgeToEdge: EdgeToEdge = EdgeToEdge(),
)

/**
 * Configuration related to preloading.
 *
 * Initially allows toggling the preloading feature.
 */
public data class Preloading(
    val enabled: Boolean = true
)

/**
 * Configuration related to drawing the sheet edge-to-edge.
 *
 * When enabled, the sheet fills the screen and draws behind the status bar, navigation bar and
 * display cutout. The header is padded by the top insets and takes the header background behind
 * the status bar; the checkout is padded by the bottom insets (or the keyboard, whichever is
 * taller) and takes the WebView background behind the navigation bar. System bar icons are made
 * light or dark to stay legible on those backgrounds.
 *
 * Disabled by default, which keeps the sheet sized and placed by the dialog's window theme.
 */
public data class EdgeToEdge(
    val enabled: Boolean = false
)

public enum class LogLevel {
    DEBUG, WARN, ERROR
}

public interface ErrorRecovery {
    public fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
        // logging or pre-recovery cleanup can be added here
    }

    public fun shouldRecoverFromError(checkoutException: CheckoutException): Boolean {
        return checkoutException.isRecoverable
    }
}

public enum class Platform(public val displayName: String) {
    REACT_NATIVE("ReactNative")
}

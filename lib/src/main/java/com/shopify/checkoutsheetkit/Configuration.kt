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
 * - Specifying the colorScheme that should be used for checkout.
 */
public data class Configuration internal constructor(
    var colorScheme: ColorScheme = ColorScheme.Automatic(),
    var preloading: Preloading = Preloading(),
    var errorRecovery: ErrorRecovery = object : ErrorRecovery {},
    var platform: Platform? = null,
    var logLevel: LogLevel = LogLevel.WARN,
)

/**
 * Configuration related to preloading.
 *
 * Initially allows toggling the preloading feature.
 */
public data class Preloading(
    val enabled: Boolean = true
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

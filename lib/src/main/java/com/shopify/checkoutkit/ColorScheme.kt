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

import kotlinx.serialization.Serializable

@Serializable
public sealed class ColorScheme(public val id: String) {
    @Serializable
    public class Light(public var colors: Colors = defaultLightColors) : ColorScheme("light")

    @Serializable
    public class Dark(public var colors: Colors = defaultDarkColors) : ColorScheme("dark")

    @Serializable
    public class Automatic(
        internal var lightColors: Colors = defaultLightColors,
        internal var darkColors: Colors = defaultDarkColors
    ) : ColorScheme("automatic")

    @Serializable
    public class Web(public var colors: Colors = defaultLightColors) : ColorScheme("web_default")

    internal fun headerBackgroundColor(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.headerBackground else this.lightColors.headerBackground
        is Dark -> this.colors.headerBackground
        is Light -> this.colors.headerBackground
        is Web -> this.colors.headerBackground
    }

    internal fun webViewBackgroundColor(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.webViewBackground else this.lightColors.webViewBackground
        is Dark -> this.colors.webViewBackground
        is Light -> this.colors.webViewBackground
        is Web -> this.colors.webViewBackground
    }

    internal fun headerFontColor(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.headerFont else this.lightColors.headerFont
        is Dark -> this.colors.headerFont
        is Light -> this.colors.headerFont
        is Web -> this.colors.headerFont
    }

    internal fun loadingSpinnerColor(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.spinnerColor else this.lightColors.spinnerColor
        is Dark -> this.colors.spinnerColor
        is Light -> this.colors.spinnerColor
        is Web -> this.colors.spinnerColor
    }
}

@Serializable
public data class Colors(
    val webViewBackground: Int,
    val headerBackground: Int,
    val headerFont: Int,
    val spinnerColor: Int,
)

private val defaultLightColors = Colors(
    webViewBackground = R.color.checkoutLightBg,
    headerBackground = R.color.checkoutLightBg,
    headerFont = R.color.checkoutLightFont,
    spinnerColor = R.color.checkoutLightLoadingSpinner,
)

private val defaultDarkColors = Colors(
    webViewBackground = R.color.checkoutDarkBg,
    headerBackground = R.color.checkoutDarkBg,
    headerFont = R.color.checkoutDarkFont,
    spinnerColor = R.color.checkoutDarkLoadingSpinner,
)

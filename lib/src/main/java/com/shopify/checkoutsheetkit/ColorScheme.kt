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
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.getColor
import kotlinx.serialization.Serializable

/**
 * The color scheme to be used when presenting checkout.
 *
 * This determines the color of both web elements (displayed in the WebView) as well as native elements
 * such as the header background and header font
 */
@Serializable
public sealed class ColorScheme(public val id: String) {
    /**
     * Always show checkout in an idiomatic light color scheme.
     */
    @Serializable
    public data class Light(public var colors: Colors = defaultLightColors) : ColorScheme("light")

    /**
     * Always show checkout in an idiomatic dark color scheme.
     */
    @Serializable
    public data class Dark(public var colors: Colors = defaultDarkColors) : ColorScheme("dark")

    /**
     * Automatically toggle between idiomatic light and dark color schemes based on device preference.
     * @property lightColors The color scheme to use when the device is in light mode.
     * @property darkColors The color scheme to use when the device is in dark mode.
     */
    @Serializable
    public data class Automatic(
        public var lightColors: Colors = defaultLightColors,
        public var darkColors: Colors = defaultDarkColors
    ) : ColorScheme("automatic")

    /**
     * Show checkout in a color scheme matching the store's web/desktop checkout branding.
     * This is especially useful for stores where checkout branding settings have been customised.
     */
    @Serializable
    public data class Web(public var colors: Colors = defaultLightColors) : ColorScheme("web_default")

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

    internal fun progressIndicatorColor(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.progressIndicator else this.lightColors.progressIndicator
        is Dark -> this.colors.progressIndicator
        is Light -> this.colors.progressIndicator
        is Web -> this.colors.progressIndicator
    }

    internal fun closeIcon(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.closeIcon else this.lightColors.closeIcon
        is Dark -> this.colors.closeIcon
        is Light -> this.colors.closeIcon
        is Web -> this.colors.closeIcon
    }

    internal fun closeIconTint(isDark: Boolean) = when (this) {
        is Automatic -> if (isDark) this.darkColors.closeIconTint else this.lightColors.closeIconTint
        is Dark -> this.colors.closeIconTint
        is Light -> this.colors.closeIconTint
        is Web -> this.colors.closeIconTint
    }

    /**
     * Creates a customized copy of a ColorScheme, facilitating modification of individual color properties
     *
     * @param block A lambda that configures colors
     * @return A new ColorScheme instance with the customized colors
     */
    public fun customize(block: ColorsBuilder.() -> Unit): ColorScheme {
        return when (this) {
            is Light -> {
                val builder = ColorsBuilder(colors)
                builder.block()
                copy(colors = builder.build())
            }

            is Dark -> {
                val builder = ColorsBuilder(colors)
                builder.block()
                copy(colors = builder.build())
            }

            is Web -> {
                val builder = ColorsBuilder(colors)
                builder.block()
                copy(colors = builder.build())
            }

            is Automatic -> {
                val lightBuilder = ColorsBuilder(lightColors)
                val darkBuilder = ColorsBuilder(darkColors)
                lightBuilder.block()
                darkBuilder.block()
                copy(
                    lightColors = lightBuilder.build(),
                    darkColors = darkBuilder.build()
                )
            }
        }
    }

    /**
     * Creates a customized copy of an Automatic ColorScheme, facilitating modification of individual color properties
     * in both light and dark schemes.
     *
     * @param light A lambda that configures the light mode colors
     * @param dark A lambda that configures the dark mode colors
     * @return A new ColorScheme instance with the customized colors
     */
    public fun customize(
        light: ColorsBuilder.() -> Unit,
        dark: ColorsBuilder.() -> Unit
    ): ColorScheme {
        return when (this) {
            is Automatic -> {
                val lightBuilder = ColorsBuilder(lightColors)
                val darkBuilder = ColorsBuilder(darkColors)
                lightBuilder.light()
                darkBuilder.dark()
                copy(
                    lightColors = lightBuilder.build(),
                    darkColors = darkBuilder.build()
                )
            }

            else -> {
                val colors = when (this) {
                    is Light -> this.colors
                    is Dark -> this.colors
                    is Web -> this.colors
                    else -> return this
                }
                val builder = ColorsBuilder(colors)
                builder.light()
                when (this) {
                    is Light -> copy(colors = builder.build())
                    is Dark -> copy(colors = builder.build())
                    is Web -> copy(colors = builder.build())
                    else -> this
                }
            }
        }
    }
}

/**
 * Specifies the colors for native elements within a ColorScheme.
 *
 * A Colors instance can be passed into a ColorScheme constructor to override its default colors. Colors
 * that can be overridden are:
 * - The WebView background color,
 * - The native header background and font color,
 * - The progress/loading indicator.
 *
 * @see ColorScheme
 */
@Serializable
public data class Colors(
    val webViewBackground: Color,
    val headerBackground: Color,
    val headerFont: Color,
    val progressIndicator: Color,
    val closeIcon: DrawableResource? = null,
    val closeIconTint: Color? = null,
)

/**
 * Builder class for customizing Colors objects in an ergonomic way.
 */
public class ColorsBuilder internal constructor(private val baseColors: Colors) {
    public var webViewBackground: Color? = null
    public var headerBackground: Color? = null
    public var headerFont: Color? = null
    public var progressIndicator: Color? = null
    public var closeIcon: DrawableResource? = null
    public var closeIconTint: Color? = null

    public fun withWebViewBackground(color: Color): ColorsBuilder {
        webViewBackground = color
        return this
    }

    public fun withHeaderBackground(color: Color): ColorsBuilder {
        headerBackground = color
        return this
    }

    public fun withHeaderFont(color: Color): ColorsBuilder {
        headerFont = color
        return this
    }

    public fun withProgressIndicator(color: Color): ColorsBuilder {
        progressIndicator = color
        return this
    }

    public fun withCloseIcon(icon: DrawableResource): ColorsBuilder {
        closeIcon = icon
        return this
    }

    public fun withCloseIconTint(tint: Color): ColorsBuilder {
        closeIconTint = tint
        return this
    }

    internal fun build(): Colors {
        return baseColors.copy(
            webViewBackground = webViewBackground ?: baseColors.webViewBackground,
            headerBackground = headerBackground ?: baseColors.headerBackground,
            headerFont = headerFont ?: baseColors.headerFont,
            progressIndicator = progressIndicator ?: baseColors.progressIndicator,
            closeIcon = closeIcon ?: baseColors.closeIcon,
            closeIconTint = closeIconTint ?: baseColors.closeIconTint
        )
    }
}

/**
 * Represents a color which can be either a resource id or an sRGB value.
 */
@Serializable
public sealed class Color {
    /**
     * Represents a color in sRGB color space.
     * @property sRGB The sRGB value of the color.
     */
    @Serializable
    public data class SRGB(public val sRGB: Int) : Color()

    /**
     * Represents a color as a resource id (e.g. {@code android.R.color.black}).
     * @property id The resource id of the color.
     */
    @Serializable
    public data class ResourceId(@ColorRes public val id: Int) : Color()

    /**
     * Returns the color value.
     * @param context The context to use when retrieving the color.
     * @return The color value.
     */
    @ColorInt
    public fun getValue(context: Context): Int = when (this) {
        is ResourceId -> getColor(context, this.id)
        is SRGB -> this.sRGB
    }
}

/**
 * Represents a drawable resource.
 */
@Serializable
public data class DrawableResource(@DrawableRes public val id: Int)

private val defaultLightColors = Colors(
    webViewBackground = Color.ResourceId(R.color.checkoutLightBg),
    headerBackground = Color.ResourceId(R.color.checkoutLightBg),
    headerFont = Color.ResourceId(R.color.checkoutLightFont),
    progressIndicator = Color.ResourceId(R.color.checkoutLightProgressIndicator),
)

private val defaultDarkColors = Colors(
    webViewBackground = Color.ResourceId(R.color.checkoutDarkBg),
    headerBackground = Color.ResourceId(R.color.checkoutDarkBg),
    headerFont = Color.ResourceId(R.color.checkoutDarkFont),
    progressIndicator = Color.ResourceId(R.color.checkoutDarkProgressIndicator),
)

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

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ColorSchemeTest {

    @Test
    fun `dark color scheme has defaults matching idiomatic dark checkout`() {
        val dark = ColorScheme.Dark()

        assertThat(dark.colors.headerBackground).isEqualTo(Color.ResourceId(R.color.checkoutDarkBg))
        assertThat(dark.colors.headerFont).isEqualTo(Color.ResourceId(R.color.checkoutDarkFont))
        assertThat(dark.colors.webViewBackground).isEqualTo(Color.ResourceId(R.color.checkoutDarkBg))
        assertThat(dark.colors.spinnerColor).isEqualTo(Color.ResourceId(R.color.checkoutDarkLoadingSpinner))
    }

    @Test
    fun `light color scheme has defaults matching idiomatic light checkout`() {
        val light = ColorScheme.Light()

        assertThat(light.colors.headerBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(light.colors.headerFont).isEqualTo(Color.ResourceId(R.color.checkoutLightFont))
        assertThat(light.colors.webViewBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(light.colors.spinnerColor).isEqualTo(Color.ResourceId(R.color.checkoutLightLoadingSpinner))
    }

    @Test
    fun `automatic color scheme has defaults matching both idiomatic light and dark checkout`() {
        val automatic = ColorScheme.Automatic()

        assertThat(automatic.darkColors.headerBackground).isEqualTo(Color.ResourceId(R.color.checkoutDarkBg))
        assertThat(automatic.darkColors.headerFont).isEqualTo(Color.ResourceId(R.color.checkoutDarkFont))
        assertThat(automatic.darkColors.webViewBackground).isEqualTo(Color.ResourceId(R.color.checkoutDarkBg))
        assertThat(automatic.darkColors.spinnerColor).isEqualTo(Color.ResourceId(R.color.checkoutDarkLoadingSpinner))

        assertThat(automatic.lightColors.headerBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(automatic.lightColors.headerFont).isEqualTo(Color.ResourceId(R.color.checkoutLightFont))
        assertThat(automatic.lightColors.webViewBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(automatic.lightColors.spinnerColor).isEqualTo(Color.ResourceId(R.color.checkoutLightLoadingSpinner))
    }

    @Test
    fun `web color scheme has defaults matching idiomatic light checkout`() {
        val web = ColorScheme.Web()

        assertThat(web.colors.headerBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(web.colors.headerFont).isEqualTo(Color.ResourceId(R.color.checkoutLightFont))
        assertThat(web.colors.webViewBackground).isEqualTo(Color.ResourceId(R.color.checkoutLightBg))
        assertThat(web.colors.spinnerColor).isEqualTo(Color.ResourceId(R.color.checkoutLightLoadingSpinner))
    }

    @Test
    fun `color schemes defaults can be overridden`() {
        val web = ColorScheme.Web(
            Colors(
                headerBackground = Color.ResourceId(1),
                headerFont = Color.ResourceId(2),
                webViewBackground = Color.ResourceId(3),
                spinnerColor = Color.ResourceId(4),
            )
        )

        assertThat(web.colors.headerBackground).isEqualTo(Color.ResourceId(1))
        assertThat(web.colors.headerFont).isEqualTo(Color.ResourceId(2))
        assertThat(web.colors.webViewBackground).isEqualTo(Color.ResourceId(3))
        assertThat(web.colors.spinnerColor).isEqualTo(Color.ResourceId(4))
    }

    @Test
    fun `color schemes has a helper function for retrieving header background color`() {
        val automatic = ColorScheme.Automatic(
            lightColors = Colors(
                headerBackground = Color.ResourceId(1),
                headerFont = Color.ResourceId(2),
                webViewBackground = Color.ResourceId(3),
                spinnerColor = Color.ResourceId(4),
            ),
            darkColors = Colors(
                headerBackground = Color.ResourceId(5),
                headerFont = Color.ResourceId(6),
                webViewBackground = Color.ResourceId(7),
                spinnerColor = Color.ResourceId(8),
            )
        )

        assertThat(
            automatic.headerBackgroundColor(isDark = false)
        ).isEqualTo(Color.ResourceId(1))

        assertThat(
            automatic.headerBackgroundColor(isDark = true)
        ).isEqualTo(Color.ResourceId(5))
    }

    @Test
    fun `color schemes has a helper function for retrieving header font color`() {
        val automatic = ColorScheme.Automatic(
            lightColors = Colors(
                headerBackground = Color.ResourceId(1),
                headerFont = Color.ResourceId(2),
                webViewBackground = Color.ResourceId(3),
                spinnerColor = Color.ResourceId(4),
            ),
            darkColors = Colors(
                headerBackground = Color.ResourceId(5),
                headerFont = Color.ResourceId(6),
                webViewBackground = Color.ResourceId(7),
                spinnerColor = Color.ResourceId(8),
            )
        )

        assertThat(
            automatic.headerFontColor(isDark = false)
        ).isEqualTo(Color.ResourceId(2))

        assertThat(
            automatic.headerFontColor(isDark = true)
        ).isEqualTo(Color.ResourceId(6))
    }

    @Test
    fun `color schemes has a helper function for retrieving webview background color`() {
        val automatic = ColorScheme.Automatic(
            lightColors = Colors(
                headerBackground = Color.ResourceId(1),
                headerFont = Color.ResourceId(2),
                webViewBackground = Color.ResourceId(3),
                spinnerColor = Color.ResourceId(4),
            ),
            darkColors = Colors(
                headerBackground = Color.ResourceId(5),
                headerFont = Color.ResourceId(6),
                webViewBackground = Color.ResourceId(7),
                spinnerColor = Color.ResourceId(8),
            )
        )

        assertThat(
            automatic.webViewBackgroundColor(isDark = false)
        ).isEqualTo(Color.ResourceId(3))

        assertThat(
            automatic.webViewBackgroundColor(isDark = true)
        ).isEqualTo(Color.ResourceId(7))
    }

    @Test
    fun `color scheme can be serialized`() {
        val automatic = ColorScheme.Automatic(
            lightColors = Colors(
                headerBackground = Color.ResourceId(1),
                headerFont = Color.ResourceId(2),
                webViewBackground = Color.ResourceId(3),
                spinnerColor = Color.ResourceId(4),
            ),
            darkColors = Colors(
                headerBackground = Color.ResourceId(5),
                headerFont = Color.ResourceId(6),
                webViewBackground = Color.ResourceId(8),
                spinnerColor = Color.ResourceId(9),
            )
        )

        val serialized = Json.encodeToString(ColorScheme.serializer(), automatic)

        assertThat(
            Json.decodeFromString(ColorScheme.serializer(), serialized)
        )
            .usingRecursiveComparison()
            .isEqualTo(automatic)
    }
}

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

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

private const val LIGHT_LUMINANCE_THRESHOLD = 0.5

/**
 * Makes the sheet's window fill the screen and draw behind the system bars and display cutout.
 *
 * The [header] is padded by the top insets, so its background shows behind the status bar; the
 * [container] is padded by the bottom insets, so the WebView background shows behind the
 * navigation bar. A window that does not fit system windows is not resized for the keyboard
 * either, so the keyboard is handled as an inset too: the container is padded by whichever of the
 * navigation bar and the keyboard is taller. Insets are added to the paddings the views were
 * inflated with, so the header keeps its own spacing.
 */
@Suppress("DEPRECATION")
internal fun Window.drawEdgeToEdge(
    header: View,
    container: View,
    @ColorInt headerBackground: Int,
    @ColorInt webViewBackground: Int,
) {
    setLayout(MATCH_PARENT, MATCH_PARENT)
    WindowCompat.setDecorFitsSystemWindows(this, false)
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = Color.TRANSPARENT
    navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        attributes = attributes.apply {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // A dialog window is otherwise placed between the bars, whatever its decor fits.
        attributes = attributes.apply { fitInsetsTypes = 0 }
    }
    WindowCompat.getInsetsController(this, decorView).apply {
        isAppearanceLightStatusBars = headerBackground.isLight()
        isAppearanceLightNavigationBars = webViewBackground.isLight()
    }

    val headerPadding = header.padding()
    val containerPadding = container.padding()
    val content = decorView.findViewById<View>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(content) { _, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        header.setPadding(
            headerPadding.left + bars.left,
            headerPadding.top + bars.top,
            headerPadding.right + bars.right,
            headerPadding.bottom,
        )
        container.setPadding(
            containerPadding.left + bars.left,
            containerPadding.top,
            containerPadding.right + bars.right,
            containerPadding.bottom + maxOf(bars.bottom, ime.bottom),
        )
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(content)
}

private fun View.padding() = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)

private fun @receiver:ColorInt Int.isLight() = ColorUtils.calculateLuminance(this) > LIGHT_LUMINANCE_THRESHOLD

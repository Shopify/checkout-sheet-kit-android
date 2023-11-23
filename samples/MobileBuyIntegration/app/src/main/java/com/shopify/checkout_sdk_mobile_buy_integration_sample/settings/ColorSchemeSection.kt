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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings

import android.support.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopify.checkoutkit.ColorScheme
import com.shopify.checkoutkit.Colors
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkoutkit.Color

@Composable
fun ColorSchemeSection(
    selected: ColorScheme,
    setSelected: (ColorScheme) -> Unit,
) {

    val horizontalPadding = 20.dp

    Text(
        "Color Scheme".uppercase(),
        color = MaterialTheme.colors.primaryVariant,
        fontSize = 12.sp,
        modifier = Modifier.padding(
            top = 18.dp,
            bottom = 8.dp,
            start = horizontalPadding,
            end = horizontalPadding
        ),
    )

    Column(modifier = Modifier.selectableGroup()) {

        val optionModifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = horizontalPadding)

        ColorSchemeOption(
            colorScheme = ColorScheme.Automatic(),
            description = "Applies a color scheme in checkout based on device preferences",
            selected = selected,
            setSelected = setSelected,
            modifier = optionModifier,
        )

        Divider(thickness = Dp.Hairline)

        ColorSchemeOption(
            colorScheme = ColorScheme.Light(),
            description = "Applies a light color scheme to checkout",
            selected = selected,
            setSelected = setSelected,
            modifier = optionModifier
        )

        Divider(thickness = Dp.Hairline)

        ColorSchemeOption(
            colorScheme = ColorScheme.Dark(),
            selected = selected,
            description = "Applies a dark color scheme to checkout",
            setSelected = setSelected,
            modifier = optionModifier,
        )

        Divider(thickness = Dp.Hairline)

        ColorSchemeOption(
            colorScheme = ColorScheme.Web(
                colors = Colors(
                    headerBackground = Color.ResourceId(R.color.header_bg),
                    webViewBackground = Color.ResourceId(R.color.web_view_bg),
                    headerFont = Color.ResourceId(R.color.header_font),
                    spinnerColor = Color.ResourceId(R.color.bright_spinner),
                )
            ),
            description = "Applies a color scheme in checkout based on the current checkout web configuration",
            selected = selected,
            setSelected = setSelected,
            modifier = optionModifier,
        )
    }

    Column(Modifier.padding(vertical = 8.dp, horizontal = horizontalPadding)) {
        Text(
            "NOTE: If preloading is enabled, color scheme changes may not be applied unless the cart is preloaded again.",
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun ColorSchemeOption(
    colorScheme: ColorScheme,
    setSelected: (ColorScheme) -> Unit,
    description: String,
    selected: ColorScheme,
    modifier: Modifier,
) {
    val isSelected = selected.id == colorScheme.id

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.selectable(
            selected = isSelected,
            role = Role.RadioButton,
            onClick = { setSelected(colorScheme) }
        ),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            modifier = Modifier.semantics { contentDescription = description }
        )
        Text(
            stringResource(id = colorScheme.name).capitalize(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

private val ColorScheme.name: Int @StringRes get() = when(this) {
    is ColorScheme.Light -> R.string.color_scheme_light
    is ColorScheme.Dark -> R.string.color_scheme_dark
    is ColorScheme.Web -> R.string.color_scheme_web
    is ColorScheme.Automatic -> R.string.color_scheme_automatic
}

// kotlin stdlib capitalize is deprecated
private fun String.capitalize() =
    this.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(
            java.util.Locale(
                Locale.current.toLanguageTag()
            )
        ) else char.toString()
    }

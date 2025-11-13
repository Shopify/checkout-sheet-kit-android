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
package com.shopify.checkout_kit_mobile_buy_integration_sample.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shopify.checkout_kit_mobile_buy_integration_sample.R
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkout_kit_mobile_buy_integration_sample.common.ui.theme.verticalPadding
import com.shopify.checkoutsheetkit.Color
import com.shopify.checkoutsheetkit.ColorScheme
import com.shopify.checkoutsheetkit.Colors

@Composable
fun ColorSchemeSection(
    selected: ColorScheme,
    setSelected: (ColorScheme) -> Unit,
) {

    Column {
        Header3(text = stringResource(id = R.string.color_scheme))

        Column(
            modifier = Modifier
                .selectableGroup()
                .padding(vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            val optionModifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxWidth()

            ColorSchemeOption(
                colorScheme = ColorScheme.Automatic(),
                description = "Applies a color scheme in checkout based on device preferences",
                selected = selected,
                setSelected = setSelected,
                modifier = optionModifier,
            )

            ColorSchemeOption(
                colorScheme = ColorScheme.Light(),
                description = "Applies a light color scheme to checkout",
                selected = selected,
                setSelected = setSelected,
                modifier = optionModifier
            )

            ColorSchemeOption(
                colorScheme = ColorScheme.Dark(),
                selected = selected,
                description = "Applies a dark color scheme to checkout",
                setSelected = setSelected,
                modifier = optionModifier,
            )

            ColorSchemeOption(
                colorScheme = ColorScheme.Web(
                    colors = Colors(
                        headerBackground = Color.ResourceId(R.color.header_bg),
                        webViewBackground = Color.ResourceId(R.color.web_view_bg),
                        headerFont = Color.ResourceId(R.color.header_font),
                        progressIndicator = Color.ResourceId(R.color.bright_progress_indicator),
                    )
                ),
                description = "Applies a color scheme in checkout based on the current checkout web configuration",
                selected = selected,
                setSelected = setSelected,
                modifier = optionModifier,
            )
        }

        Column(Modifier.padding(vertical = 8.dp)) {
            BodySmall(
                stringResource(R.string.preloading_note),
            )
        }
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
        BodyMedium(
            stringResource(id = colorScheme.name),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

private val ColorScheme.name: Int
    get() = when (this) {
        is ColorScheme.Light -> R.string.color_scheme_light
        is ColorScheme.Dark -> R.string.color_scheme_dark
        is ColorScheme.Web -> R.string.color_scheme_web
        is ColorScheme.Automatic -> R.string.color_scheme_automatic
    }

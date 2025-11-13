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
package com.shopify.checkout_kit_mobile_buy_integration_sample.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun Header1(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = MaterialTheme.typography.titleLarge.fontSize,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
    )
}

@Composable
fun Header2(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = MaterialTheme.typography.titleMedium.fontSize,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
    )
}

@Composable
fun Header3(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = MaterialTheme.typography.titleSmall.fontSize,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
    )
}

@Composable
fun Header(
    text: String?,
    resourceId: Int?,
    modifier: Modifier = Modifier,
    color:
    Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle,
    textAlign: TextAlign,
    fontSize: TextUnit
) {
    if (text == null && resourceId == null) {
        throw IllegalArgumentException("No text or resourceId passed to Header component")
    }
    Text(
        text = text ?: stringResource(id = resourceId!!),
        style = style,
        modifier = modifier,
        color = color,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontSize = fontSize,
    )
}

@Composable
fun BodyMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textDecoration: TextDecoration = TextDecoration.None,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
        color = color,
        textDecoration = textDecoration,
    )
}

@Composable
fun BodySmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textDecoration: TextDecoration = TextDecoration.None,
    fontSize: TextUnit = MaterialTheme.typography.bodySmall.fontSize,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        color = color,
        textDecoration = textDecoration,
        fontSize = fontSize,
    )
}

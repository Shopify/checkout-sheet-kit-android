package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.toDisplayText

@Composable
fun Header1(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun Header2(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun Header3(
    modifier: Modifier = Modifier,
    text: String? = null,
    resourceId: Int? = null,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
) {
    Header(
        text = text,
        resourceId = resourceId,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
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
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        color = color,
        textDecoration = textDecoration,
    )
}

@Composable
fun MoneyAmount(
    currency: String,
    price: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    includeSuffix: Boolean = true,
) {
    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocalConfiguration.current.locales.get(0)
    } else {
        LocalConfiguration.current.locale
    }

    Text(
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        text = toDisplayText(
            currency,
            price,
            locale,
            includeSuffix,
        )
    )
}
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header3
import com.shopify.checkoutsheetkit.PrivacyConsent

@Composable
fun PrivacyConsentSection(
    privacyConsent: PrivacyConsent,
    onMarketingChange: (Boolean) -> Unit,
    onAnalyticsChange: (Boolean) -> Unit,
    onPreferencesChange: (Boolean) -> Unit,
    onSaleOfDataChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Header3(text = stringResource(id = R.string.privacy_consent_signals))

        PrivacyConsentSwitch(
            label = stringResource(id = R.string.marketing),
            checked = privacyConsent.marketing,
            onCheckedChange = onMarketingChange,
        )

        PrivacyConsentSwitch(
            label = stringResource(id = R.string.analytics),
            checked = privacyConsent.analytics,
            onCheckedChange = onAnalyticsChange,
        )

        PrivacyConsentSwitch(
            label = stringResource(id = R.string.preferences),
            checked = privacyConsent.preferences,
            onCheckedChange = onPreferencesChange,
        )

        PrivacyConsentSwitch(
            label = stringResource(id = R.string.sale_of_data),
            checked = privacyConsent.saleOfData,
            onCheckedChange = onSaleOfDataChange,
        )
    }
}

@Composable
private fun PrivacyConsentSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        BodyMedium(label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

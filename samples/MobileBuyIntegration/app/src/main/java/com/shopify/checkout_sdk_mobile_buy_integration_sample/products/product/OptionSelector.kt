package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodyMedium
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantOptionDetails

@Composable
fun OptionSelector(
    availableOptions: Map<String, List<ProductVariantOptionDetails>>,
    selectedOptions: Map<String, String>,
    onSelected: (name: String, value: String) -> Unit,
) {
    availableOptions.forEach { (optionName, optionValues) ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            BodySmall(optionName)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                optionValues.forEach { optionDetails ->
                    val isSelected = selectedOptions[optionName] == optionDetails.name

                    OutlinedButton(
                        onClick = { onSelected(optionName, optionDetails.name) },
                        enabled = optionDetails.availableForSale,
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                        } else {
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        }
                    ) {
                        BodyMedium(
                            optionDetails.name,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                            textDecoration = if (optionDetails.availableForSale) TextDecoration.None else TextDecoration.LineThrough,
                        )
                    }
                }
            }
        }
    }
}

package com.shopify.checkout_sdk_mobile_buy_integration_sample.home

import HeroImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.Header1
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ui.theme.cooperBTFontFamily
import timber.log.Timber

@Composable
fun Hero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            HeroImage()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Header1(
                    resourceId = R.string.hero_text,
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = {
                        Timber.i("Clicked shop all")
                    },
                    modifier = Modifier
                        .border(width = 1.dp, color = White)
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    Text(
                        stringResource(id = R.string.hero_cta),
                        color = White,
                        fontFamily = cooperBTFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

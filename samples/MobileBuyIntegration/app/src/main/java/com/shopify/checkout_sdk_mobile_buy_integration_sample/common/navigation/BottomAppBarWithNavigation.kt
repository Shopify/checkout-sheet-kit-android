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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.components.BodySmall

@Composable
fun BottomAppBarWithNavigation(
    navController: NavHostController,
    currentScreen: Screen,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationItem(
                navController,
                Screen.Home,
                ImageVector.vectorResource(R.drawable.home),
                stringResource(id = R.string.navigation_home),
                currentScreen,
            )
            NavigationItem(
                navController,
                Screen.Products,
                ImageVector.vectorResource(R.drawable.product),
                stringResource(id = R.string.navigation_shop),
                currentScreen,
            )
            NavigationItem(
                navController,
                Screen.Settings,
                ImageVector.vectorResource(R.drawable.profile),
                stringResource(id = R.string.navigation_log_in),
                currentScreen,
            )
        }
    }
}

@Composable
fun NavigationItem(
    navController: NavHostController,
    screen: Screen,
    icon: ImageVector,
    label: String,
    currentScreen: Screen,
) {
    val isActiveScreen = currentScreen == screen
    val color = if (isActiveScreen) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Unspecified
    }

    Column {
        IconButton(
            onClick = { navController.navigate(screen.route) },
            modifier = Modifier.semantics {
                this.contentDescription = "$label icon"
            }
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
        }

        BodySmall(
            text = label,
            color = color,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset(0.dp, (-7.5).dp)
        )
    }
}

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Badge
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.totalQuantity

@Composable
fun BottomAppBarWithNavigation(
    navController: NavHostController,
    currentScreen: Screen,
    cartViewModel: CartViewModel,
) {
    BottomAppBar(
        backgroundColor = MaterialTheme.colors.background,
        cutoutShape = CircleShape,
    ) {

        val state = cartViewModel.cartState.collectAsState()
        val totalQuantity = state.value.totalQuantity

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationItem(
                navController,
                Screen.Product,
                ImageVector.vectorResource(R.drawable.product),
                "Product",
                currentScreen,
            )
            NavigationItem(
                navController,
                Screen.Cart,
                ImageVector.vectorResource(R.drawable.cart),
                "Cart",
                currentScreen,
            ) {
                if (totalQuantity > 0) {
                    Badge(modifier = Modifier.offset(x = 12.5.dp, y = -(12.5.dp))) {
                        Text("$totalQuantity")
                    }
                }
            }
            NavigationItem(
                navController,
                Screen.Settings,
                ImageVector.vectorResource(R.drawable.settings),
                "Settings",
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
    badge: @Composable () -> Unit = {}
) {
    IconButton(
        onClick = { navController.navigate(screen.route) },
        modifier = Modifier.semantics {
            this.contentDescription = "$label icon"
        }
    ) {
        val tint = if (currentScreen == screen) {
            MaterialTheme.colors.primary
        } else {
            Color.Unspecified
        }

        Icon(imageVector = icon, contentDescription = label, tint = tint)
        badge()
    }
}

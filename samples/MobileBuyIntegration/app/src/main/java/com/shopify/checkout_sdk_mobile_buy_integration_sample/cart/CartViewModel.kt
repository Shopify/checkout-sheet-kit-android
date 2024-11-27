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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

typealias OnComplete = (CartState.Cart?) -> Unit

class CartViewModel(
    private val cartRepository: CartRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _cartState = MutableStateFlow<CartState>(CartState.Empty)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private var demoBuyerIdentityEnabled = false

    init {
        // clear cart when buyer identity demo setting toggled
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect {
                if (demoBuyerIdentityEnabled != it.buyerIdentityDemoEnabled) {
                    clearCart()
                    demoBuyerIdentityEnabled = it.buyerIdentityDemoEnabled
                }
            }
        }
    }

    fun addToCart(variantId: String, quantity: Int, onComplete: OnComplete) {
        Timber.i("Adding variant: $variantId to cart with quantity: $quantity")
        when (val state = _cartState.value) {
            is CartState.Empty -> performCartCreate(variantId, quantity, onComplete)
            is CartState.Cart -> performCartLinesAdd(state.cartID, variantId, quantity, onComplete)
        }
    }

    fun modifyLineItem(lineItemId: String, quantity: Int?) {
        when (val state = _cartState.value) {
            is CartState.Cart -> {
                viewModelScope.launch {
                    Timber.i("Updating or removing line item: $lineItemId, quantity: $quantity")
                    _loadingState.value = true
                    cartRepository.modifyCartLine(state.cartID, lineItemId, quantity)
                        .catch { exception ->
                            Timber.e("Error updating cart $exception")
                            _loadingState.value = false
                        }
                        .collect { cart ->
                            Timber.i("Cart modification complete")
                            Timber.i("Invalidating previous preloads, so checkout reflects modified cart state")
                            ShopifyCheckoutSheetKit.invalidate()
                            _cartState.value = if (cart.cartTotals.totalQuantity == 0) CartState.Empty else cart
                            _loadingState.value = false
                        }
                }
            }

            is CartState.Empty -> Timber.e("attempting to update the quantity on an empty cart")
        }
    }

    fun clearCart() {
        _cartState.value = CartState.Empty
    }

    fun <T : DefaultCheckoutEventProcessor> presentCheckout(
        url: String,
        activity: ComponentActivity,
        eventProcessor: T
    ) {
        Timber.i("Presenting checkout with $url")
        ShopifyCheckoutSheetKit.present(url, activity, eventProcessor)
    }

    fun preloadCheckout(
        activity: ComponentActivity,
    ) {
        val state = _cartState.value
        if (state is CartState.Cart) {
            Timber.i("Preloading checkout with url ${state.checkoutUrl}")
            ShopifyCheckoutSheetKit.preload(state.checkoutUrl, activity)
        } else {
            Timber.i("Skipping checkout preload, cart is empty")
        }
    }

    fun continueShopping(navController: NavController) {
        Timber.i("Continue shopping clicked, navigating to products")
        navController.navigate(Screen.Products.route)
    }

    private fun performCartLinesAdd(cartId: String, variantId: String, quantity: Int, onComplete: OnComplete) {
        viewModelScope.launch {
            Timber.i("Adding cart lines to existing cart: $cartId, variant: $variantId, and $quantity")
            cartRepository.addCartLine(cartId, variantId, quantity)
                .catch { e -> Timber.e("Couldn't add cart line $e") }
                .collect { cart ->
                    _cartState.value = cart
                    onComplete(cart)
                }
        }
    }

    private fun performCartCreate(variantId: String, quantity: Int, onComplete: OnComplete) {
        viewModelScope.launch {
            Timber.i("No existing cart, creating new")
            cartRepository.createCart(
                variantId = variantId,
                demoBuyerIdentityEnabled = demoBuyerIdentityEnabled,
                quantity = quantity,
            )
                .catch { e -> Timber.e("Couldn't create cart $e") }
                .collect { cart ->
                    Timber.i("Cart created")
                    _cartState.value = cart
                    onComplete.invoke(cart)
                }
        }
    }
}

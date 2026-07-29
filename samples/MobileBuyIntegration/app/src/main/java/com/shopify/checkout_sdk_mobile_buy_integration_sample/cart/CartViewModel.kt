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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.ID
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.CheckoutExpiredException
import com.shopify.checkoutsheetkit.CheckoutSheetKitException
import com.shopify.checkoutsheetkit.ConfigurationException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.HttpException
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

typealias OnComplete = (Result<CartState.Cart>) -> Unit

class CartViewModel(
    private val cartRepository: CartRepository,
    private val preferencesManager: PreferencesManager,
    private val customerRepository: CustomerRepository,
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

    fun addToCart(variantId: ID, quantity: Int, onComplete: OnComplete) {
        Timber.i("Adding variant: $variantId to cart with quantity: $quantity")
        when (val state = _cartState.value) {
            is CartState.Empty -> performCartCreate(variantId, quantity, onComplete)
            is CartState.Cart -> performCartLinesAdd(state.cartID, variantId, quantity, onComplete)
        }
    }

    fun modifyLineItem(lineItemId: ID, quantity: Int?) = viewModelScope.launch {
        when (val state = _cartState.value) {
            is CartState.Cart -> {
                Timber.i("Updating or removing line item: $lineItemId, quantity: $quantity")
                _loadingState.value = true
                try {
                    val cart = cartRepository.modifyCartLine(state.cartID, lineItemId, quantity)
                    Timber.i("Cart modification complete")
                    Timber.i("Invalidating previous preloads, so checkout reflects modified cart state")
                    ShopifyCheckoutSheetKit.invalidate()
                    _cartState.value = if (cart.cartTotals.totalQuantity == 0) CartState.Empty else cart
                    _loadingState.value = false
                } catch (e: Exception) {
                    Timber.e("Error updating cart $e")
                    SnackbarController.sendEvent(SnackbarEvent(R.string.cart_error_updating))
                    _loadingState.value = false
                }
            }

            is CartState.Empty -> Timber.e("attempting to update the quantity on an empty cart")
        }
    }

    fun clearCart() {
        _cartState.value = CartState.Empty
    }

    internal fun handleCheckoutFailed(error: CheckoutException) {
        // The Sheet Kit hierarchy carries recovery semantics in its concrete exception type.
        // Only an expired checkout URL makes this app-owned cart unusable; all other failures
        // leave it intact for host-owned retry or fallback behavior.
        if (error is CheckoutExpiredException) {
            clearCart()
        }

        viewModelScope.launch {
            SnackbarController.sendEvent(SnackbarEvent(error.userMessageResourceId()))
        }
    }

    private fun CheckoutException.userMessageResourceId(): Int = when (this) {
        is CheckoutExpiredException -> R.string.checkout_error_cart_unavailable
        is ConfigurationException -> when (errorCode) {
            ConfigurationException.STOREFRONT_PASSWORD_REQUIRED ->
                R.string.checkout_error_storefront_password_required
            else -> R.string.checkout_error
        }
        is HttpException -> if (statusCode.isRetryableCheckoutHttpStatus()) {
            R.string.checkout_error_retry
        } else {
            R.string.checkout_error
        }
        is CheckoutSheetKitException -> when (errorCode) {
            CheckoutSheetKitException.RENDER_PROCESS_GONE -> R.string.checkout_error_retry
            else -> R.string.checkout_error
        }
        else -> R.string.checkout_error
    }

    private fun Int.isRetryableCheckoutHttpStatus(): Boolean =
        this == HTTP_STATUS_REQUEST_TIMEOUT ||
            this == HTTP_STATUS_TOO_MANY_REQUESTS ||
            this in HTTP_STATUS_SERVER_ERROR_RANGE

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

    private fun performCartLinesAdd(cartId: ID, variantId: ID, quantity: Int, onComplete: OnComplete) = viewModelScope.launch {
        Timber.i("Adding cart lines to existing cart: $cartId, variant: $variantId, and $quantity")
        try {
            val cart = cartRepository.addCartLine(cartId, variantId, quantity)
            _cartState.value = cart
            onComplete(Result.success(cart))
        } catch (e: Exception) {
            Timber.e("Couldn't add cart line $e")
            SnackbarController.sendEvent(SnackbarEvent(R.string.cart_error_updating))
            onComplete(Result.failure(e))
        }
    }

    private fun performCartCreate(variantId: ID, quantity: Int, onComplete: OnComplete) = viewModelScope.launch {
        Timber.i("No existing cart, creating a new one")
        val customerAccessToken = customerRepository.getCustomerAccessToken()?.accessToken
        try {
            val cart = cartRepository.createCart(
                variantId,
                quantity,
                demoBuyerIdentityEnabled,
                customerAccessToken,
            )

            Timber.i("Cart created $cart")
            _cartState.value = cart
            onComplete(Result.success(cart))
        } catch (e: Exception) {
            Timber.e("Couldn't create cart $e")
            SnackbarController.sendEvent(SnackbarEvent(R.string.cart_error_creating))
            onComplete(Result.failure(e))
        }
    }

    private companion object {
        const val HTTP_STATUS_REQUEST_TIMEOUT = 408
        const val HTTP_STATUS_TOO_MANY_REQUESTS = 429
        val HTTP_STATUS_SERVER_ERROR_RANGE = 500..599
    }
}

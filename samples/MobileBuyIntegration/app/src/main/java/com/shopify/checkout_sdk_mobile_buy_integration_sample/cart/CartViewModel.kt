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
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.Cart
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.source.network.CartStorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarController
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.SnackbarEvent
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.navigation.Screen
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

typealias OnComplete = (Cart?) -> Unit

class CartViewModel(
    private val client: CartStorefrontApiClient,
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

    fun addToCart(variant: ID, quantity: Int, onComplete: OnComplete) = viewModelScope.launch {
        Timber.i("Adding variant: $variant to cart with quantity: $quantity")
        when (val state = _cartState.value) {
            is CartState.Empty -> performCartCreate(variant, quantity, onComplete)
            is CartState.Populated -> performCartLinesAdd(state.cartID, variant, quantity, onComplete)
        }
    }

    fun modifyLineItem(lineItemID: ID, quantity: Int?) {
        Timber.i("Updating or removing line item: $lineItemID, quantity: $quantity")
        when (val state = _cartState.value) {
            is CartState.Populated -> {
                _loadingState.value = true
                client.cartLinesModify(state.cartID, lineItemID, quantity, {
                    Timber.i("Cart modification complete")
                    Timber.i("Invalidating previous preloads, so checkout reflects modified cart state")
                    ShopifyCheckoutSheetKit.invalidate()
                    if (quantity != null) _cartState.value = it.data?.cartLinesUpdate?.cart.toUiState()
                    else _cartState.value = it.data?.cartLinesRemove?.cart.toUiState()

                    _loadingState.value = false
                }, { error ->
                    _loadingState.value = false
                    Timber.e("Error updating cart $error")
                })
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
        if (state is CartState.Populated) {
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

    private fun showSnackbar(resourceId: Int) = viewModelScope.launch {
        SnackbarController.sendEvent(SnackbarEvent(resourceId))
    }

    private fun performCartLinesAdd(cartID: ID, variant: ID, quantity: Int, onComplete: OnComplete) {
        Timber.i("Adding cart lines to existing cart: $cartID, variant: $variant, and $quantity")
        val line = CartLineInput(ID("12123")).setQuantity(quantity)

        val onFail = {
            showSnackbar(R.string.cart_error_updating)
            onComplete(null)
        }

        client.cartLinesAdd(
            lines = listOf(line),
            cartId = cartID,
            successCallback = {
                if (it.data != null) {
                    Timber.i("Adding cart lines complete")
                    _cartState.value = it.data?.cartLinesAdd?.cart.toUiState()
                    onComplete(it.data?.cartLinesAdd?.cart)
                } else {
                    onFail()
                }
            },
            failureCallback = { onFail() }
        )
    }

    private fun performCartCreate(variant: ID, quantity: Int, onComplete: OnComplete) {
        Timber.i("No existing cart, creating new")
        val buyerIdentity = if (demoBuyerIdentityEnabled) {
            DemoBuyerIdentity.value
        } else {
            Storefront.CartBuyerIdentityInput().setCountryCode(Storefront.CountryCode.CA)
        }

        val onFail = {
            showSnackbar(R.string.cart_error_creating)
            onComplete(null)
        }

        client.createCart(
            variant = Storefront.ProductVariant(variant),
            buyerIdentity = buyerIdentity,
            quantity = quantity,
            successCallback = { response ->
                if (response.data != null) {
                    Timber.i("Cart created")
                    val cart = response.data?.cartCreate?.cart
                    _cartState.value = cart.toUiState()
                    onComplete(cart)
                } else {
                    onFail()
                }
            },
            failureCallback = { onFail() }
        )
    }

    private fun Cart?.toUiState(): CartState {
        if (this == null) {
            return CartState.Empty
        }
        val cartLines = lines.nodes.mapNotNull { cartLine ->
            val cartLineId = cartLine.id
            (cartLine.merchandise as? Storefront.ProductVariant)?.let {
                CartLine(
                    id = cartLineId,
                    imageURL = it.product.featuredImage.url,
                    imageAltText = it.product.featuredImage.altText ?: "",
                    title = it.product.title,
                    vendor = it.product.vendor,
                    quantity = cartLine.quantity,
                    pricePerQuantity = cartLine.cost.amountPerQuantity.amount.toDouble(),
                    currencyPerQuantity = cartLine.cost.amountPerQuantity.currencyCode.name,
                    totalPrice = cartLine.cost.totalAmount.amount.toDouble(),
                    totalCurrency = cartLine.cost.totalAmount.currencyCode.name,
                )
            }
        }

        if (cartLines.isEmpty()) {
            return CartState.Empty
        }

        return CartState.Populated(
            cartID = id,
            cartLines = cartLines,
            cartTotals = CartTotals(
                totalAmount = Amount(
                    currency = cost.totalAmount.currencyCode.name,
                    price = cost.totalAmount.amount.toDouble(),
                ),
                totalAmountEstimated = cost.totalAmountEstimated,
                totalQuantity = totalQuantity
            ),
            checkoutUrl = checkoutUrl,
        )
    }
}

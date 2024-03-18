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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.Cart
import com.shopify.buy3.Storefront.CartLineInput
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.pay.PaymentUtils
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkoutsheetkit.CheckoutEventProcessor
import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.wallet.contract.TaskResultContracts.GetPaymentDataResult
import androidx.activity.result.registerForActivityResult
import com.google.android.gms.common.api.CommonStatusCodes

typealias OnComplete = (Cart?) -> Unit

class CartViewModel(
    private val client: StorefrontClient,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {
    private val _cartState = MutableStateFlow<CartState>(CartState.Empty)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private var demoBuyerIdentityEnabled = false
    var paymentsClient: PaymentsClient? = null
    private val log: LogWrapper = LogWrapper()

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

    fun addToCart(variant: ID, onComplete: OnComplete) {
        when (val state = _cartState.value) {
            is CartState.Empty -> performCartCreate(variant, onComplete)
            is CartState.Populated -> performCartLinesAdd(state.cartID, variant, onComplete)
        }
    }

    fun clearCart() {
        _cartState.value = CartState.Empty
    }

    fun presentCheckout(
        url: String,
        activity: ComponentActivity,
        eventProcessor: CheckoutEventProcessor
    ) {
        ShopifyCheckoutSheetKit.present(url, activity, eventProcessor)
    }

    private fun performCartLinesAdd(cartID: ID, variant: ID, onComplete: OnComplete) {
        val line = CartLineInput(variant).setQuantity(1)
        client.cartLinesAdd(lines = listOf(line), cartId = cartID, {
            _cartState.value = it.data?.cartLinesAdd?.cart.toUiState()
            onComplete.invoke(it.data?.cartLinesAdd?.cart)
        })
    }

    private fun performCartCreate(variant: ID, onComplete: OnComplete) {
        val buyerIdentity = if (demoBuyerIdentityEnabled) {
            DemoBuyerIdentity.value
        } else {
            Storefront.CartBuyerIdentityInput().setCountryCode(Storefront.CountryCode.CA)
        }

        client.createCart(
            variant = Storefront.ProductVariant(variant),
            buyerIdentity = buyerIdentity,
            successCallback = { response ->
                val cart = response.data?.cartCreate?.cart
                _cartState.value = cart.toUiState()
                onComplete.invoke(cart)
            }
        )
    }

    private fun Cart?.toUiState(): CartState {
        if (this == null) {
            return CartState.Empty
        }
        val cartLines = lines.nodes.mapNotNull { cartLine ->
            (cartLine.merchandise as? Storefront.ProductVariant)?.let {
                CartLine(
                    title = it.product.title,
                    vendor = it.product.vendor,
                    quantity = cartLine.quantity,
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
                totalQuantity = totalQuantity
            ),
            checkoutUrl = checkoutUrl,
        )
    }

    suspend fun isGooglePayAvailable(activity: ComponentActivity): Boolean {
        val isReadyToPayJson = PaymentUtils.isReadyToPayRequest() ?: return false
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
        paymentsClient = PaymentUtils.createPaymentsClient(activity)

        return try {
            val task = paymentsClient!!.isReadyToPay(request)
            Tasks.await(task)
        } catch (exception: Exception) {
            log.w("isReadyToPay failed", exception.toString())
            false
        }
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @return a [Task] with the payment information.
     * @see [PaymentDataRequest](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest)
    ) */
    fun getLoadPaymentDataTask(priceCents: Long): Task<PaymentData> {
        val paymentDataRequestJson = PaymentUtils.getPaymentDataRequest(priceCents)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient!!.loadPaymentData(request)
    }

    val paymentDataLauncher = registerForActivityResult(GetPaymentDataResult()) { taskResult ->
        when (taskResult.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                taskResult.result!!.let {
                    log.w("Google Pay result:", it.toJson())
                    model.setPaymentData(it)
                }
            }
            //CommonStatusCodes.CANCELED -> The user canceled
            //AutoResolveHelper.RESULT_ERROR -> The API returned an error (it.status: Status)
            //CommonStatusCodes.INTERNAL_ERROR -> Handle other unexpected errors
            else -> {}
        }
    }
}

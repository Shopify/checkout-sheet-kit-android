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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartState
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.GooglePay
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.Product
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariant
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantOptionDetails
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductVariantSelectedOption
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class ProductViewModel(
    private val cartViewModel: CartViewModel,
    private val productRepository: ProductRepository,
    private val paymentsClient: PaymentsClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    private val _paymentUiState: MutableStateFlow<PaymentUiState> = MutableStateFlow(PaymentUiState.NotStarted)
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

    fun setAddQuantityAmount(quantity: Int) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Loaded) {
            Timber.i("Updating addQuantityAmount to $quantity")
            _uiState.value = currentState.copy(addQuantityAmount = quantity)
        }
    }

    fun addToCart() = viewModelScope.launch {
        val state = _uiState.value
        if (state is ProductUIState.Loaded) {
            val quantity = state.addQuantityAmount
            setIsAddingToCart(true)
            cartViewModel.addToCart(state.selectedVariant.id, quantity)
            setIsAddingToCart(false)
        }
    }

    fun initiateGooglePayment() = viewModelScope.launch {
        val paymentState = _paymentUiState.value
        if (paymentState is PaymentUiState.Available) {
            val state = _uiState.value
            if (state is ProductUIState.Loaded) {
                val quantity = state.addQuantityAmount
                setIsAddingToCart(true)
                val cart = cartViewModel.addToCart(state.selectedVariant.id, quantity, true)
                _paymentUiState.value = PaymentUiState.InProgress(cart = cart)
            }
        }
    }

    fun fetchProduct(productId: ID) = viewModelScope.launch {
        Timber.i("Fetching product with id $productId")
        try {
            val product = productRepository.getProduct(productId)
            Timber.i("Fetching product complete $product")
            val selectedVariant = product.variants.first()
            _uiState.value = ProductUIState.Loaded(
                product = product,
                selectedVariant = selectedVariant,
                availableOptions = buildAvailableOptions(product, selectedVariant),
                isAddingToCart = false,
                addQuantityAmount = 1
            )
        } catch (e: Exception) {
            Timber.e("Fetching product failed $e")
            _uiState.value = ProductUIState.Error(e.message ?: "Unknown error")
        }
    }

    // Select a new variant option (e.g. size = large)
    fun updateSelectedOption(name: String, value: String) {
        val state = _uiState.value
        if (state is ProductUIState.Loaded) {
            val matchingVariant = state.product.variants.first { variant ->
                variant.selectedOptions.containsAll(newOptions(state.selectedVariant, name, value))
            }
            matchingVariant.let {
                _uiState.value = state.copy(
                    selectedVariant = it,
                    availableOptions = buildAvailableOptions(product = state.product, selectedVariant = it)
                )
            }
        }
    }

    /**
     * Determine the user's ability to pay with a payment method supported by your app and display
     * a Google Pay payment button.
    ) */
    suspend fun verifyGooglePayReadiness() = viewModelScope.launch {
        val newUiState: PaymentUiState = try {
            if (fetchCanUseGooglePay()) {
                PaymentUiState.Available
            } else {
                PaymentUiState.Error(CommonStatusCodes.ERROR)
            }
        } catch (exception: ApiException) {
            PaymentUiState.Error(exception.statusCode, exception.message)
        }

        _paymentUiState.update { newUiState }
    }

    fun googlePayCanceled() {
        Timber.i("Google pay request canceled by user")
        _paymentUiState.value = PaymentUiState.Available
    }

    /**
     * Determine the user's ability to pay with a payment method supported by your app.
    ) */
    private suspend fun fetchCanUseGooglePay(): Boolean {
        val request = IsReadyToPayRequest.fromJson(GooglePay.isReadyToPayRequest().toString())
        return paymentsClient.isReadyToPay(request).await()
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @return a [Task] with the payment information.
     * @see [PaymentDataRequest](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest)
    ) */
    fun getLoadPaymentDataTask(priceLabel: String, countryCode: String, currencyCode: String): Task<PaymentData> {
        val paymentDataRequestJson = GooglePay.getPaymentDataRequest(priceLabel, countryCode, currencyCode)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(request)
    }

    fun setPaymentData(paymentData: PaymentData) {
        val payState = extractPaymentBillingName(paymentData)?.let {
            PaymentUiState.PaymentCompleted(payerName = it)
        } ?: PaymentUiState.Error(CommonStatusCodes.INTERNAL_ERROR)

        _paymentUiState.update { payState }
    }

    private fun extractPaymentBillingName(paymentData: PaymentData): String? {
        val paymentInformation = paymentData.toJson()

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Timber.i("BillingName $billingName")

            // Logging token string.
            Timber.i(
                "Google Pay token ${
                    paymentMethodData
                        .getJSONObject("tokenizationData")
                        .getString("token")
                }"
            )

            return billingName
        } catch (error: JSONException) {
            Timber.e("handlePaymentSuccess - Error: $error")
        }

        return null
    }

    // Returns variant options for the product, and whether the option is available for sale (when combined with other options on the
    // currently selected variant) e.g. { "size": [{"large", true}, {"medium", false}], "color": [{"red", true}, {"blue", false}]}
    private fun buildAvailableOptions(product: Product, selectedVariant: ProductVariant): Map<String, List<ProductVariantOptionDetails>> {
        // Only return available options if more than one option exists
        if (product.variants.size == 1) {
            return emptyMap()
        }

        val allOptions = product.variants.flatMap { variant -> variant.selectedOptions }
        return allOptions.map { option -> option.name }.distinct().associateWith { optionName ->
            allOptions.filter { option -> option.name == optionName }.map { it.value }.distinct().map { optionValue ->
                val newOptions = newOptions(selectedVariant, optionName, optionValue)
                ProductVariantOptionDetails(
                    name = optionValue,
                    availableForSale = product.variants.find { it.selectedOptions.containsAll(newOptions) }!!.availableForSale,
                )
            }
        }
    }

    // Modifies the options for the selected variant (e.g: [size: large, color: red]) by replacing one with a new option (e.g. color: blue)
    // to return e.g. [size: large, color: blue]
    private fun newOptions(selectedVariant: ProductVariant, name: String, value: String): List<ProductVariantSelectedOption> =
        selectedVariant.selectedOptions
            .filter { it.name != name }
            .plus(ProductVariantSelectedOption(name, value))

    private fun setIsAddingToCart(value: Boolean) {
        val currentState = _uiState.value
        if (currentState is ProductUIState.Loaded) {
            Timber.i("isAddingToCart - $value")
            _uiState.value = currentState.copy(isAddingToCart = value)
        }
    }
}

sealed class ProductUIState {
    data object Loading : ProductUIState()
    data class Error(val error: String) : ProductUIState()
    data class Loaded(
        val product: Product,
        val selectedVariant: ProductVariant,
        val availableOptions: Map<String, List<ProductVariantOptionDetails>>,
        val isAddingToCart: Boolean,
        val addQuantityAmount: Int
    ) : ProductUIState()
}

sealed class PaymentUiState {
    data object NotStarted : PaymentUiState()
    data object Available : PaymentUiState()
    data class InProgress(val cart: CartState.Cart) : PaymentUiState()
    data class PaymentCompleted(val payerName: String) : PaymentUiState()
    data class Error(val code: Int, val message: String? = null) : PaymentUiState()
}

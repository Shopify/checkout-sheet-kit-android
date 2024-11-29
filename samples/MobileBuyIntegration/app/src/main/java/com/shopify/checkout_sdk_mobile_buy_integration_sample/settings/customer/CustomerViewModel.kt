package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.Customer
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    fun loadCustomer() = viewModelScope.launch {
        val customer = customerRepository.getCustomer()
        if (customer != null) {
            _uiState.value = UIState.Loaded(customer)
        } else {
            _uiState.value = UIState.Error
        }
    }
}

sealed class UIState {
    data object Loading : UIState()
    data object Error : UIState()
    data class Loaded(val customer: Customer) : UIState()
}

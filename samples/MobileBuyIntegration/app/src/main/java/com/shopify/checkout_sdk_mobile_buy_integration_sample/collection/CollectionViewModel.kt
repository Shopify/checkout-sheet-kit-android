package com.shopify.checkout_sdk_mobile_buy_integration_sample.collection

import androidx.lifecycle.ViewModel
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CollectionViewModel(private val client: StorefrontClient) : ViewModel() {
    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState.asStateFlow()

    fun fetchCollection(handle: String) {
        client.fetchCollection(handle, numProducts = 10, successCallback = { result ->
            val collection = result.data?.collection
            if (collection != null) {
                _uiState.value = CollectionUIState.Loaded(collection = collection)
            } else {
                _uiState.value = CollectionUIState.Error("Failed to fetch collection")
            }
        }, failureCallback = {
            _uiState.value = CollectionUIState.Error("Failed to fetch collection")
        })
    }
}

sealed class CollectionUIState {
    data object Loading : CollectionUIState()
    data class Error(val error: String) : CollectionUIState()
    data class Loaded(val collection: Storefront.Collection) : CollectionUIState()
}

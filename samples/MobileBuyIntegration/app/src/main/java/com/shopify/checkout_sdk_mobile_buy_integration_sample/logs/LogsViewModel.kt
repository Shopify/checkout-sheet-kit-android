package com.shopify.checkout_sdk_mobile_buy_integration_sample.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogDatabase
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogsViewModel(private val logDb: LogDatabase, ): ViewModel() {

    private val _logState = MutableStateFlow<LogState>(LogState.Loading)
    val logState: StateFlow<LogState> = _logState.asStateFlow()

    fun checkLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            _logState.value = LogState.Populated(
                logDb.logDao().getLast(10)
            )
        }
    }

    fun clear() {
        viewModelScope.launch(Dispatchers.IO) {
            logDb.logDao().clear()
            _logState.value = LogState.Populated(emptyList())
        }
    }
}

sealed class LogState {
    object Loading: LogState()
    data class Populated(
        val logs: List<LogLine>
    ): LogState()
}

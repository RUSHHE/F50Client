package org.rushhe.f50client.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.rushhe.f50client.model.Message
import org.rushhe.f50client.repository.F50ClientRepository
import org.rushhe.f50client.repository.F50ClientRepositoryInterface

sealed class SMSListUiState {
    data object Loading : SMSListUiState()
    data class Error(val message: String) : SMSListUiState()
    data class Success(val result: List<Message>) : SMSListUiState()
}

class SMSViewModel : ViewModel(), KoinComponent {
    private val f50ClientRepository: F50ClientRepositoryInterface by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    val uiState = f50ClientRepository.fetchSMSAsFlow()
        .map { SMSListUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SMSListUiState.Loading)

    var networkState = (f50ClientRepository as F50ClientRepository).networkState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SMSListUiState.Loading)

    val pollingJob = CoroutineScope(Dispatchers.IO).launch {
        while (settingsViewModel.pollingInterval.value > 0) {
            f50ClientRepository.fetchAndStoreSMS()
            delay(settingsViewModel.pollingInterval.value * 1000L) // 延迟以实现轮询间隔
        }
    }

    fun refresh() {
        viewModelScope.launch {
            f50ClientRepository.fetchAndStoreSMS()
        }
    }
}
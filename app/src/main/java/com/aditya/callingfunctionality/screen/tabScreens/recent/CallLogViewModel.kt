package com.aditya.callingfunctionality.screen.tabScreens.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.callingfunctionality.component.CallLogItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val repository: CallLogRepository
) : ViewModel() {

    private val _groupedLogs = MutableStateFlow<Map<String, List<CallLogItem>>>(emptyMap())
    val groupedLogs: StateFlow<Map<String, List<CallLogItem>>> = _groupedLogs

    init {
        refreshLogs() // Initial load
    }

    fun refreshLogs() {
        viewModelScope.launch {
            val groups = repository.getGroupedCallLogs()
            _groupedLogs.value = groups
        }
    }
}


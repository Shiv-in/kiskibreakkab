package com.example.kiskibreakkab.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.PreferenceManager
import com.example.kiskibreakkab.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady = _isAuthReady.asStateFlow()

    val isDarkTheme = preferenceManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val currentUser = authRepository.currentUser
        .onEach { _isAuthReady.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleTheme() {
        viewModelScope.launch {
            preferenceManager.saveTheme(!isDarkTheme.value)
        }
    }
}

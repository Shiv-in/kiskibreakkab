package com.example.kiskibreakkab.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.usecase.ValidateUserIdentityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val validateUserIdentityUseCase: ValidateUserIdentityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(uid: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.login(uid, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(uid: String, name: String, email: String, password: String, confirmPassword: String) {
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        
        val validation = validateUserIdentityUseCase(uid, email)
        if (validation is ValidateUserIdentityUseCase.ValidationResult.Error) {
            _uiState.value = AuthUiState.Error(validation.message)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val user = User(uid = uid, name = name, email = email, section = "", labGroup = "")
            val result = repository.register(user, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.VerificationSent
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !email.contains("@cuchd.in")) {
            _uiState.value = AuthUiState.Error("Enter a valid @cuchd.in email")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.PasswordResetSent
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset link")
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object VerificationSent : AuthUiState()
    object PasswordResetSent : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

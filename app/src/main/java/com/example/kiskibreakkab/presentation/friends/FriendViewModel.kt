package com.example.kiskibreakkab.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.FriendRequest
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    _uiState.update { it.copy(currentUser = user) }
                    
                    // Collect friends
                    launch {
                        friendRepository.getFriends(user.userId).collect { friends ->
                            _uiState.update { it.copy(friends = friends) }
                        }
                    }
                    
                    // Collect incoming requests
                    launch {
                        friendRepository.getIncomingRequests(user.userId).collect { requests ->
                            _uiState.update { it.copy(incomingRequests = requests) }
                        }
                    }
                }
            }
        }
    }

    fun sendFriendRequest(receiverUid: String) {
        val currentUser = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = friendRepository.sendRequest(currentUser, receiverUid)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Request sent successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        val currentUser = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            friendRepository.acceptRequest(request, currentUser)
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.rejectRequest(requestId)
        }
    }

    fun removeFriend(friendId: String) {
        val currentUser = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            friendRepository.removeFriend(currentUser.userId, friendId)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

data class FriendUiState(
    val currentUser: User? = null,
    val friends: List<User> = emptyList(),
    val incomingRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

package com.example.kiskibreakkab.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.Squad
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.FriendRepository
import com.example.kiskibreakkab.domain.repository.SquadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SquadViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val squadRepository: SquadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SquadUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    _uiState.update { it.copy(currentUser = user) }
                    
                    // Collect friends for member selection
                    launch {
                        friendRepository.getFriends(user.userId).collect { friends ->
                            _uiState.update { it.copy(availableFriends = friends) }
                        }
                    }
                    
                    // Collect squads
                    launch {
                        squadRepository.getSquads(user.userId).collect { squads ->
                            _uiState.update { it.copy(squads = squads) }
                        }
                    }
                }
            }
        }
    }

    fun createSquad(name: String, selectedMemberIds: List<String>) {
        val currentUser = _uiState.value.currentUser ?: return
        val allMemberIds = selectedMemberIds + currentUser.userId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = squadRepository.createSquad(name, allMemberIds)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Squad mobilized!") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to mobilize squad") }
            }
        }
    }

    fun viewSquadIntel(squad: Squad) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSquad = squad, isLoadingCommonSlots = true) }
            val result = squadRepository.getCommonFreeSlots(squad.memberIds)
            if (result.isSuccess) {
                _uiState.update { it.copy(commonFreeSlots = result.getOrDefault(emptyList()), isLoadingCommonSlots = false) }
            } else {
                _uiState.update { it.copy(isLoadingCommonSlots = false, error = "Failed to detect common free time") }
            }
        }
    }

    fun closeIntel() {
        _uiState.update { it.copy(selectedSquad = null, commonFreeSlots = emptyList()) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

data class SquadUiState(
    val currentUser: User? = null,
    val squads: List<Squad> = emptyList(),
    val availableFriends: List<User> = emptyList(),
    val selectedSquad: Squad? = null,
    val commonFreeSlots: List<TimetableSlot> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingCommonSlots: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

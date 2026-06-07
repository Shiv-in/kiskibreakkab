package com.example.kiskibreakkab.presentation.friends

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.DashboardRepository
import com.example.kiskibreakkab.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendScheduleViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val dashboardRepository: DashboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String = checkNotNull(savedStateHandle["friendId"])

    private val _uiState = MutableStateFlow(FriendScheduleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFriendData()
    }

    private fun loadFriendData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Get user info
            dashboardRepository.getUserData(friendId).collect { user ->
                _uiState.update { it.copy(friend = user) }
            }
        }
        
        viewModelScope.launch {
            // Get timetable
            timetableRepository.getTimetable(friendId).collect { slots ->
                _uiState.update { it.copy(timetable = slots, isLoading = false) }
            }
        }
    }
}

data class FriendScheduleUiState(
    val friend: User? = null,
    val timetable: List<TimetableSlot> = emptyList(),
    val isLoading: Boolean = false
)

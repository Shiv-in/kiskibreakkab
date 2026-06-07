package com.example.kiskibreakkab.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _currentTime = MutableStateFlow(TimeUtils.formatCurrentTime())
    val currentTime = _currentTime.asStateFlow()

    private val _currentDay = MutableStateFlow(TimeUtils.getCurrentDay())
    val currentDay = _currentDay.asStateFlow()

    private val _currentSlot = MutableStateFlow(TimeUtils.getCurrentSlot())
    val currentSlot = _currentSlot.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userData = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val friendsFreeNow = combine(userData, _currentDay, _currentSlot) { user, day, slot ->
        if (user != null && slot != null) {
            dashboardRepository.getFriendsFreeNow(user.userId, day, slot.slotNumber)
        } else flowOf(emptyList())
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val freeRooms = combine(_currentDay, _currentSlot) { day, slot ->
        if (slot != null) {
            dashboardRepository.getFreeRooms(day, slot.slotNumber)
        } else flowOf(emptyList())
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val squadCount = userData.flatMapLatest { user ->
        if (user != null) dashboardRepository.getSquadCount(user.userId)
        else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val friendCount = userData.flatMapLatest { user ->
        if (user != null) dashboardRepository.getFriendCount(user.userId)
        else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        updateTimePeriodically()
    }

    private fun updateTimePeriodically() {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = TimeUtils.formatCurrentTime()
                _currentDay.value = TimeUtils.getCurrentDay()
                _currentSlot.value = TimeUtils.getCurrentSlot()
                delay(60000) // Update every minute
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

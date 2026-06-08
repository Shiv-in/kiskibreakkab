package com.example.kiskibreakkab.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.TemporaryLocation
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class RoomWithDuration(
    val room: Room,
    val duration: Int,
    val occupants: List<String> = emptyList()
)

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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userData = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val friendsFreeNow = combine(userData, _currentDay, _currentSlot) { user, day, slot ->
        if (user != null && slot != null) {
            dashboardRepository.getFriendsFreeNow(user.userId, day, slot.slotNumber)
        } else flowOf(emptyList())
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Enhanced Free Rooms with Duration calculation
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val freeRooms = combine(_currentDay, _currentSlot) { day, slot ->
        if (slot != null) {
            dashboardRepository.getAllRoomsForDay(day).map { allRooms ->
                // Filter for rooms free in CURRENT slot
                val currentFree = allRooms.filter { it.slotNumber == slot.slotNumber && it.isAvailable }
                
                currentFree.map { room ->
                    // Calculate continuous duration
                    var duration = 0
                    for (s in slot.slotNumber..8) {
                        val isFreeInSlot = allRooms.any { 
                            it.roomName == room.roomName && it.slotNumber == s && it.isAvailable 
                        }
                        if (isFreeInSlot) duration++ else break
                    }
                    RoomWithDuration(room, duration, room.occupantNames)
                }
                .sortedByDescending { it.duration }
            }
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
                delay(1000) // Update every second for live feel
            }
        }
    }

    fun markSittingHere(roomName: String, duration: Int) {
        viewModelScope.launch {
            val user = userData.value ?: return@launch
            val slot = currentSlot.value ?: return@launch
            val day = currentDay.value
            
            val location = TemporaryLocation(
                room = roomName,
                day = day,
                startSlot = slot.slotNumber,
                endSlot = slot.slotNumber + duration - 1,
                setAt = LocalDateTime.now().toString()
            )
            
            dashboardRepository.setTemporaryLocation(user.userId, location)
        }
    }

    fun clearLocation() {
        viewModelScope.launch {
            val user = userData.value ?: return@launch
            dashboardRepository.setTemporaryLocation(user.userId, null)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

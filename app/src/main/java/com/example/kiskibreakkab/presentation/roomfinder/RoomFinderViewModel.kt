package com.example.kiskibreakkab.presentation.roomfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.PreferenceManager
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomFinderViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(TimeUtils.getCurrentDay())
    val selectedDay = _selectedDay.asStateFlow()

    private val _selectedSlot = MutableStateFlow(TimeUtils.getCurrentSlot()?.slotNumber ?: 6)
    val selectedSlot = _selectedSlot.asStateFlow()

    private val _selectedBlock = MutableStateFlow<String?>("B6")
    val selectedBlock = _selectedBlock.asStateFlow()

    private val _selectedDept = MutableStateFlow<String?>(null)
    val selectedDept = _selectedDept.asStateFlow()

    init {
        // Load persistent selections
        viewModelScope.launch {
            preferenceManager.selectedDay.collect { day ->
                day?.let { _selectedDay.value = it }
            }
        }
        viewModelScope.launch {
            preferenceManager.selectedSlot.collect { slot ->
                slot?.let { _selectedSlot.value = it }
            }
        }
        viewModelScope.launch {
            preferenceManager.selectedBlock.collect { block ->
                block?.let { _selectedBlock.value = it }
            }
        }
        viewModelScope.launch {
            preferenceManager.selectedDept.collect { dept ->
                dept?.let { _selectedDept.value = it }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rooms = combine(_selectedDay, _selectedSlot, _selectedBlock, _selectedDept) { day, slot, block, dept ->
        roomRepository.getRooms(day, slot, block, dept)
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: String) {
        _selectedDay.value = day
        viewModelScope.launch { preferenceManager.saveSelectedDay(day) }
    }

    fun selectSlot(slotNumber: Int) {
        _selectedSlot.value = slotNumber
        viewModelScope.launch { preferenceManager.saveSelectedSlot(slotNumber) }
    }

    fun selectBlock(block: String?) {
        _selectedBlock.value = block
        viewModelScope.launch { preferenceManager.saveSelectedBlock(block) }
    }

    fun selectDept(dept: String?) {
        _selectedDept.value = dept
        viewModelScope.launch { preferenceManager.saveSelectedDept(dept) }
    }

    fun claimRoom(roomId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                roomRepository.claimRoom(roomId, user.userId)
            }
        }
    }

    fun occupyRoom(roomName: String?) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                roomRepository.occupyRoom(roomName, user.userId)
            }
        }
    }
}

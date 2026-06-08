package com.example.kiskibreakkab.presentation.roomfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.PreferenceManager
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    private val _selectedBlock = MutableStateFlow<String?>(null)
    val selectedBlock = _selectedBlock.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val availableBlocks = roomRepository.getAllBlocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load persistent selections
        viewModelScope.launch {
            preferenceManager.selectedDay.collect { day ->
                day?.let { _selectedDay.value = it.uppercase() }
            }
        }
        viewModelScope.launch {
            preferenceManager.selectedSlot.collect { slot ->
                slot?.let { _selectedSlot.value = it }
            }
        }
        viewModelScope.launch {
            preferenceManager.selectedBlock.collect { block ->
                block?.let { _selectedBlock.value = it.uppercase() }
            }
        }
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rooms = combine(_selectedDay, _selectedSlot, _selectedBlock, refreshTrigger) { day, slot, block, _ ->
        val blockFilter = if (block == "SELECT BLOCK" || block.isNullOrBlank()) null else block.uppercase().trim()
        roomRepository.getRooms(day.uppercase().trim(), slot, blockFilter).map { list ->
            list
        }
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks if user has claimed a room in current slot
    val userClaimedRoomId = combine(rooms, authRepository.currentUser) { roomList, user ->
        roomList.find { it.claimedBy == user?.userId }?.roomId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDay(day: String) {
        val normalized = day.uppercase().trim()
        _selectedDay.value = normalized
        viewModelScope.launch { preferenceManager.saveSelectedDay(normalized) }
    }

    fun selectSlot(slotNumber: Int) {
        _selectedSlot.value = slotNumber
        viewModelScope.launch { preferenceManager.saveSelectedSlot(slotNumber) }
    }

    fun selectBlock(block: String?) {
        val normalized = block?.uppercase()?.trim()
        _selectedBlock.value = normalized
        if (normalized != null && normalized != "SELECT BLOCK") {
            viewModelScope.launch { preferenceManager.saveSelectedBlock(normalized) }
        }
    }

    fun refreshRooms() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    fun claimRoom(roomId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                // If already has a claim, release it first (like web logic)
                userClaimedRoomId.value?.let { oldId ->
                    roomRepository.releaseRoom(oldId, user.name)
                }
                roomRepository.claimRoom(roomId, user.userId, user.name)
            }
        }
    }

    fun releaseRoom() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val roomId = userClaimedRoomId.value
            if (user != null && roomId != null) {
                roomRepository.releaseRoom(roomId, user.name)
            }
        }
    }

    fun occupyRoom(roomId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                roomRepository.occupyRoom(roomId, user.userId)
            }
        }
    }
}

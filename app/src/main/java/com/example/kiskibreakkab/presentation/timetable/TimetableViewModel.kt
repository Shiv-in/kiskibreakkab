package com.example.kiskibreakkab.presentation.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    // Main UI state: Derived directly from the repository flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val timetable = authRepository.currentUser.flatMapLatest { user ->
        if (user != null) {
            timetableRepository.getTimetable(user.userId).onEach { slots ->
                if (slots.isEmpty()) {
                    // Auto-initialize if completely empty
                    timetableRepository.saveTimetable(user.userId, generateInitialTimetable())
                }
            }.map { slots ->
                if (slots.isEmpty()) {
                    generateInitialTimetable()
                } else {
                    // Check for Saturday migration
                    val hasSaturday = slots.any { it.day == "SAT" }
                    if (!hasSaturday) {
                        val migrated = slots + TimeUtils.slots.map { it.copy(day = "SAT", isFree = true) }
                        timetableRepository.saveTimetable(user.userId, migrated)
                        migrated
                    } else {
                        slots
                    }
                }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    val userUid = authRepository.currentUser.map { it?.uid ?: "SCANNING..." }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SCANNING...")

    private fun generateInitialTimetable(): List<TimetableSlot> {
        val initialList = mutableListOf<TimetableSlot>()
        TimeUtils.WEEK_DAYS.forEach { day ->
            TimeUtils.slots.forEach { baseSlot ->
                initialList.add(baseSlot.copy(day = day, isFree = true, lastUpdated = 1L))
            }
        }
        return initialList
    }

    fun toggleSlot(day: String, slotNumber: Int) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val currentSlots = timetable.value
            val targetSlot = currentSlots.find { it.day == day && it.slotNumber == slotNumber } ?: return@launch
            
            val updatedSlot = targetSlot.copy(isFree = !targetSlot.isFree)
            
            // Persist immediately to Repository (Local + Cloud)
            timetableRepository.updateSlot(user.userId, updatedSlot)
        }
    }

    fun updateSlotLocation(day: String, slotNumber: Int, location: String?) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val currentSlots = timetable.value
            val targetSlot = currentSlots.find { it.day == day && it.slotNumber == slotNumber } ?: return@launch
            
            val updatedSlot = targetSlot.copy(isFree = true, location = location)
            timetableRepository.updateSlot(user.userId, updatedSlot)
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                _isSaving.value = true
                timetableRepository.saveTimetable(user.userId, timetable.value)
                _isSaving.value = false
            }
        }
    }
}

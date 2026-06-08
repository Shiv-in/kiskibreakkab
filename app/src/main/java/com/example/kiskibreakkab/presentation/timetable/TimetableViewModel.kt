package com.example.kiskibreakkab.presentation.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _timetable = MutableStateFlow<List<TimetableSlot>>(emptyList())
    val timetable = _timetable.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    val userUid = authRepository.currentUser.map { it?.uid ?: "SCANNING..." }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SCANNING...")

    init {
        loadTimetable()
    }

    private var hasLoaded = false

    private fun loadTimetable() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    timetableRepository.getTimetable(user.userId).collectLatest { remoteSlots ->
                        if (remoteSlots.isEmpty()) {
                            if (!hasLoaded) {
                                val initial = generateInitialTimetable()
                                _timetable.update { initial }
                                timetableRepository.saveTimetable(user.userId, initial)
                                hasLoaded = true
                            }
                        } else {
                            // Check if Saturday is missing (migration for old users)
                            val hasSaturday = remoteSlots.any { it.day == "SAT" }
                            if (!hasSaturday) {
                                val saturdaySlots = TimeUtils.slots.map { it.copy(day = "SAT", isFree = true) }
                                val updatedList = remoteSlots + saturdaySlots
                                _timetable.update { updatedList }
                                timetableRepository.saveTimetable(user.userId, updatedList)
                                hasLoaded = true
                            } else {
                                // Only update local state if we haven't loaded yet or if we're not currently saving
                                if (!hasLoaded) {
                                    _timetable.update { remoteSlots }
                                    hasLoaded = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateInitialTimetable(): List<TimetableSlot> {
        val initialList = mutableListOf<TimetableSlot>()
        TimeUtils.WEEK_DAYS.forEach { day ->
            TimeUtils.slots.forEach { baseSlot ->
                initialList.add(baseSlot.copy(day = day, isFree = true))
            }
        }
        return initialList
    }

    fun toggleSlot(day: String, slotNumber: Int) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val currentSlot = _timetable.value.find { it.day == day && it.slotNumber == slotNumber } ?: return@launch
            
            val updatedSlot = currentSlot.copy(isFree = !currentSlot.isFree)
            
            // Optimistic update
            _timetable.update { list ->
                list.map { if (it.day == day && it.slotNumber == slotNumber) updatedSlot else it }
            }
            
            // Persist immediately
            timetableRepository.updateSlot(user.userId, updatedSlot)
        }
    }

    fun saveChanges() {
        // Now optional since we save on toggle, but kept for bulk UI feedback if needed
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                _isSaving.value = true
                timetableRepository.saveTimetable(user.userId, _timetable.value)
                _isSaving.value = false
            }
        }
    }
}

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

    private val days = listOf("MON", "TUE", "WED", "THU", "FRI")
    private var isInitialized = false

    init {
        loadTimetable()
    }

    private fun loadTimetable() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    timetableRepository.getTimetable(user.userId).collectLatest { remoteSlots ->
                        // Only update if we haven't initialized or if we're not currently saving
                        if (!_isSaving.value) {
                            if (remoteSlots.isEmpty()) {
                                if (!isInitialized) {
                                    _timetable.update { generateInitialTimetable() }
                                    isInitialized = true
                                }
                            } else {
                                _timetable.update { remoteSlots }
                                isInitialized = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateInitialTimetable(): List<TimetableSlot> {
        val initialList = mutableListOf<TimetableSlot>()
        days.forEach { day ->
            TimeUtils.slots.forEach { baseSlot ->
                initialList.add(baseSlot.copy(day = day, isFree = true))
            }
        }
        return initialList
    }

    fun toggleSlot(day: String, slotNumber: Int) {
        _timetable.update { currentList ->
            currentList.map {
                if (it.day == day && it.slotNumber == slotNumber) {
                    it.copy(isFree = !it.isFree)
                } else it
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null) {
                _isSaving.value = true
                val result = timetableRepository.saveTimetable(user.userId, _timetable.value)
                if (result.isSuccess) {
                    // Local state is already updated via toggleSlot and StateFlow collection
                }
                _isSaving.value = false
            }
        }
    }
}

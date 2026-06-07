package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.TimetableSlot
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {
    fun getTimetable(userId: String): Flow<List<TimetableSlot>>
    suspend fun updateSlot(userId: String, slot: TimetableSlot): Result<Unit>
    suspend fun saveTimetable(userId: String, timetable: List<TimetableSlot>): Result<Unit>
}

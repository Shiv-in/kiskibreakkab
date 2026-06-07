package com.example.kiskibreakkab.data.local.dao

import androidx.room.*
import com.example.kiskibreakkab.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable WHERE userId = :userId ORDER BY day, slotNumber ASC")
    fun getAllSlots(userId: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<TimetableEntity>)

    @Update
    suspend fun updateSlot(slot: TimetableEntity)

    @Query("DELETE FROM timetable WHERE userId = :userId")
    suspend fun clearUserTimetable(userId: String)

    @Query("DELETE FROM timetable")
    suspend fun clearAll()
}

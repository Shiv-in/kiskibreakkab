package com.example.kiskibreakkab.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "timetable",
    primaryKeys = ["userId", "day", "slotNumber"]
)
data class TimetableEntity(
    val userId: String,
    val day: String,
    val slotNumber: Int,
    val startTime: String,
    val endTime: String,
    val isFree: Boolean,
    val location: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

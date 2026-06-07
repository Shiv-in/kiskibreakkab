package com.example.kiskibreakkab.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "timetable",
    primaryKeys = ["day", "slotNumber"]
)
data class TimetableEntity(
    val day: String,
    val slotNumber: Int,
    val startTime: String,
    val endTime: String,
    val isFree: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

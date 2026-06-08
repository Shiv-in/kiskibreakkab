package com.example.kiskibreakkab.domain.model

data class TimetableSlot(
    val day: String = "",
    val slotNumber: Int = 0,
    val startTime: String = "", // HH:mm format
    val endTime: String = "",   // HH:mm format
    val isFree: Boolean = true,
    val location: String? = null, // For FREE@Location logic
    val lastUpdated: Long = 0L
)

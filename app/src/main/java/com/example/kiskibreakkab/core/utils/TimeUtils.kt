package com.example.kiskibreakkab.core.utils

import com.example.kiskibreakkab.domain.model.TimetableSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    val slots = listOf(
        TimetableSlot("", 1, "09:30", "10:20"),
        TimetableSlot("", 2, "10:20", "11:10"),
        TimetableSlot("", 3, "11:20", "12:10"),
        TimetableSlot("", 4, "12:10", "13:00"),
        TimetableSlot("", 5, "13:00", "13:55"),
        TimetableSlot("", 6, "13:55", "14:45"),
        TimetableSlot("", 7, "14:45", "15:35"),
        TimetableSlot("", 8, "15:35", "16:25")
    )

    fun getCurrentDay(): String {
        return Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "MON"
    }

    fun getCurrentSlot(): TimetableSlot? {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return slots.find { slot ->
            val start = LocalTime.parse(slot.startTime, formatter)
            val end = LocalTime.parse(slot.endTime, formatter)
            (now.isAfter(start) || now.equals(start)) && now.isBefore(end)
        }
    }

    fun formatCurrentTime(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
    }
}

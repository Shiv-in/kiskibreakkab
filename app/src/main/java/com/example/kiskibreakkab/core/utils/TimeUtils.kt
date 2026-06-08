package com.example.kiskibreakkab.core.utils

import com.example.kiskibreakkab.domain.model.TimetableSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    val WEEK_DAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")

    val slots = listOf(
        TimetableSlot("", 1, "09:30", "10:20"),
        TimetableSlot("", 2, "10:20", "11:10"),
        TimetableSlot("", 3, "11:20", "12:10"),
        TimetableSlot("", 4, "12:10", "13:00"),
        TimetableSlot("", 5, "13:05", "13:55"),
        TimetableSlot("", 6, "13:55", "14:45"),
        TimetableSlot("", 7, "14:45", "15:35"),
        TimetableSlot("", 8, "15:35", "16:25")
    )

    fun getCurrentDay(): String {
        return Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "MON"
    }

    fun getCurrentSlot(): TimetableSlot? {
        val now = LocalTime.now()
        val minutes = now.hour * 60 + now.minute

        // Website timings logic:
        // Slot 1: 9:30 - 10:20 (570 - 620)
        // Slot 2: 10:20 - 11:10 (620 - 670)
        // Slot 3: 11:20 - 12:10 (680 - 730) -> Break 11:10-11:20 (670-680)
        // Slot 4: 12:10 - 1:00 (730 - 780)
        // Slot 5: 1:05 - 1:55 (785 - 835) -> Break 1:00-1:05 (780-785)
        // Slot 6: 1:55 - 2:45 (835 - 885)
        // Slot 7: 2:45 - 3:35 (885 - 935)
        // Slot 8: 3:35 - 4:25 (935 - 985)

        val slotNumber = when {
            // Match website logic: Break periods show the UPCOMING slot
            minutes >= 570 && minutes < 620 -> 1       // 9:30 - 10:20
            minutes >= 620 && minutes < 670 -> 2       // 10:20 - 11:10
            minutes >= 670 && minutes < 730 -> 3       // 11:10 - 12:10 (Includes 11:10-11:20 break)
            minutes >= 730 && minutes < 780 -> 4       // 12:10 - 1:00
            minutes >= 780 && minutes < 835 -> 5       // 1:00 - 1:55 (Includes 1:00-1:05 break)
            minutes >= 835 && minutes < 885 -> 6       // 1:55 - 2:45
            minutes >= 885 && minutes < 935 -> 7       // 2:45 - 3:35
            minutes >= 935 && minutes < 985 -> 8       // 3:35 - 4:25
            else -> null
        }

        return if (slotNumber != null) slots.find { it.slotNumber == slotNumber } else null
    }

    fun isOffHours(): Boolean {
        val now = LocalTime.now()
        val minutes = now.hour * 60 + now.minute
        return minutes < 570 || minutes >= 985
    }

    fun formatCurrentTime(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    fun isWeekend(): Boolean {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return day == Calendar.SUNDAY
    }
}

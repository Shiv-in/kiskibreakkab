package com.example.kiskibreakkab.domain.model

data class User(
    val userId: String = "",
    val uid: String = "", // College UID
    val name: String = "",
    val email: String = "",
    val section: String = "",
    val labGroup: String = "",
    val currentRoom: String? = null, // e.g. "B6_610"
    val temporaryLocation: TemporaryLocation? = null
)

data class TemporaryLocation(
    val room: String = "",
    val day: String = "",
    val startSlot: Int = 0,
    val endSlot: Int = 0,
    val setAt: String = ""
)

package com.example.kiskibreakkab.domain.model

data class User(
    val userId: String = "",
    val uid: String = "", // College UID
    val name: String = "",
    val email: String = "",
    val section: String = "",
    val labGroup: String = "",
    val currentRoom: String? = null // e.g. "B6_610"
)

package com.example.kiskibreakkab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val uid: String,
    val name: String,
    val email: String,
    val section: String,
    val labGroup: String,
    val lastSynced: Long = System.currentTimeMillis()
)

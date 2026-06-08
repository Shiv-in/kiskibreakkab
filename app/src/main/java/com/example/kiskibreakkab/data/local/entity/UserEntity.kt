package com.example.kiskibreakkab.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kiskibreakkab.domain.model.TemporaryLocation

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val uid: String,
    val name: String,
    val email: String,
    val section: String,
    val labGroup: String,
    @Embedded(prefix = "temp_") val temporaryLocation: TemporaryLocation? = null,
    val lastSynced: Long = System.currentTimeMillis()
)

package com.example.kiskibreakkab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kiskibreakkab.data.local.dao.TimetableDao
import com.example.kiskibreakkab.data.local.dao.UserDao
import com.example.kiskibreakkab.data.local.entity.TimetableEntity
import com.example.kiskibreakkab.data.local.entity.UserEntity

@Database(
    entities = [TimetableEntity::class, UserEntity::class], 
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun userDao(): UserDao
}

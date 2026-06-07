package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getUserData(userId: String): Flow<User?>
    fun getFriendsFreeNow(userId: String, day: String, slotNumber: Int): Flow<List<User>>
    fun getFreeRooms(day: String, slotNumber: Int): Flow<List<Room>>
    fun getSquadCount(userId: String): Flow<Int>
    fun getFriendCount(userId: String): Flow<Int>
}

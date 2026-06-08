package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun getRooms(
        day: String, 
        slotNumber: Int, 
        blockCode: String? = null
    ): Flow<List<Room>>

    fun getAllBlocks(): Flow<List<String>>

    fun getRoomsForDay(day: String): Flow<List<Room>>

    suspend fun clearAllRooms(): Result<Unit>

    suspend fun claimRoom(roomId: String, userId: String, userName: String): Result<Unit>
    suspend fun releaseRoom(roomId: String, userName: String): Result<Unit>
    suspend fun occupyRoom(roomId: String?, userId: String): Result<Unit>
}

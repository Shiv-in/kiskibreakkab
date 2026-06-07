package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun getRooms(
        day: String, 
        slotNumber: Int, 
        blockCode: String? = null,
        department: String? = null
    ): Flow<List<Room>>

    suspend fun claimRoom(roomId: String, userId: String): Result<Unit>
    suspend fun occupyRoom(roomId: String?, userId: String): Result<Unit>
}

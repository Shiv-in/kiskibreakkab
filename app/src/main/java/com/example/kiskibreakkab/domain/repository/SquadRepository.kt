package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.Squad
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SquadRepository {
    fun getSquads(userId: String): Flow<List<Squad>>
    fun getSquadMembers(memberIds: List<String>): Flow<List<User>>
    suspend fun createSquad(squadName: String, memberIds: List<String>): Result<Unit>
    suspend fun getCommonFreeSlots(memberIds: List<String>): Result<List<TimetableSlot>>
}

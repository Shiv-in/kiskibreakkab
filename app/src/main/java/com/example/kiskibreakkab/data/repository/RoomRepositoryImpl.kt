package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.repository.RoomRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RoomRepository {

    override fun getRooms(
        day: String,
        slotNumber: Int,
        blockCode: String?,
        department: String?
    ): Flow<List<Room>> = callbackFlow {
        var query: Query = firestore.collection("rooms")
            .whereEqualTo("day", day)
            .whereEqualTo("slotNumber", slotNumber)
            .whereEqualTo("isAvailable", true)

        if (blockCode != null) {
            query = query.whereEqualTo("blockCode", blockCode)
        }
        if (department != null) {
            query = query.whereEqualTo("department", department)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
            trySend(rooms)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun claimRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId)
                .update("claimedBy", userId, "isAvailable", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun occupyRoom(roomId: String?, userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("currentRoom", roomId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

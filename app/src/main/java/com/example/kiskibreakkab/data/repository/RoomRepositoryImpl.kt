package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.repository.RoomRepository
import com.google.firebase.firestore.FieldValue
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
        blockCode: String?
    ): Flow<List<Room>> = callbackFlow {
        // Build query step by step to be safe
        var query: Query = firestore.collection("rooms")
            .whereEqualTo("day", day.uppercase().trim())
            .whereEqualTo("slotNumber", slotNumber)
            .whereEqualTo("isAvailable", true)

        // Only add blockCode filter if it's a real block and not a placeholder
        if (!blockCode.isNullOrBlank() && blockCode != "SELECT BLOCK") {
            query = query.whereEqualTo("blockCode", blockCode.uppercase().trim())
        }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
            
            // Central Blacklist: Filter out entities that are definitely not actual student rooms
            val blacklist = listOf("Department of Computer Science")
            val filteredRooms = rooms.filter { room ->
                blacklist.none { it.equals(room.roomName, ignoreCase = true) }
            }
            
            trySend(filteredRooms)
        }
        awaitClose { listener.remove() }
    }

    override fun getAllBlocks(): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("rooms")
            .addSnapshotListener { snapshot, _ ->
                val blocks = snapshot?.documents?.mapNotNull { it.getString("blockCode") }
                    ?.distinct()
                    ?.sorted() ?: emptyList()
                trySend(blocks)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun claimRoom(roomId: String, userId: String, userName: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId)
                .update(
                    "claimedBy", userId, 
                    "isAvailable", false,
                    "occupantNames", FieldValue.arrayUnion(userName)
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun releaseRoom(roomId: String, userName: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId)
                .update(
                    "claimedBy", null,
                    "isAvailable", true,
                    "occupantNames", FieldValue.arrayRemove(userName)
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRoomsForDay(day: String): Flow<List<Room>> = callbackFlow {
        val listener = firestore.collection("rooms")
            .whereEqualTo("day", day)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
                
                val blacklist = listOf("Department of Computer Science")
                val filteredRooms = rooms.filter { room ->
                    blacklist.none { it.equals(room.roomName, ignoreCase = true) }
                }
                
                trySend(filteredRooms)
            }
        awaitClose { listener.remove() }
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

    override suspend fun clearAllRooms(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("rooms").get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.chunked(500).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

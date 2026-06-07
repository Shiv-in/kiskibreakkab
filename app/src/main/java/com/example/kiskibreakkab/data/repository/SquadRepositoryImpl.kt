package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.domain.model.Squad
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.SquadRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SquadRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SquadRepository {

    override fun getSquads(userId: String): Flow<List<Squad>> = callbackFlow {
        val listener = firestore.collection("squads")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val squads = snapshot?.toObjects(Squad::class.java) ?: emptyList()
                trySend(squads)
            }
        awaitClose { listener.remove() }
    }

    override fun getSquadMembers(memberIds: List<String>): Flow<List<User>> = callbackFlow {
        if (memberIds.isEmpty()) {
            trySend(emptyList())
        } else {
            val listener = firestore.collection("users")
                .whereIn("userId", memberIds)
                .addSnapshotListener { snapshot, _ ->
                    val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                    trySend(users)
                }
            awaitClose { listener.remove() }
        }
        awaitClose { }
    }

    override suspend fun createSquad(squadName: String, memberIds: List<String>): Result<Unit> {
        return try {
            val squadId = firestore.collection("squads").document().id
            val squad = Squad(squadId, squadName, memberIds)
            firestore.collection("squads").document(squadId).set(squad).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCommonFreeSlots(memberIds: List<String>): Result<List<TimetableSlot>> {
        return try {
            val allTimetables = mutableListOf<List<TimetableSlot>>()
            for (id in memberIds) {
                val snapshot = firestore.collection("users").document(id)
                    .collection("timetable").get().await()
                val slots = snapshot.toObjects(TimetableSlot::class.java)
                allTimetables.add(slots)
            }

            if (allTimetables.isEmpty()) return Result.success(emptyList())

            // Find slots that are FREE for everyone
            val firstUserSlots = allTimetables.first()
            val commonFree = firstUserSlots.filter { slot ->
                slot.isFree && allTimetables.all { userSlots ->
                    userSlots.any { it.day == slot.day && it.slotNumber == slot.slotNumber && it.isFree }
                }
            }

            Result.success(commonFree)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

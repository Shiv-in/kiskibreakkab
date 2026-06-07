package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.data.local.dao.UserDao
import com.example.kiskibreakkab.data.local.entity.UserEntity
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.DashboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : DashboardRepository {

    override fun getUserData(userId: String): Flow<User?> {
        return userDao.getUser(userId).map { it?.toDomain() }
    }

    override fun getFriendsFreeNow(userId: String, day: String, slotNumber: Int): Flow<List<User>> = callbackFlow {
        val friendsListener = firestore.collection("users").document(userId)
            .collection("friends")
            .addSnapshotListener { friendsSnapshot, _ ->
                val friendIds = friendsSnapshot?.documents?.map { it.id } ?: emptyList()
                
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // For every friend, check if their current slot is marked as FREE
                val slotId = "${day}_${slotNumber}"
                firestore.collectionGroup("timetable")
                    .whereEqualTo("day", day)
                    .whereEqualTo("slotNumber", slotNumber)
                    .whereEqualTo("isFree", true)
                    .get()
                    .addOnSuccessListener { timetableSnapshot ->
                        // This identifies all FREE slots in the system
                        // Now we filter for those belonging to your friends
                        val usersWithFreeSlots = timetableSnapshot.documents.map { it.reference.parent.parent!!.id }
                        val freeFriendsIds = friendIds.filter { usersWithFreeSlots.contains(it) }

                        if (freeFriendsIds.isEmpty()) {
                            trySend(emptyList())
                        } else {
                            firestore.collection("users")
                                .whereIn("userId", freeFriendsIds)
                                .get()
                                .addOnSuccessListener { usersSnapshot ->
                                    trySend(usersSnapshot.toObjects(User::class.java))
                                }
                        }
                    }
            }
        awaitClose { friendsListener.remove() }
    }

    override fun getFreeRooms(day: String, slotNumber: Int): Flow<List<Room>> = callbackFlow {
        val listener = firestore.collection("rooms")
            .whereEqualTo("day", day)
            .whereEqualTo("slotNumber", slotNumber)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, _ ->
                val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    override fun getSquadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("squads")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    override fun getFriendCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("friends")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    // Reuse mapper from Profile
    private fun UserEntity.toDomain() = User(
        userId = userId,
        uid = uid,
        name = name,
        email = email,
        section = section,
        labGroup = labGroup
    )
}

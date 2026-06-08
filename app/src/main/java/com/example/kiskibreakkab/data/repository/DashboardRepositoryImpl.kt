package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.data.local.dao.UserDao
import com.example.kiskibreakkab.data.local.entity.UserEntity
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.TemporaryLocation
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.DashboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : DashboardRepository {

    override fun getUserData(userId: String): Flow<User?> {
        return userDao.getUser(userId).map { it?.toDomain() }
    }

    override fun getFriendsFreeNow(userId: String, day: String, slotNumber: Int): Flow<List<User>> = callbackFlow {
        // Step 1: Get all confirmed friends
        val friendsListener = firestore.collection("users").document(userId)
            .collection("friends")
            .addSnapshotListener { friendsSnapshot, _ ->
                val friendIds = friendsSnapshot?.documents?.map { it.id } ?: emptyList()
                
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Step 2: Query users who are your friends and have a "FREE" slot or temporary location
                // Real-time listener for friends' data
                firestore.collection("users")
                    .whereIn("userId", friendIds)
                    .addSnapshotListener { usersSnapshot, _ ->
                        val friends = usersSnapshot?.toObjects(User::class.java) ?: emptyList()
                        
                        // Step 3: Filter those who are free at this exact moment
                        // This requires checking their sub-collection 'timetable' or their 'temporaryLocation'
                        // Since we can't easily query cross-collection in real-time for everyone, 
                        // we'll check their 'temporaryLocation' field which is on the user object.
                        
                        val freeFriends = friends.filter { friend ->
                            // Case 1: They explicitly marked themselves in a room
                            val temp = friend.temporaryLocation
                            val isLocallyFree = temp != null && 
                                               temp.day == day && 
                                               slotNumber >= temp.startSlot && 
                                               slotNumber <= temp.endSlot
                            
                            // Case 2: Their master timetable says they are free (optional check)
                            // For simplicity, we'll stick to Case 1 or implement a background sync
                            isLocallyFree
                        }
                        
                        trySend(freeFriends)
                    }
            }
        awaitClose { friendsListener.remove() }
    }

    override fun getFreeRooms(day: String, slotNumber: Int): Flow<List<Room>> = callbackFlow {
        val listener = firestore.collection("rooms")
            .whereEqualTo("day", day.uppercase().trim())
            .whereEqualTo("slotNumber", slotNumber)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, _ ->
                val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
                // Central Blacklist: Filter out entities that are not actual student rooms
                val blacklist = listOf("Department of Computer Science")
                val filteredRooms = rooms.filter { room ->
                    blacklist.none { it.equals(room.roomName, ignoreCase = true) }
                }
                trySend(filteredRooms)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllRoomsForDay(day: String): Flow<List<Room>> = callbackFlow {
        val listener = firestore.collection("rooms")
            .whereEqualTo("day", day.uppercase().trim())
            .addSnapshotListener { snapshot, _ ->
                val rooms = snapshot?.toObjects(Room::class.java) ?: emptyList()
                
                val blacklist = listOf("Department of Computer Science")
                val filteredRooms = rooms.filter { room ->
                    blacklist.none { it.equals(room.roomName, ignoreCase = true) }
                }
                
                trySend(filteredRooms)
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

    override suspend fun setTemporaryLocation(userId: String, location: TemporaryLocation?): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("temporaryLocation", location).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reuse mapper from Profile
    private fun UserEntity.toDomain() = User(
        userId = userId,
        uid = uid,
        name = name,
        email = email,
        section = section,
        labGroup = labGroup,
        temporaryLocation = temporaryLocation
    )
}

package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.domain.model.FriendRequest
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.FriendRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FriendRepository {

    override fun getFriends(userId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val friendIds = snapshot?.documents?.map { it.id } ?: emptyList()
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                // Fetch user objects for all friend IDs
                firestore.collection("users")
                    .whereIn("userId", friendIds)
                    .get()
                    .addOnSuccessListener { usersSnapshot ->
                        val friends = usersSnapshot.toObjects(User::class.java)
                        trySend(friends)
                    }
            }
        awaitClose { listener.remove() }
    }

    override fun getIncomingRequests(userId: String): Flow<List<FriendRequest>> = callbackFlow {
        val listener = firestore.collection("friend_requests")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val requests = snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendRequest(sender: User, receiverUid: String): Result<Unit> {
        return try {
            // Find receiver by UID
            val query = firestore.collection("users")
                .whereEqualTo("uid", receiverUid)
                .get()
                .await()
            
            val receiverDoc = query.documents.firstOrNull() ?: return Result.failure(Exception("User not found"))
            val receiverId = receiverDoc.id
            
            if (receiverId == sender.userId) return Result.failure(Exception("Cannot add yourself"))

            val request = FriendRequest(
                requestId = firestore.collection("friend_requests").document().id,
                senderId = sender.userId,
                senderName = sender.name,
                senderUid = sender.uid,
                receiverId = receiverId,
                status = "PENDING"
            )
            
            firestore.collection("friend_requests").document(request.requestId).set(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(request: FriendRequest, receiver: User): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // Add to both users' friends collection
            val senderFriendRef = firestore.collection("users").document(request.senderId).collection("friends").document(receiver.userId)
            val receiverFriendRef = firestore.collection("users").document(receiver.userId).collection("friends").document(request.senderId)
            
            batch.set(senderFriendRef, mapOf("addedAt" to FieldValue.serverTimestamp()))
            batch.set(receiverFriendRef, mapOf("addedAt" to FieldValue.serverTimestamp()))
            
            // Update request status
            val requestRef = firestore.collection("friend_requests").document(request.requestId)
            batch.update(requestRef, "status", "ACCEPTED")
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectRequest(requestId: String): Result<Unit> {
        return try {
            firestore.collection("friend_requests").document(requestId).update("status", "REJECTED").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(userId: String, friendId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            batch.delete(firestore.collection("users").document(userId).collection("friends").document(friendId))
            batch.delete(firestore.collection("users").document(friendId).collection("friends").document(userId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

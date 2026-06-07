package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.FriendRequest
import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun getFriends(userId: String): Flow<List<User>>
    fun getIncomingRequests(userId: String): Flow<List<FriendRequest>>
    suspend fun sendRequest(sender: User, receiverUid: String): Result<Unit>
    suspend fun acceptRequest(request: FriendRequest, receiver: User): Result<Unit>
    suspend fun rejectRequest(requestId: String): Result<Unit>
    suspend fun removeFriend(userId: String, friendId: String): Result<Unit>
}

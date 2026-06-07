package com.example.kiskibreakkab.domain.model

data class FriendRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUid: String = "",
    val receiverId: String = "",
    val status: String = "PENDING" // PENDING, ACCEPTED, REJECTED
)

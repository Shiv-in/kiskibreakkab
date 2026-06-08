package com.example.kiskibreakkab.domain.model

data class Room(
    val roomId: String = "",
    val roomName: String = "",
    val buildingName: String = "", // e.g., "Block 1", "M-Block"
    val blockCode: String = "",     // e.g., "B1", "M"
    val floor: Int = 0,
    val isAvailable: Boolean = true,
    val day: String = "",           // Added for slot-specific queries
    val slotNumber: Int = 0,        // Added for slot-specific queries
    val claimedBy: String? = null,  // Added to track who has booked the room
    val occupantNames: List<String> = emptyList(), // List of names currently in the room
    val lastUpdated: Long = 0L
)

package com.example.kiskibreakkab.data.remote.admin

import com.example.kiskibreakkab.domain.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * UTILITY SCRIPT: Use this to populate your university's room database.
 * This should be triggered once during setup or via an admin-only feature.
 */
class MasterDataLoader @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun populateSampleUniversityData(): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val roomsRef = firestore.collection("rooms")

            // Define your Buildings and Rooms here
            val universityData = listOf(
                RoomData("M-Block", "M", "CSE", listOf("M-713", "M-714", "M-306")),
                RoomData("B-Block", "B1", "ME", listOf("B-101", "B-102", "B-205")),
                RoomData("Library", "LIB", "GEN", listOf("Reading Room 1", "Digital Lab"))
            )

            val days = listOf("MON", "TUE", "WED", "THU", "FRI")
            val slots = 1..8

            universityData.forEach { building ->
                building.roomNames.forEach { name ->
                    // For each physical room, we create an availability entry for every slot
                    // This allows for granular "Set Difference" queries
                    days.forEach { day ->
                        slots.forEach { slot ->
                            val roomId = "${building.blockCode}_${name}_${day}_S${slot}".replace(" ", "_")
                            val room = Room(
                                roomId = roomId,
                                roomName = name,
                                buildingName = building.name,
                                blockCode = building.blockCode,
                                department = building.department,
                                floor = name.filter { it.isDigit() }.take(1).toIntOrNull() ?: 1,
                                day = day,
                                slotNumber = slot,
                                isAvailable = true // Default to free, master timetable will override
                            )
                            batch.set(roomsRef.document(roomId), room)
                        }
                    }
                }
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class RoomData(
        val name: String,
        val blockCode: String,
        val department: String,
        val roomNames: List<String>
    )
}

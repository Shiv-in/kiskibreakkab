package com.example.kiskibreakkab.data.local

import com.example.kiskibreakkab.domain.model.Room

/**
 * Utility to store room data extracted from Building PDFs.
 * You can add your data here to customize free rooms per building/department.
 */
object RoomDataProvider {

    val buildingRooms = mapOf(
        "B1" to listOf(
            Room(roomId = "b1_101", roomName = "101", blockCode = "B1"),
            Room(roomId = "b1_102", roomName = "102", blockCode = "B1")
        ),
        "B6" to listOf(
            Room(roomId = "b6_601", roomName = "601", blockCode = "B6"),
            Room(roomId = "b6_610", roomName = "610", blockCode = "B6")
        )
    )

    fun getAllRooms(): List<Room> {
        return buildingRooms.values.flatten()
    }
}

package com.example.kiskibreakkab.core.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Timetable : Screen("timetable")
    object Friends : Screen("friends")
    object Groups : Screen("groups")
    object RoomFinder : Screen("roomfinder")
    object Profile : Screen("profile")
    object FriendSchedule : Screen("friend_schedule/{friendId}") {
        fun createRoute(friendId: String) = "friend_schedule/$friendId"
    }
}

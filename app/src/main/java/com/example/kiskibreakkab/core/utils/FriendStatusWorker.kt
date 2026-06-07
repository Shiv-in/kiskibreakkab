package com.example.kiskibreakkab.core.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendStatusWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return Result.failure()

        val currentDay = TimeUtils.getCurrentDay()
        val currentSlot = TimeUtils.getCurrentSlot() ?: return Result.success()

        try {
            // 1. Get Friends
            val friendsSnapshot = firestore.collection("users").document(currentUserId)
                .collection("friends")
                .get()
                .await()
            
            val friendIds = friendsSnapshot.documents.map { it.id }
            if (friendIds.isEmpty()) return Result.success()

            // 2. Check each friend's status for current day/slot
            friendIds.forEach { friendId ->
                val slotId = "${currentDay}_${currentSlot.slotNumber}"
                val slotDoc = firestore.collection("users").document(friendId)
                    .collection("timetable").document(slotId)
                    .get()
                    .await()

                val slot = slotDoc.toObject(TimetableSlot::class.java)
                if (slot != null && slot.isFree) {
                    val userDoc = firestore.collection("users").document(friendId).get().await()
                    val friendName = userDoc.getString("name") ?: "A friend"
                    
                    NotificationHelper.showNotification(
                        applicationContext,
                        "Tactical Opportunity!",
                        "$friendName is FREE right now in Slot ${slot.slotNumber}."
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}

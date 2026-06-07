package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.data.local.dao.TimetableDao
import com.example.kiskibreakkab.data.local.entity.TimetableEntity
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.repository.TimetableRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val localDao: TimetableDao
) : TimetableRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private var isSyncing = false

    override fun getTimetable(userId: String): Flow<List<TimetableSlot>> {
        // Trigger background synchronization from cloud if not already syncing
        if (!isSyncing) {
            syncRemoteToLocal(userId)
        }
        
        // Expose local data as the single source of truth for the UI
        return localDao.getAllSlots().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun syncRemoteToLocal(userId: String) {
        isSyncing = true
        firestore.collection("users").document(userId)
            .collection("timetable")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isSyncing = false
                    return@addSnapshotListener
                }
                
                val remoteSlots = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TimetableSlot::class.java)
                } ?: emptyList()

                if (remoteSlots.isNotEmpty()) {
                    repositoryScope.launch {
                        localDao.insertAll(remoteSlots.map { it.toEntity() })
                    }
                }
            }
    }

    override suspend fun updateSlot(userId: String, slot: TimetableSlot): Result<Unit> {
        return try {
            // Update local first for instant UI response
            val entity = slot.toEntity()
            localDao.insertSlot(entity)
            
            // Sync to cloud
            val slotId = "${slot.day}_${slot.slotNumber}"
            firestore.collection("users").document(userId)
                .collection("timetable").document(slotId)
                .set(slot).await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            // In a production app, you might want to mark the entity as "sync_pending" 
            // if cloud update fails
            Result.failure(e)
        }
    }

    override suspend fun saveTimetable(userId: String, timetable: List<TimetableSlot>): Result<Unit> {
        return try {
            // 1. Local Batch Update
            localDao.insertAll(timetable.map { it.toEntity() })

            // 2. Cloud Batch Update
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(userId).collection("timetable")
            
            timetable.forEach { slot ->
                val slotId = "${slot.day}_${slot.slotNumber}"
                batch.set(userRef.document(slotId), slot)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mappers
    private fun TimetableSlot.toEntity() = TimetableEntity(
        day = day,
        slotNumber = slotNumber,
        startTime = startTime,
        endTime = endTime,
        isFree = isFree,
        lastUpdated = System.currentTimeMillis()
    )

    private fun TimetableEntity.toDomain() = TimetableSlot(
        day = day,
        slotNumber = slotNumber,
        startTime = startTime,
        endTime = endTime,
        isFree = isFree
    )
}

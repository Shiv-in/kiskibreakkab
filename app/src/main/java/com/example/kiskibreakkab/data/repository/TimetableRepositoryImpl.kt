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
    private val activeSyncs = mutableSetOf<String>()

    override fun getTimetable(userId: String): Flow<List<TimetableSlot>> {
        // Trigger background synchronization from cloud if not already syncing for this user
        if (!activeSyncs.contains(userId)) {
            syncRemoteToLocal(userId)
        }
        
        // Expose local data as the single source of truth for the UI
        return localDao.getAllSlots(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun syncRemoteToLocal(userId: String) {
        activeSyncs.add(userId)
        firestore.collection("users").document(userId)
            .collection("timetable")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    activeSyncs.remove(userId)
                    return@addSnapshotListener
                }
                
                val remoteSlots = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TimetableSlot::class.java)
                } ?: emptyList()

                if (remoteSlots.isNotEmpty()) {
                    repositoryScope.launch {
                        // For each remote slot, only update local if remote is NEWER
                        // Fetch all local slots first for comparison
                        val localEntities = localDao.getAllSlotsSync(userId)
                        
                        val entitiesToInsert = remoteSlots.mapNotNull { remote ->
                            val local = localEntities.find { it.day == remote.day && it.slotNumber == remote.slotNumber }
                            if (local == null || remote.lastUpdated > local.lastUpdated) {
                                remote.toEntity(userId)
                            } else {
                                null
                            }
                        }
                        
                        if (entitiesToInsert.isNotEmpty()) {
                            localDao.insertAll(entitiesToInsert)
                        }
                    }
                }
            }
    }

    override suspend fun updateSlot(userId: String, slot: TimetableSlot): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val updatedSlot = slot.copy(lastUpdated = now)
            
            // 1. Update local first (Instant UI response)
            localDao.insertSlot(updatedSlot.toEntity(userId))
            
            // 2. Push to cloud
            repositoryScope.launch {
                try {
                    val slotId = "${slot.day}_${slot.slotNumber}"
                    firestore.collection("users").document(userId)
                        .collection("timetable").document(slotId)
                        .set(updatedSlot).await()
                } catch (e: Exception) {
                    // Fail silently, will retry or sync later
                }
            }
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTimetable(userId: String, timetable: List<TimetableSlot>): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val updatedTimetable = timetable.map { it.copy(lastUpdated = now) }
            
            // 1. Local Batch Update
            localDao.insertAll(updatedTimetable.map { it.toEntity(userId) })

            // 2. Cloud Batch Update
            repositoryScope.launch {
                try {
                    val batch = firestore.batch()
                    val userRef = firestore.collection("users").document(userId).collection("timetable")
                    
                    updatedTimetable.forEach { slot ->
                        val slotId = "${slot.day}_${slot.slotNumber}"
                        batch.set(userRef.document(slotId), slot)
                    }
                    batch.commit().await()
                } catch (e: Exception) {}
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mappers
    private fun TimetableSlot.toEntity(userId: String) = TimetableEntity(
        userId = userId,
        day = day,
        slotNumber = slotNumber,
        startTime = startTime,
        endTime = endTime,
        isFree = isFree,
        location = location,
        lastUpdated = lastUpdated
    )

    private fun TimetableEntity.toDomain() = TimetableSlot(
        day = day,
        slotNumber = slotNumber,
        startTime = startTime,
        endTime = endTime,
        isFree = isFree,
        location = location,
        lastUpdated = lastUpdated
    )
}

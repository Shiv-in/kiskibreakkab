package com.example.kiskibreakkab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.ProfileRepository
import com.example.kiskibreakkab.domain.repository.RoomRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val roomRepository: RoomRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    val isAdmin = _uiState.map { it.user?.uid?.lowercase() == "23ics10005" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    _uiState.update { it.copy(user = user) }
                }
            }
        }
    }

    fun updateProfile(name: String, section: String, labGroup: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updatedUser = currentUser.copy(name = name, section = section, labGroup = labGroup)
            val result = profileRepository.updateProfile(updatedUser)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Profile updated!") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Update failed") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun clearAllRooms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, importProgress = "Purging all campus data...") }
            val result = roomRepository.clearAllRooms()
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, importProgress = null, successMessage = "System database wiped. Ready for fresh JSON.") }
            } else {
                _uiState.update { it.copy(isLoading = false, importProgress = null, error = "Purge failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun importRoomsFromJson(blockCode: String, jsonText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, error = null, importProgress = "Initializing deployment...") }
            try {
                val jsonObj = JSONObject(jsonText)
                val trimmedBlock = blockCode.trim().uppercase()
                
                _uiState.update { it.copy(importProgress = "Purging stale records for $trimmedBlock...") }
                
                // 1. Full Purge for this building to avoid stale rooms
                val oldRooms = firestore.collection("rooms")
                    .whereEqualTo("blockCode", trimmedBlock)
                    .get()
                    .await()
                
                if (!oldRooms.isEmpty) {
                    val documents = oldRooms.documents
                    documents.chunked(500).forEach { chunk ->
                        val deleteBatch = firestore.batch()
                        chunk.forEach { deleteBatch.delete(it.reference) }
                        deleteBatch.commit().await()
                    }
                }

                val daysMap = mapOf(
                    "Mon" to "MON", "Tu" to "TUE", "We" to "WED", 
                    "Th" to "THU", "Fr" to "FRI", "Sa" to "SAT", "Sat" to "SAT"
                )
                
                val roomsRef = firestore.collection("rooms")
                var totalOperations = 0
                var currentBatch = firestore.batch()
                var batchSize = 0
                
                _uiState.update { it.copy(importProgress = "Processing JSON schedule map...") }
                
                val roomNames = jsonObj.keys()
                while(roomNames.hasNext()) {
                    val originalKey = roomNames.next()
                    var roomName = originalKey.trim()
                    
                    // Mark Library by building (e.g. B3 Library)
                    if (roomName.contains("Library", ignoreCase = true) || roomName.equals("Lib", ignoreCase = true)) {
                        roomName = "$trimmedBlock Library"
                    }
                    
                    val schedule = jsonObj.getJSONObject(originalKey)
                    
                    daysMap.forEach { (jsonDay, dbDay) ->
                        if (schedule.has(jsonDay)) {
                            val slotsArray = schedule.getJSONArray(jsonDay)
                            for (i in 0 until 8) {
                                val slotNumber = i + 1
                                // CRITICAL LOGIC: true = Class (Occupied), false = Free (Available)
                                val isOccupied = if (i < slotsArray.length()) {
                                    try { slotsArray.getBoolean(i) } catch (e: Exception) { true }
                                } else { true }
                                
                                val roomId = "${trimmedBlock}_${roomName.replace(" ", "_")}_${dbDay}_S$slotNumber"
                                    .lowercase()
                                
                                val room = Room(
                                    roomId = roomId,
                                    roomName = roomName,
                                    buildingName = "${trimmedBlock}-Block",
                                    blockCode = trimmedBlock,
                                    isAvailable = !isOccupied, // Store TRUE in DB only if FALSE in JSON
                                    day = dbDay,
                                    slotNumber = slotNumber,
                                    claimedBy = null,
                                    occupantNames = emptyList(),
                                    lastUpdated = System.currentTimeMillis()
                                )
                                
                                currentBatch.set(roomsRef.document(roomId), room)
                                batchSize++
                                totalOperations++

                                if (batchSize >= 500) {
                                    currentBatch.commit().await()
                                    currentBatch = firestore.batch()
                                    batchSize = 0
                                    _uiState.update { it.copy(importProgress = "Deployed $totalOperations units...") }
                                }
                            }
                        }
                    }
                }

                if (batchSize > 0) {
                    currentBatch.commit().await()
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        importProgress = null,
                        successMessage = "SUCCESS: $totalOperations entries deployed for $trimmedBlock."
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, importProgress = null, error = "IMPORT FAILED: ${e.message}") }
            }
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val importProgress: String? = null
)

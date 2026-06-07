package com.example.kiskibreakkab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
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

    fun importRooms(roomsText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Logic to parse text: Expecting "RoomName, Block, Department" per line
                val lines = roomsText.lines().filter { it.isNotBlank() }
                val batch = firestore.batch()
                val roomsRef = firestore.collection("rooms")
                
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        val roomName = parts[0].trim()
                        val blockCode = parts[1].trim()
                        val department = parts[2].trim()
                        
                        val room = Room(
                            roomId = "${blockCode}_${roomName}".lowercase(),
                            roomName = roomName,
                            blockCode = blockCode,
                            department = department,
                            isAvailable = true
                        )
                        batch.set(roomsRef.document(room.roomId), room)
                    }
                }
                batch.commit().await()
                _uiState.update { it.copy(isLoading = false, successMessage = "Deployed ${lines.size} units without capacity logic.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Import failed: ${e.message}") }
            }
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

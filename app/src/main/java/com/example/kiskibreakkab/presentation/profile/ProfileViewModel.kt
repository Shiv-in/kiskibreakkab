package com.example.kiskibreakkab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.example.kiskibreakkab.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
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
            processRoomsText(roomsText)
        }
    }

    fun importRoomsFromPdf(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val document = PDDocument.load(inputStream)
                    val stripper = PDFTextStripper()
                    val text = stripper.getText(document)
                    document.close()
                    processRoomsText(text)
                } ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to open PDF") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "PDF Import failed: ${e.message}") }
            }
        }
    }

    private suspend fun processRoomsText(roomsText: String) {
        _uiState.update { it.copy(isLoading = true, successMessage = null, error = null, importProgress = "Analyzing document content...") }
        try {
            // Log the start for debugging purposes if possible, but here we will adjust logic
            // Many PDFs might not use commas. We'll try to support spaces or tabs as well
            val lines = roomsText.lines()
                .filter { it.isNotBlank() }
                .distinct()
            
            if (lines.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "No readable text found in document.") }
                return
            }

            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")
            val slots = 1..8
            val roomsRef = firestore.collection("rooms")
            
            var totalOperations = 0
            var currentBatch = firestore.batch()
            var batchSize = 0
            var validRoomsFound = 0
            
            lines.forEach { line ->
                // Attempt to split by comma first, then fallback to whitespace for PDF tables
                var parts = line.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                if (parts.size < 3) {
                    // Fallback: split by multiple spaces (common in PDF text extraction for tables)
                    parts = line.split(Regex("\\s{2,}")).map { it.trim() }.filter { it.isNotEmpty() }
                }

                if (parts.size >= 3) {
                    val roomName = parts[0]
                    val blockCode = parts[1]
                    val department = parts[2]
                    validRoomsFound++

                    days.forEach { day ->
                        slots.forEach { slot ->
                            val roomId = "${blockCode}_${roomName}_${day}_S$slot"
                                .replace(" ", "_")
                                .lowercase()
                            
                            val room = Room(
                                roomId = roomId,
                                roomName = roomName,
                                buildingName = "${blockCode}-Block",
                                blockCode = blockCode,
                                department = department,
                                isAvailable = true,
                                day = day,
                                slotNumber = slot
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
            
            if (validRoomsFound == 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Parsing failed: Found ${lines.size} lines but none matched 'Name, Block, Dept' format. Check document structure."
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        importProgress = null,
                        successMessage = "Successfully deployed $totalOperations units from $validRoomsFound rooms discovered."
                    ) 
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, importProgress = null, error = "Import failed: ${e.message}") }
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

package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(userId: String): Flow<User?>
    suspend fun updateProfile(user: User): Result<Unit>
}

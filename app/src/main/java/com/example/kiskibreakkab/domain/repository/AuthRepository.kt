package com.example.kiskibreakkab.domain.repository

import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(uid: String, password: String): Result<Unit>
    suspend fun register(user: User, password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun logout()
    suspend fun deleteAccount(): Result<Unit>
    fun isUserLoggedIn(): Boolean
}

package com.example.kiskibreakkab.data.repository

import com.example.kiskibreakkab.data.local.dao.UserDao
import com.example.kiskibreakkab.data.local.entity.UserEntity
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : ProfileRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getUserProfile(userId: String): Flow<User?> {
        // Background sync from remote to local
        syncProfileToLocal(userId)
        
        // Return local flow
        return userDao.getUser(userId).map { it?.toDomain() }
    }

    private fun syncProfileToLocal(userId: String) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    repositoryScope.launch {
                        userDao.insertUser(user.toEntity())
                    }
                }
            }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            // Update local
            userDao.insertUser(user.toEntity())
            
            // Sync to remote
            firestore.collection("users").document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mappers
    private fun User.toEntity() = UserEntity(
        userId = userId,
        uid = uid,
        name = name,
        email = email,
        section = section,
        labGroup = labGroup
    )

    private fun UserEntity.toDomain() = User(
        userId = userId,
        uid = uid,
        name = name,
        email = email,
        section = section,
        labGroup = labGroup
    )
}

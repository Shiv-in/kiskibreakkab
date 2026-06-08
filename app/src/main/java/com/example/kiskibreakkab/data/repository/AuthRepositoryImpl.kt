package com.example.kiskibreakkab.data.repository

import android.content.Context
import com.example.kiskibreakkab.data.local.dao.UserDao
import com.example.kiskibreakkab.data.local.entity.UserEntity
import com.example.kiskibreakkab.domain.model.User
import com.example.kiskibreakkab.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Check session age
                val lastLogin = prefs.getLong("last_login_ts", 0)
                val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
                
                if (lastLogin == 0L) {
                    // Initialize last_login_ts for users who were already logged in
                    prefs.edit().putLong("last_login_ts", System.currentTimeMillis()).apply()
                } else if (System.currentTimeMillis() - lastLogin > oneWeekMillis) {
                    auth.signOut()
                    trySend(null)
                    return@AuthStateListener
                }

                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        val user = snapshot?.toObject(User::class.java)
                        if (user != null) {
                            repositoryScope.launch {
                                userDao.insertUser(user.toEntity())
                            }
                        }
                        trySend(user)
                    }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun User.toEntity() = UserEntity(
        userId = userId,
        uid = uid,
        name = name,
        email = email,
        section = section,
        labGroup = labGroup
    )

    override suspend fun login(uid: String, password: String): Result<Unit> {
        return try {
            val normalizedUid = uid.trim().lowercase()
            val email = if (normalizedUid.contains("@")) {
                normalizedUid
            } else {
                val query = firestore.collection("users")
                    .whereEqualTo("uid", normalizedUid)
                    .get()
                    .await()
                
                query.documents.firstOrNull()?.getString("email") 
                    ?: return Result.failure(Exception("User ID not found in system."))
            }
            
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            prefs.edit().putLong("last_login_ts", System.currentTimeMillis()).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            val message = when(e) {
                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Account has been purged from system."
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Authentication failed: Check your passkey."
                else -> "System Error: ${e.localizedMessage}"
            }
            Result.failure(Exception(message))
        }
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val normalizedEmail = user.email.trim().lowercase()
            val normalizedUid = user.uid.trim().lowercase()

            // Check if UID is already taken
            val uidCheck = firestore.collection("users")
                .whereEqualTo("uid", normalizedUid)
                .get()
                .await()
            
            if (!uidCheck.isEmpty) {
                return Result.failure(Exception("This College UID is already registered."))
            }
            
            val authResult = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Registration failed"))
            
            // Send verification email
            firebaseUser.sendEmailVerification().await()
            
            val userId = firebaseUser.uid
            val newUser = user.copy(
                userId = userId,
                email = normalizedEmail,
                uid = normalizedUid
            )
            firestore.collection("users").document(userId).set(newUser).await()
            prefs.edit().putLong("last_login_ts", System.currentTimeMillis()).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
        prefs.edit().remove("last_login_ts").apply()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val userId = user.uid
            
            // Delete from Firestore first
            firestore.collection("users").document(userId).delete().await()
            
            // Delete from Firebase Auth
            user.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

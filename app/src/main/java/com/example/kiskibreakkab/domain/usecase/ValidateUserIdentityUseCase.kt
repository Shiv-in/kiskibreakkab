package com.example.kiskibreakkab.domain.usecase

import javax.inject.Inject

class ValidateUserIdentityUseCase @Inject constructor() {
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    operator fun invoke(uid: String, email: String): ValidationResult {
        val uidClean = uid.trim().lowercase()
        val emailClean = email.trim().lowercase()
        val emailPrefix = emailClean.substringBefore("@")
        val domain = emailClean.substringAfter("@")

        if (uidClean.isEmpty() || emailClean.isEmpty()) {
            return ValidationResult.Error("Tactical ID and Email required.")
        }

        if (domain != "cuchd.in") {
            return ValidationResult.Error("Use official @cuchd.in email.")
        }

        if (uidClean != emailPrefix) {
            return ValidationResult.Error("Identity Mismatch: UID must match Email prefix (e.g. $uidClean@cuchd.in)")
        }

        return ValidationResult.Success
    }
}

package com.psyfen.domain.use_cases

import android.app.Activity
import com.psyfen.common.Resource
import com.psyfen.domain.model.User
import com.psyfen.domain.respository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendVerificationCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, activity: Activity): Resource<String> {
        // Validate phone number format
        val cleanNumber = phoneNumber.replace(Regex("[^+0-9]"), "")

        if (cleanNumber.isBlank()) {
            return Resource.Failure(Exception("Phone number cannot be empty"))
        }

        if (!cleanNumber.startsWith("+")) {
            return Resource.Failure(Exception("Phone number must include country code (e.g., +91)"))
        }

        if (cleanNumber.length < 10) {
            return Resource.Failure(Exception("Invalid phone number"))
        }

        return repository.sendVerificationCode(cleanNumber, activity)
    }
}

class VerifyCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(verificationId: String, code: String): Resource<User> {
        if (verificationId.isBlank()) {
            return Resource.Failure(Exception("Verification ID is missing"))
        }

        if (code.isBlank()) {
            return Resource.Failure(Exception("Verification code cannot be empty"))
        }

        if (code.length != 6) {
            return Resource.Failure(Exception("Verification code must be 6 digits"))
        }

        return repository.verifyCode(verificationId, code)
    }
}

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.signOut()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getCurrentUser()
    }
}

class IsUserLoggedInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isUserLoggedIn()
    }
}
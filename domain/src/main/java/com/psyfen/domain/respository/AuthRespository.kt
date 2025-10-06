package com.psyfen.domain.respository

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import com.psyfen.common.Resource
import com.psyfen.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ): Resource<String> // Returns verification ID

     suspend fun verifyCode(
        verificationId: String,
        code: String
    ): Resource<User>

     suspend fun signOut(): Resource<Unit>

     fun getCurrentUser(): Flow<User?>

     suspend fun isUserLoggedIn(): Boolean

     fun getCurrentUserSync(): User?
}
package com.psyfen.data.repository

import android.app.Activity
import android.content.Context
import android.media.ResourceBusyException
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import com.google.android.gms.common.api.Response
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.psyfen.common.Resource
import com.psyfen.common.firebase.await
import com.psyfen.domain.model.User
import com.psyfen.domain.respository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth : FirebaseAuth,
    private val firestore: FirebaseFirestore
):AuthRepository {


    override suspend fun sendVerificationCode(
          phoneNumber: String,
          activity: Activity
      ): Resource<String> = suspendCancellableCoroutine  {continuation->

          try {
              val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                  override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                   }

                  override fun onVerificationFailed(p0: FirebaseException) {
                      if(continuation.isActive){
                          continuation.resume(Resource.Failure(p0))
                      }
                  }

                  override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                      if(continuation.isActive){
                          continuation.resume(Resource.Success(p0))
                      }
                  }
              }
              val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                  .setPhoneNumber(phoneNumber)
                  .setActivity(activity)
                  .setTimeout(60L,TimeUnit.SECONDS)
                  .setCallbacks(callback)
                  .build()

             PhoneAuthProvider.verifyPhoneNumber(options)

          }catch (e:Exception){
              if(continuation.isActive){
                  continuation.resume((Resource.Failure(e)))
              }
          }

      }

      override suspend fun verifyCode(verificationId: String, code: String): Resource<User> {
          return try{
              val credential = PhoneAuthProvider.getCredential(verificationId,code)
              val authResult = firebaseAuth.signInWithCredential(credential).await()
              val firebaseUser = authResult.user
                  ?:return Resource.Failure(Exception("Authentication Failed"))

               val userDoc = firestore.collection(Companion.COLLECTION_USERS)
                  .document(firebaseUser.uid)
                  .get()
                  .await()

              val user = if (userDoc.exists()) {
                  // Existing user
                  userDoc.toObject(User::class.java)?.apply {
                      uid = firebaseUser.uid
                  } ?: return Resource.Failure(Exception("Failed to parse user data"))
              } else {
                  // New user - create profile
                  val newUser = User(
                      uid = firebaseUser.uid,
                      phoneNumber = firebaseUser.phoneNumber,
                      displayName = "User_${firebaseUser.phoneNumber?.takeLast(4)}",
                      createdAt = System.currentTimeMillis()
                  )

                  firestore.collection(Companion.COLLECTION_USERS)
                      .document(firebaseUser.uid)
                      .set(newUser)
                      .await()

                  newUser
              }

              Resource.Success(user)


          }catch (e:Exception){
              e.printStackTrace()
              Resource.Failure(e)
          }
      }

      override suspend fun signOut(): Resource<Unit> {
          return try {
              firebaseAuth.signOut()
              Resource.Success(Unit)
          } catch (e: Exception) {
              Resource.Failure(e)
          }
      }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
                return@AuthStateListener
            }

            // Listen to user document changes
            val userListener = firestore.collection(Companion.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)?.apply {
                            uid = snapshot.id
                        }
                        trySend(user)
                    } else {
                        trySend(null)
                    }
                }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }


      override suspend fun isUserLoggedIn(): Boolean {
          return firebaseAuth.currentUser != null
      }

      override fun getCurrentUserSync(): User? {
          val firebaseUser = firebaseAuth.currentUser ?: return null
          return User(
              uid = firebaseUser.uid,
              phoneNumber = firebaseUser.phoneNumber,
              displayName = firebaseUser.displayName
          )
      }

    companion object {
        private const val COLLECTION_USERS = "users"
    }


}
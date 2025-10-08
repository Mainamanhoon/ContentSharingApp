package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.psyfen.common.AppViewModel
import com.psyfen.common.Resource
import com.psyfen.domain.model.AuthState
import com.psyfen.domain.use_cases.GetCurrentUserUseCase
import com.psyfen.domain.use_cases.IsUserLoggedInUseCase
import com.psyfen.domain.use_cases.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    @ApplicationContext private val context: Context
) : AppViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    init {
        checkAuthStatus()
        observeCurrentUser()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val isLoggedIn = isUserLoggedInUseCase()
                Log.d("AuthViewModel", "Initial auth check: isLoggedIn=$isLoggedIn")
                _authState.value = _authState.value.copy(
                    isAuthenticated = isLoggedIn,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking auth status", e)
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { user ->
                    Log.d("AuthViewModel", "User state changed: ${user?.uid}")
                    _authState.value = _authState.value.copy(
                        isAuthenticated = user != null,
                        user = user,
                        isLoading = false
                    )

                    // Save user info to SharedPreferences
                    if (user != null) {
                        saveUserToPrefs(user)
                    } else {
                        clearUserFromPrefs()
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error observing user", e)
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    user = null,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "Logout initiated")

        viewModelScope.launch {
            try {
                // Set loading state
                _authState.value = _authState.value.copy(isLoading = true)

                // Sign out from Firebase
                when (val result = signOutUseCase()) {
                    is Resource.Success -> {
                        Log.d("AuthViewModel", "Logout successful")
                        // Clear user data
                        clearUserFromPrefs()
                        // Update state
                        _authState.value = AuthState(
                            isAuthenticated = false,
                            user = null,
                            isLoading = false
                        )
                    }
                    is Resource.Failure -> {
                        Log.e("AuthViewModel", "Logout failed: ${result.exception.message}")
                        // Even if logout fails, clear local state
                        clearUserFromPrefs()
                        _authState.value = AuthState(
                            isAuthenticated = false,
                            user = null,
                            isLoading = false,
                            error = "Logout failed: ${result.exception.message}"
                        )
                    }
                    is Resource.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during logout", e)
                // Clear local state even on exception
                clearUserFromPrefs()
                _authState.value = AuthState(
                    isAuthenticated = false,
                    user = null,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun saveUserToPrefs(user: com.psyfen.domain.model.User) {
        try {
            prefs.edit().apply {
                putString("user_id", user.uid)
                putString("username", user.username)
                putString("phone_number", user.phoneNumber)
                apply()
            }
            Log.d("AuthViewModel", "User saved to prefs: ${user.uid}")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving user to prefs", e)
        }
    }

    private fun clearUserFromPrefs() {
        try {
            prefs.edit().clear().apply()
            Log.d("AuthViewModel", "User cleared from prefs")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error clearing prefs", e)
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
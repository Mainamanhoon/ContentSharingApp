package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.psyfen.common.AppViewModel
import com.psyfen.domain.model.AuthState
import com.psyfen.domain.model.User
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
            val isLoggedIn = isUserLoggedInUseCase()
            _authState.value = _authState.value.copy(isAuthenticated = isLoggedIn)
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    user = user
                )

                // Save user info to SharedPreferences for file management
                user?.let { saveUserToPrefs(it) }
            }
        }
    }

    fun logout() {
        launchCatching {
            when (signOutUseCase()) {
                is com.psyfen.common.Resource.Success -> {
                    clearUserFromPrefs()
                    _authState.value = AuthState()
                }
                is com.psyfen.common.Resource.Failure -> {
                    _authState.value = _authState.value.copy(
                        error = "Failed to logout"
                    )
                }
                is com.psyfen.common.Resource.Loading -> Unit
            }
        }
    }

    private fun saveUserToPrefs(user: User) {
        prefs.edit().apply {
            putString("user_id", user.uid)
            putString("username", user.username)
            putString("phone_number", user.phoneNumber)
            apply()
        }
    }

    private fun clearUserFromPrefs() {
        prefs.edit().clear().apply()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
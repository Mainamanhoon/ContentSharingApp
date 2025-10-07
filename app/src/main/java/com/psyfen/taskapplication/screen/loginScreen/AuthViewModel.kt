package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen

import androidx.lifecycle.viewModelScope
import com.psyfen.common.Resource
import com.psyfen.common.AppViewModel
import com.psyfen.domain.model.AuthState
import com.psyfen.domain.use_cases.GetCurrentUserUseCase
import com.psyfen.domain.use_cases.LoginUseCase
import com.psyfen.domain.use_cases.LogoutUseCase
import com.psyfen.domain.use_cases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : AppViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    user = user
                )
            }
        }
    }

    fun login(username: String, password: String) {
        launchCatching {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase(username, password)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = result.result,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Failure -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is Resource.Loading -> {
                    // Already set loading
                }
            }
        }
    }

    fun register(username: String, phoneNumber: String, password: String) {
        launchCatching {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = registerUseCase(username, phoneNumber, password)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = result.result,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Failure -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is Resource.Loading -> {
                    // Already set loading
                }
            }
        }
    }

    fun logout() {
        launchCatching {
            when (logoutUseCase()) {
                is Resource.Success -> {
                    _authState.value = AuthState()
                }
                is Resource.Failure -> {
                    _authState.value = _authState.value.copy(
                        error = "Failed to logout"
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
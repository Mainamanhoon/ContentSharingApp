package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen


import android.app.Activity
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.psyfen.common.AppViewModel
import com.psyfen.common.Resource
import com.psyfen.domain.model.User
import com.psyfen.domain.use_cases.SendVerificationCodeUseCase
import com.psyfen.domain.use_cases.VerifyCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class CodeSent(val verificationId: String) : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val verifyCodeUseCase: VerifyCodeUseCase,
    @ApplicationContext private val context: Context
) : AppViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    private var currentVerificationId: String? = null

    fun onPhoneNumberChange(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    fun onVerificationCodeChange(newCode: String) {
        if (newCode.all { it.isDigit() } && newCode.length <= 6) {
            _verificationCode.value = newCode
        }
    }

    fun sendVerificationCode(activity: Activity) {
        if (_phoneNumber.value.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = sendVerificationCodeUseCase(_phoneNumber.value, activity)) {
                is Resource.Success -> {
                    val data = result.result
                    if (data.length == 6 && data.all { it.isDigit() }) {
                        onVerificationCodeChange(data)
                        verifyCode()
                    } else {
                        currentVerificationId = data
                        _uiState.value = LoginUiState.CodeSent(data)
                    }
                }
                is Resource.Failure -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Failed to send verification code"
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun verifyCode() {
        val verificationId = currentVerificationId
        if (verificationId == null) {
            _uiState.value = LoginUiState.Error("Please request verification code first")
            return
        }

        if (_verificationCode.value.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter verification code")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = verifyCodeUseCase(verificationId, _verificationCode.value)) {
                is Resource.Success -> {
                    // Save user to SharedPreferences for file management
                    saveUserToPrefs(result.result)
                    _uiState.value = LoginUiState.Success(result.result)
                }
                is Resource.Failure -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Verification failed"
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun saveUserToPrefs(user: User) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_id", user.uid)
            putString("username", user.username)
            putString("phone_number", user.phoneNumber)
            apply()
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Initial
        _phoneNumber.value = ""
        _verificationCode.value = ""
        currentVerificationId = null
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = if (currentVerificationId != null) {
                LoginUiState.CodeSent(currentVerificationId!!)
            } else {
                LoginUiState.Initial
            }
        }
    }
}
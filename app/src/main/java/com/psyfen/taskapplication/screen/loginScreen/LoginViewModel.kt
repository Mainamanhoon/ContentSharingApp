package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.psyfen.common.AppViewModel
import com.psyfen.common.Resource
import com.psyfen.domain.model.User
import com.psyfen.domain.use_cases.SendVerificationCodeUseCase
import com.psyfen.domain.use_cases.VerifyCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val verifyCodeUseCase: VerifyCodeUseCase
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
                    currentVerificationId = result.result
                    _uiState.value = LoginUiState.CodeSent(result.result)
                }
                is Resource.Failure -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Failed to send verification code"
                    )
                }
                is Resource.Loading -> {
                    // Already set above
                }
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
                    _uiState.value = LoginUiState.Success(result.result)
                }
                is Resource.Failure -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Verification failed"
                    )
                }
                is Resource.Loading -> {
                    // Already set above
                }
            }
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

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class CodeSent(val verificationId: String) : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

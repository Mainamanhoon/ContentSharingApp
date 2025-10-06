package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen


import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val activity = LocalContext.current as ComponentActivity

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1c213c))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFfd511e)
                )

                Text(
                    text = "Phone Verification",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (uiState is LoginUiState.CodeSent) {
                        "Enter the 6-digit code sent to\n$phoneNumber"
                    } else {
                        "Enter your phone number to continue"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFFC6CBC5),
                    textAlign = TextAlign.Center
                )

                // Show either phone input or OTP input based on state
                when (val state = uiState) {
                    is LoginUiState.Initial, is LoginUiState.Error -> {
                        PhoneNumberInput(
                            phoneNumber = phoneNumber,
                            onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
                            onSendCode = { viewModel.sendVerificationCode(activity) },
                            isLoading = false
                        )
                    }
                    is LoginUiState.Loading -> {
                        if (verificationCode.isEmpty()) {
                            PhoneNumberInput(
                                phoneNumber = phoneNumber,
                                onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
                                onSendCode = { viewModel.sendVerificationCode(activity) },
                                isLoading = true
                            )
                        } else {
                            OtpInput(
                                verificationCode = verificationCode,
                                onCodeChange = { viewModel.onVerificationCodeChange(it) },
                                onVerify = { viewModel.verifyCode() },
                                onResend = { viewModel.sendVerificationCode(activity) },
                                isLoading = true
                            )
                        }
                    }
                    is LoginUiState.CodeSent -> {
                        OtpInput(
                            verificationCode = verificationCode,
                            onCodeChange = { viewModel.onVerificationCodeChange(it) },
                            onVerify = { viewModel.verifyCode() },
                            onResend = { viewModel.sendVerificationCode(activity) },
                            isLoading = false
                        )
                    }
                    is LoginUiState.Success -> {
                        // Will trigger navigation
                    }
                }

                // Error message
                if (uiState is LoginUiState.Error) {
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        color = Color(0xFFFF3333),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color(0xFFfd511e))
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendCode: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            placeholder = { Text("+91 98765 43210") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFfd511e),
                unfocusedBorderColor = Color(0xFFC6CBC5),
                focusedLabelColor = Color(0xFFfd511e),
                unfocusedLabelColor = Color(0xFFC6CBC5),
                cursorColor = Color(0xFFfd511e)
            )
        )

        Text(
            text = "Please include country code (e.g., +91 for India)",
            fontSize = 12.sp,
            color = Color(0xFFC6CBC5)
        )

        Button(
            onClick = onSendCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && phoneNumber.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFfd511e)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpInput(
    verificationCode: String,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = verificationCode,
            onValueChange = onCodeChange,
            label = { Text("Verification Code") },
            placeholder = { Text("123456") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFfd511e),
                unfocusedBorderColor = Color(0xFFC6CBC5),
                focusedLabelColor = Color(0xFFfd511e),
                unfocusedLabelColor = Color(0xFFC6CBC5),
                cursorColor = Color(0xFFfd511e)
            )
        )

        Button(
            onClick = onVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && verificationCode.length == 6,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFfd511e)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verify",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = onResend,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                text = "Resend Code",
                color = if (isLoading) Color(0xFF666666) else Color(0xFFfd511e)
            )
        }
    }
}
package com.example.insightsapp.ui.onboarding

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    OtpVerificationScreen(
        phoneNumber = "+919876543210",
        onOtpVerified = { },
        onNavigateBack = {}
    )
}

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onOtpVerified: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OtpVerificationViewModel = viewModel() // ✅ No factory needed
) {
    val otpValue by viewModel.otpValue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    val databaseProvider = RemoteDatabaseProvider.getInstance(context)
    val authService = remember { AuthenticationService(databaseProvider) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.sendInitialOtp(phoneNumber, it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F8))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify your mobile number",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "We've sent a verification code to",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Text(
            text = phoneNumber,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OtpInputFields(
            otpValue = otpValue,
            onOtpChange = viewModel::onOtpChange
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (resendTimer > 0) {
            Text(
                text = "Resend in ${String.format("%02d:%02d", resendTimer / 60, resendTimer % 60)}",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
        } else {
            TextButton(
                onClick = {
                    activity?.let { viewModel.resendOtp(phoneNumber, it) }
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Resend OTP",
                    fontSize = 16.sp,
                    color = Color(0xFF57C6A9),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // ✅ Fixed Button with all database operations
        Button(
            onClick = {
                println("🔘 Verify button clicked")
                viewModel.verifyOtp(phoneNumber, otpValue) { success ->
                    println("📞 OTP verification callback received: $success")

                    if (success) {
                        println("✅ OTP verification successful, creating user and checking status...")

                        scope.launch {
                            try {
                                // ✅ Use AuthenticationService directly
                                val authResult = authService.checkUserStatus(phoneNumber)

                                if (authResult.user == null) {
                                    // Create new user and store ID
                                    val newUser = authService.createUser(phoneNumber)
                                    println("✅ New user created: ${newUser.phoneNumber}")

                                    // Navigate to onboarding
                                    onOtpVerified(false) // New user = false
                                } else {
                                    // Existing user - store ID and check completion
                                    println("✅ User exists: ${authResult.user.fullName}")

                                    // Navigate based on completion status
                                    onOtpVerified(authResult.shouldSkipOnboarding)
                                }

                            } catch (e: Exception) {
                                println("❌ Error handling user: ${e.message}")
                                e.printStackTrace()
                                // Default to new user flow if error
                                onOtpVerified(false)
                            }
                        }
                    } else {
                        println("❌ OTP verification failed")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = otpValue.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Verify",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OtpInputFields(
    otpValue: String,
    onOtpChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        repeat(6) { index ->
            val char = otpValue.getOrNull(index)?.toString() ?: ""

            BasicTextField(
                value = char,
                onValueChange = { newValue ->
                    if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                        val newOtp = otpValue.padEnd(6, ' ').toMutableList()

                        if (newValue.isEmpty()) {
                            newOtp[index] = ' '
                            if (index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        } else {
                            newOtp[index] = newValue[0]
                            if (index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            } else {
                                focusManager.clearFocus()
                            }
                        }

                        onOtpChange(newOtp.joinToString("").trim())
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .focusRequester(focusRequesters[index])
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = if (char.isNotEmpty()) 2.dp else 1.dp,
                        color = if (char.isNotEmpty()) Color(0xFF57C6A9) else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }
}

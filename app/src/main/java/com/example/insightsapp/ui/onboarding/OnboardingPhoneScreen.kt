package com.example.insightsapp.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextFieldDefaults
import com.example.insightsapp.R


@Preview(showBackground = true)
@Composable
fun MobileNumberEntryScreenPreview() {
    var phoneNumber by remember { mutableStateOf("9876543210") }
    val isValid = phoneNumber.length == 10

    MobileNumberEntryScreen(
        phoneNumber = phoneNumber,
        onPhoneNumberChange = { phoneNumber = it },
        onSendOtp = { /* noop */ },
        isValid = isValid
    )
}

@Composable
fun OnboardingPhoneScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onNavigateToOtp: (String) -> Unit // Assuming you'll add navigation for OTP
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isValid by viewModel.isValid.collectAsState() // Assuming isValid is a StateFlow in ViewModel

    MobileNumberEntryScreen(
        phoneNumber = phoneNumber,
        onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
        isValid = isValid,
        onSendOtp = {
            if (isValid) {
                // Here you would typically call a ViewModel function to send OTP
                // and then navigate.
                // For now, let's assume it directly navigates after validation.
                onNavigateToOtp(phoneNumber)
            }
        }
    )
}


@Composable
fun MobileNumberEntryScreen(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isValid: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center  // Center vertically
    ) {
        Text(
            text = "Enter your mobile number",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center  // Center text horizontally
        )
        Spacer(Modifier.height(36.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = Color(0xFFF6F8F8),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = "+91",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF57C6A9)
            )
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(10)
                    onPhoneNumberChange(filtered)
                },
                placeholder = {
                    Text(
                        "9876543210",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Normal
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.dialer),
                contentDescription = "Dialer",
                modifier = Modifier.size(24.dp).align(Alignment.CenterVertically)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onSendOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
        ) {
            Text(
                text = "Send OTP",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}



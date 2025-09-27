package com.example.insightsapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun BasicDetailsScreenPreview() {
    BasicDetailsScreen(
        phoneNumber = "+919876543210",
        onDetailsCompleted = {}
    )
}

@Composable
fun BasicDetailsScreen(
    phoneNumber: String,
    onDetailsCompleted: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: BasicDetailsViewModel = viewModel(
        factory = BasicDetailsViewModelFactory(phoneNumber, context)
    )

    // ✅ Fixed: Collect state properly
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F4F8))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_person),
                contentDescription = "Profile",
                tint = Color(0xFF57C6A9),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Complete Your Basic Details",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }

        // Subtitle
        Text(
            text = "Provide these details to verify your account and enable payouts. We only ask for what's necessary.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Full Name Input
        OutlinedTextField(
            value = state.fullName, // ✅ Fixed: use state variable
            onValueChange = { viewModel.updateFullName(it) },
            placeholder = {
                Text(
                    "Enter your full name",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF57C6A9),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                cursorColor = Color(0xFF57C6A9)
            ),
            singleLine = true
        )

        // Date of Birth Input
        OutlinedTextField(
            value = state.dateOfBirth, // ✅ Fixed: use state variable
            onValueChange = { input ->
                // Auto-format date as DD/MM/YYYY
                val formatted = formatDateInput(input, state.dateOfBirth)
                viewModel.updateDateOfBirth(formatted)
            },
            placeholder = {
                Text(
                    "DD/MM/YYYY",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    tint = Color(0xFF57C6A9)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF57C6A9),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                cursorColor = Color(0xFF57C6A9)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Gender Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Male", "Female", "Other").forEach { gender ->
                GenderButton(
                    text = gender,
                    isSelected = state.selectedGender == gender, // ✅ Fixed: use state variable
                    onClick = { viewModel.updateGender(gender) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // PAN Number Input
        OutlinedTextField(
            value = state.panNumber, // ✅ Fixed: use state variable
            onValueChange = { viewModel.updatePanNumber(it) },
            placeholder = {
                Text(
                    "Enter PAN Number",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_id_card),
                    contentDescription = "PAN Card",
                    tint = Color(0xFF57C6A9)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF57C6A9),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                cursorColor = Color(0xFF57C6A9)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        // Credit Score Status
        if (state.creditScore > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Credit Score",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${state.creditScore}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF57C6A9)
                    )
                    Text(
                        text = "Powered by CIBIL",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error Message
        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Privacy Notice
        Text(
            text = "Your information will be stored securely and used only for verification as per our Privacy Policy.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Continue Button
        Button(
            onClick = {
                // First fetch credit score, then save to database
                viewModel.fetchCreditScore { success ->
                    if (success) {
                        // Now save to database using AuthenticationService
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                val databaseProvider = com.example.insightsapp.data.remote.RemoteDatabaseProvider.getInstance(context)
                                val authService = databaseProvider.authenticationService

                                // ✅ Save basic details using stored user ID
                                authService.updateBasicDetails(
                                    fullName = state.fullName,
                                    dateOfBirth = state.dateOfBirth,
                                    gender = state.selectedGender,
                                    panNumber = state.panNumber,
                                    creditScore = state.creditScore
                                )

                                println("✅ Basic details saved and credit score fetched: ${state.creditScore}")

                                // Complete onboarding
                                val phoneNumber = databaseProvider.userSessionManager.getPhoneNumber()
                                if (phoneNumber != null) {
                                    authService.completeUserOnboarding(phoneNumber)
                                    println("✅ Onboarding completed for $phoneNumber")
                                }

                                onDetailsCompleted()

                            } catch (e: Exception) {
                                println("❌ Error saving basic details: ${e.message}")
                                e.printStackTrace()
                                // Still navigate even if save fails
                                onDetailsCompleted()
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = !state.isLoading && !state.creditScoreLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF57C6A9),
                disabledContainerColor = Color.Gray
            )
        ) {
            if (state.creditScoreLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fetching Credit Score...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF57C6A9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

fun formatDateInput(input: String, currentValue: String): String {
    val digitsOnly = input.filter { it.isDigit() }

    return when {
        digitsOnly.isEmpty() -> ""
        digitsOnly.length == 1 -> digitsOnly
        digitsOnly.length == 2 -> {
            val day = digitsOnly.toIntOrNull() ?: return currentValue
            if (day > 31) currentValue else digitsOnly
        }
        digitsOnly.length == 3 -> {
            val day = digitsOnly.substring(0, 2).toIntOrNull() ?: return currentValue
            val monthDigit = digitsOnly.substring(2, 3)
            if (day > 31) currentValue else "${digitsOnly.substring(0, 2)}/$monthDigit"
        }
        digitsOnly.length == 4 -> {
            val day = digitsOnly.substring(0, 2).toIntOrNull() ?: return currentValue
            val month = digitsOnly.substring(2, 4).toIntOrNull() ?: return currentValue
            if (day > 31 || month > 12 || month == 0) currentValue
            else "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}"
        }
        digitsOnly.length in 5..8 -> {
            val day = digitsOnly.substring(0, 2).toIntOrNull() ?: return currentValue
            val month = digitsOnly.substring(2, 4).toIntOrNull() ?: return currentValue
            val yearPart = digitsOnly.substring(4)

            if (day > 31 || month > 12 || month == 0) currentValue
            else "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}/$yearPart"
        }
        else -> {
            // More than 8 digits, truncate and format
            val truncated = digitsOnly.take(8)
            val day = truncated.substring(0, 2).toIntOrNull() ?: return currentValue
            val month = truncated.substring(2, 4).toIntOrNull() ?: return currentValue
            val year = truncated.substring(4)

            if (day > 31 || month > 12 || month == 0) currentValue
            else "${truncated.substring(0, 2)}/${truncated.substring(2, 4)}/$year"
        }
    }
}

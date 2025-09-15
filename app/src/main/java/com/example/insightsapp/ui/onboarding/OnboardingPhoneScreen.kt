package com.example.insightsapp.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.tooling.preview.Preview


@Preview(showBackground = true)
@Composable
fun OnboardingPhoneScreenPreview() {
    // Use simple remember state for preview, instead of real ViewModel
    var phoneNumber by remember { mutableStateOf("9876543210") }
    val isValid = phoneNumber.length == 10

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Enter your mobile number", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.filter { c -> c.isDigit() }.take(10) },
            label = { Text("Mobile Number (+91)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { /* No-op in preview */ },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun OnboardingPhoneScreen(viewModel: OnboardingViewModel = viewModel()) {
    val phoneNumber by viewModel.phoneNumber.collectAsState(initial = "")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Enter your mobile number", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { viewModel.onPhoneNumberChange(it) },
            label = { Text("Mobile Number (+91)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        val isValid = viewModel.isValidPhoneNumber(phoneNumber)

        Button(
            onClick = { /* Trigger navigation or backend call */ },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

//class OnboardingViewModel : androidx.lifecycle.ViewModel() {
//    private val _phoneNumber = kotlinx.coroutines.flow.MutableStateFlow("")
//    val phoneNumber = _phoneNumber.asStateFlow()
//
//    fun onPhoneNumberChange(newNumber: String) {
//        _phoneNumber.value = newNumber.filter { it.isDigit() }.take(10)
//    }
//
//    fun isValidPhoneNumber(number: String): Boolean = number.length == 10
//}

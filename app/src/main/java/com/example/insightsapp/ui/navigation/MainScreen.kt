package com.example.insightsapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightsapp.data.database.AppDatabase
import com.example.insightsapp.ui.main.MainViewModel
import com.example.insightsapp.ui.main.MainViewModelFactory
import com.example.insightsapp.ui.surveys.SurveysScreen
import com.example.insightsapp.ui.wallet.WalletScreen

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(phoneNumber = "+919876543210")
}

@Composable
fun MainScreen(
    initialTab: Int = 0,
    phoneNumber: String = "+919876543210"
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(database, phoneNumber)
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(initialTab) }

    LaunchedEffect(uiState) {
        println("🐛 MainScreen Debug:")
        println("   - User: ${uiState.user?.fullName} (${uiState.user?.phoneNumber})")
        println("   - Balance: ${uiState.walletBalance}")
        println("   - Transactions: ${uiState.transactions.size}")
        println("   - Surveys: ${uiState.surveys.size}")
    }

    LaunchedEffect(Unit) {
        // ✅ ALWAYS initialize surveys first
        viewModel.initializeSampleSurveys()
        // ✅ Only add signup reward if not already received
        // This will be handled by completeUserOnboarding in CongratulationsScreen
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF57C6A9)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = com.example.insightsapp.R.drawable.ic_survey),
                            contentDescription = "Surveys"
                        )
                    },
                    label = { Text("Surveys") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF57C6A9),
                        selectedTextColor = Color(0xFF57C6A9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            // ✅ Use XML wallet icon for navigation
                            painterResource(id = com.example.insightsapp.R.drawable.ic_wallet_nav),
                            contentDescription = "Wallet"
                        )
                    },
                    label = { Text("Wallet") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF57C6A9),
                        selectedTextColor = Color(0xFF57C6A9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = com.example.insightsapp.R.drawable.ic_profile),
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF57C6A9),
                        selectedTextColor = Color(0xFF57C6A9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF57C6A9))
                }
            } else {
                when (selectedTab) {
                    0 -> SurveysScreen(
                        // ✅ Better name extraction logic
                        userName = getFirstName(uiState.user?.fullName),
                        surveys = uiState.surveys,
                        onSurveyClick = { /* Handle survey click */ }
                    )
                    1 -> WalletScreen(
                        currentBalance = uiState.walletBalance,
                        transactions = uiState.transactions,
                        onWithdrawClick = { /* Handle withdraw */ },
                        onRedeemClick = { /* Handle redeem */ }
                    )
                    2 -> ProfileScreen(user = uiState.user)
                }
            }
        }
    }
}

// ✅ Helper function to extract first name properly
private fun getFirstName(fullName: String?): String {
    return when {
        fullName.isNullOrBlank() -> "User"
        fullName.contains(" ") -> fullName.split(" ").first()
        else -> fullName
    }.also {
        println("✅ Displaying name: '$it' from fullName: '$fullName'")
    }
}


@Composable
fun ProfileScreen(user: com.example.insightsapp.data.database.User?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            user?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Name: ${it.fullName}")
                Text("Phone: ${it.phoneNumber}")
                Text("Credit Score: ${it.creditScore}")
            }
        }
    }
}

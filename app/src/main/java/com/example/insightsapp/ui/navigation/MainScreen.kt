package com.example.insightsapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.ui.main.MainViewModel
import com.example.insightsapp.ui.main.MainViewModelFactory
import com.example.insightsapp.ui.profile.ProfileScreen
import com.example.insightsapp.ui.surveys.SurveysScreen
import com.example.insightsapp.ui.wallet.WalletScreen
import com.example.insightsapp.ui.coupons.CouponsScreen

// NEW imports for writing the debit on claim
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.insightsapp.data.database.Transaction

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
    val databaseProvider = RemoteDatabaseProvider.getInstance(context)

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(databaseProvider, phoneNumber)
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Overlay flag for the coupons screen (unchanged)
    var showCoupons by remember { mutableStateOf(false) }

    // NEW: scope to perform suspend repository calls from UI callbacks
    val scope = rememberCoroutineScope()

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
                        onSurveyClick = { survey ->
                            println("Survey clicked: ${survey.title}")
                            // TODO: Navigate to survey detail screen
                        }
                    )
                    1 -> WalletScreen(
                        phoneNumber = phoneNumber,
                        onRedeemClick = { showCoupons = true } // open overlay
                    )
                    2 -> ProfileScreen(
                        phoneNumber = phoneNumber
                    )
                }
            }

            // Coupons overlay: now writes a DEBIT on claim, then closes
            if (showCoupons) {
                CouponsScreen(
                    onClose = { showCoupons = false },
                    onClaimed = { coupon ->
                        val userId = uiState.user?.userId
                        if (userId == null) {
                            // No user yet; just close overlay gracefully
                            showCoupons = false
                            return@CouponsScreen
                        }
                        val txn = Transaction(
                            transactionId = "txn_${System.currentTimeMillis()}",
                            userId = userId,
                            type = "DEBIT",
                            amount = coupon.points.toDouble(),     // treat points as wallet unit
                            description = "Coupon: ${coupon.title}",
                            timestamp = System.currentTimeMillis(),
                            status = "SUCCESS"
                        )
                        scope.launch {
                            // Persist the debit; Wallet observes transactions and will recompute balance
                            databaseProvider.transactionRepository.insertTransaction(txn)
                            // Close overlay after saving
                            showCoupons = false
                        }
                    }
                )
            }
        }
    }
}

// ✅ Helper function to extract first name properly (unchanged)
private fun getFirstName(fullName: String?): String {
    return when {
        fullName.isNullOrBlank() -> "User"
        fullName.contains(" ") -> fullName.split(" ").first()
        else -> fullName
    }.also {
        println("✅ Displaying name: '$it' from fullName: '$fullName'")
    }
}

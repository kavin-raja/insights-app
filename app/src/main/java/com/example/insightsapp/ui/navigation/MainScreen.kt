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

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.insightsapp.R
import kotlinx.coroutines.launch
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.remote.SupabaseConfig
import com.example.insightsapp.data.remote.SupabaseHttpClient
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

import com.example.insightsapp.ui.coupons.MyCouponsScreen
import com.example.insightsapp.ui.coupons.MyCouponsViewModel
import com.example.insightsapp.data.remote.GetUserCouponsUseCase
import com.example.insightsapp.data.api.CouponApiService
import okhttp3.OkHttpClient

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        phoneNumber = "+919876543210",
        initialTab = TODO(),
        navController = TODO(),
        userId = TODO()
    )
}

@Composable
fun MainScreen(
    initialTab: Int = 0,
    phoneNumber: String = "+919876543210",
    navController: NavController,
    userId: String
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

    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        println("🐛 MainScreen Debug:")
        println("   - User: ${uiState.user?.fullName} (${uiState.user?.phoneNumber})")
        println("   - Balance: ${uiState.walletBalance}")
        println("   - Transactions: ${uiState.transactions.size}")
        println("   - Surveys: ${uiState.surveys.size}")
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
                            painterResource(id = R.drawable.ic_survey),
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
                            painterResource(id = R.drawable.ic_wallet_nav),
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
                            painterResource(id = R.drawable.ic_profile),
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
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF57C6A9))
                }
            } else {
                when (selectedTab) {
                    0 -> SurveysScreen(
                        userName = getFirstName(uiState.user?.fullName),
                        surveys = uiState.surveys,
                        phoneNumber = phoneNumber,
                        onSurveyClick = { survey, phoneNumber ->
                            println("🚀 MainScreen: Navigating to survey with userId: $userId")
                            navController.navigate("survey/${survey.surveyId}/$userId")
                        }
                    )
                    1 -> {
                        println("📱 MainScreen: Passing to WalletScreen - balance=${uiState.walletBalance}, transactions=${uiState.transactions.size}")

                        WalletScreen(
                            phoneNumber = phoneNumber,
                            onRedeemClick = { showCoupons = true },
                            currentBalance = uiState.walletBalance,
                            transactions = uiState.transactions,
                            isLoading = uiState.isLoading
                        )
                    }
                    2 -> ProfileScreen(
                        phoneNumber = phoneNumber,
                        onNavigateToMyCoupons = {
                            // Use the userId from uiState or the parameter
                            val userIdToPass = uiState.user?.userId ?: userId
                            println("🎫 Navigation: Opening My Coupons for user: $userIdToPass")
                            navController.navigate("my_coupons/$userIdToPass")
                        }
                    )
                }
            }

            if (showCoupons) {
                CouponsScreen(
                    phoneNumber = phoneNumber,
                    onClose = { showCoupons = false },
                    onClaimed = { coupon ->
                        val userId = uiState.user?.userId
                        if (userId == null) {
                            showCoupons = false
                            return@CouponsScreen
                        }

                        scope.launch {
                            try {
                                val txn = Transaction(
                                    transactionId = "txn_${System.currentTimeMillis()}",
                                    userId = userId,
                                    type = "DEBIT",
                                    amount = coupon.points.toDouble(),
                                    description = "Coupon: ${coupon.title}",
                                    timestamp = System.currentTimeMillis(),
                                    status = "SUCCESS"
                                )

                                databaseProvider.transactionRepository.insertTransaction(txn)
                                println("✅ Transaction created: ${txn.description}")

                                try {
                                    val httpClient = SupabaseHttpClient.getInstance()
                                    val result = httpClient.client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/user_coupons") {
                                        headers {
                                            append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                            append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                                            append("Content-Profile", "public")
                                        }
                                        contentType(ContentType.Application.Json)
                                        setBody("""{"user_id":"$userId","coupon_id":"${coupon.id}"}""")
                                    }

                                    println("✅ User coupon inserted: ${result.status}")
                                } catch (e: Exception) {
                                    println("⚠️ User coupon insert failed (continuing anyway): ${e.message}")
                                }

                                viewModel.onCouponPurchased(coupon.points.toDouble())

                            } catch (e: Exception) {
                                println("❌ Coupon purchase error: ${e.message}")
                                e.printStackTrace()
                            } finally {
                                showCoupons = false
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun getFirstName(fullName: String?): String {
    return when {
        fullName.isNullOrBlank() -> "User"
        fullName.contains(" ") -> fullName.split(" ").first()
        else -> fullName
    }.also {
        println("✅ Displaying name: '$it' from fullName: '$fullName'")
    }
}

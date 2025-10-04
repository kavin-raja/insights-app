package com.example.insightsapp.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.insightsapp.ui.navigation.MainScreen
import com.example.insightsapp.ui.privacy.PrivacyConsentScreen
import com.example.insightsapp.ui.profile.BasicDetailsScreen
import com.example.insightsapp.ui.rewards.CongratulationsScreen
import com.example.insightsapp.ui.survey.SurveyCompletionScreen
import com.example.insightsapp.ui.survey.SurveyQuestionScreen
import com.example.insightsapp.data.api.CouponApiService
import com.example.insightsapp.data.repository.CouponRepository
import com.example.insightsapp.data.remote.GetUserCouponsUseCase
import com.example.insightsapp.ui.coupons.MyCouponsScreen
import com.example.insightsapp.ui.coupons.MyCouponsViewModel
import okhttp3.OkHttpClient

@Composable
fun OnboardingNavGraph() {
    val navController = rememberNavController()

    val currentUserId = remember { mutableStateOf<String?>(null) }

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onFinished = { navController.navigate("continue_mobile") })
        }

        composable("continue_mobile") {
            ContinueWithMobileScreen(onContinueClick = { navController.navigate("phone_input") })
        }

        composable("phone_input") {
            OnboardingPhoneScreen(onNavigateToOtp = { phoneNumber ->
                val formattedNumber = if (phoneNumber.startsWith("+91")) {
                    phoneNumber
                } else {
                    "+91$phoneNumber"
                }
                navController.navigate("otp_verification/$formattedNumber")
            })
        }

        composable(
            route = "otp_verification/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onOtpVerified = { isReturningUser, userId ->
                    currentUserId.value = userId

                    if (isReturningUser) {
                        println("✅ Returning user - skipping to main app. UserId: $userId")
                        navController.navigate("main_screen/0/$phoneNumber/$userId") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    } else {
                        println("✅ New user - continuing onboarding. UserId: $userId")
                        navController.navigate("privacy_consent/$phoneNumber/$userId") {
                            popUpTo("phone_input") { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "privacy_consent/{phoneNumber}/{userId}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            PrivacyConsentScreen(
                phoneNumber = phoneNumber,
                onPermissionsGranted = {
                    navController.navigate("basic_details/$phoneNumber/$userId") {
                        popUpTo("otp_verification/$phoneNumber") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "basic_details/{phoneNumber}/{userId}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            BasicDetailsScreen(
                phoneNumber = phoneNumber,
                onDetailsCompleted = {
                    navController.navigate("congratulations/$phoneNumber/$userId") {
                        popUpTo("basic_details/$phoneNumber/$userId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "congratulations/{phoneNumber}/{userId}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            CongratulationsScreen(
                phoneNumber = phoneNumber,
                onExploreSurveys = {
                    navController.navigate("main_screen/0/$phoneNumber/$userId") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onGoToWallet = {
                    navController.navigate("main_screen/1/$phoneNumber/$userId") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "main_screen/{tab}/{phoneNumber}/{userId}",
            arguments = listOf(
                navArgument("tab") { type = NavType.IntType; defaultValue = 0 },
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            currentUserId.value = userId

            MainScreen(
                initialTab = initialTab,
                phoneNumber = phoneNumber,
                userId = userId,
                navController = navController
            )
        }

        composable(
            route = "survey/{surveyId}/{userId}",
            arguments = listOf(
                navArgument("surveyId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId") ?: return@composable
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

            SurveyQuestionScreen(
                surveyId = surveyId,
                userId = userId,
                onBack = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
                onComplete = { rewardPoints ->
                    val storedUserId = currentUserId.value ?: userId
                    navController.navigate("surveyComplete/$rewardPoints/$storedUserId") {
                        popUpTo("main_screen/{tab}/{phoneNumber}/{userId}")
                    }
                }
            )
        }

        composable(
            route = "surveyComplete/{rewardPoints}/{userId}",
            arguments = listOf(
                navArgument("rewardPoints") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rewardPoints = backStackEntry.arguments?.getInt("rewardPoints") ?: 0
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            SurveyCompletionScreen(
                rewardPoints = rewardPoints,
                onBackToHome = {
                    navController.navigate("main_screen/0/+919150423766/$userId") {
                        popUpTo("main_screen/0/+919150423766/$userId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "my_coupons/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId.value ?: "USER0000000002"

            println("🎫 Navigation: Opening My Coupons for user: $userId")

            val viewModel: MyCouponsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val apiService = CouponApiService()
                        val repository = CouponRepository(apiService)
                        val useCase = GetUserCouponsUseCase(repository)
                        @Suppress("UNCHECKED_CAST")
                        return MyCouponsViewModel(useCase) as T
                    }
                }
            )

            MyCouponsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel,
                userId = userId
            )
        }
    }
}

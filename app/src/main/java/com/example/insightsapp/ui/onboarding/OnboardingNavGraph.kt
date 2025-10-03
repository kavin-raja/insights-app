package com.example.insightsapp.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.ui.navigation.MainScreen
import com.example.insightsapp.ui.privacy.PrivacyConsentScreen
import com.example.insightsapp.ui.profile.BasicDetailsScreen
import com.example.insightsapp.ui.rewards.CongratulationsScreen
import com.example.insightsapp.ui.survey.SurveyCompletionScreen
import com.example.insightsapp.ui.survey.SurveyQuestionScreen

@Composable
fun OnboardingNavGraph(
) {
    val navController = rememberNavController()

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
                onOtpVerified = { isReturningUser ->
                    if (isReturningUser) {
                        println("✅ Returning user - skipping to main app")
                        navController.navigate("main_screen/0/$phoneNumber") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    } else {
                        println("✅ New user - continuing onboarding")
                        navController.navigate("privacy_consent/$phoneNumber") {
                            popUpTo("phone_input") { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "privacy_consent/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            PrivacyConsentScreen(
                phoneNumber = phoneNumber,
                onPermissionsGranted = {
                    navController.navigate("basic_details/$phoneNumber") {
                        popUpTo("otp_verification/$phoneNumber") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "basic_details/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            BasicDetailsScreen(
                phoneNumber = phoneNumber,
                onDetailsCompleted = {
                    navController.navigate("congratulations/$phoneNumber") {
                        popUpTo("basic_details/$phoneNumber") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "congratulations/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            CongratulationsScreen(
                phoneNumber = phoneNumber,
                onExploreSurveys = {
                    navController.navigate("main_screen/0/$phoneNumber") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onGoToWallet = {
                    navController.navigate("main_screen/1/$phoneNumber") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "main_screen/{tab}/{phoneNumber}",
            arguments = listOf(
                navArgument("tab") { type = NavType.IntType; defaultValue = 0 },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            MainScreen(
                initialTab = initialTab,
                phoneNumber = phoneNumber,
                navController = navController
            )
        }

        // ✅ Fixed survey navigation with proper reward points handling
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
                onComplete = { rewardPoints -> // ✅ Now receives reward points parameter
                    navController.navigate("surveyComplete/$rewardPoints") {
                        popUpTo("main_screen/{tab}/{phoneNumber}") // ✅ Go back to main screen
                    }
                }
            )
        }

        composable(
            route = "surveyComplete/{rewardPoints}",
            arguments = listOf(navArgument("rewardPoints") { type = NavType.IntType })
        ) { backStackEntry ->
            val rewardPoints = backStackEntry.arguments?.getInt("rewardPoints") ?: 0

            SurveyCompletionScreen(
                rewardPoints = rewardPoints,
                onBackToHome = {
                    navController.navigate("main_screen/0/+919150423766") { // ✅ Use actual phone number or pass it through navigation
                        popUpTo("main_screen/0/+919150423766") { inclusive = true }
                    }
                }
            )
        }
    }
}

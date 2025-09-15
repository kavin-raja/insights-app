package com.example.insightsapp.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun OnboardingNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onFinished = { navController.navigate("continue_mobile") })
        }
        composable("continue_mobile") {
            ContinueWithMobileScreen(onContinueClick = { navController.navigate("phone_input") })
        }
        composable("phone_input") {
            OnboardingPhoneScreen()
        }
    }
}
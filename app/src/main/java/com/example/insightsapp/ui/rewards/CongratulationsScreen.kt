package com.example.insightsapp.ui.rewards

import android.R.attr.phoneNumber
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun CongratulationsScreenPreview() {
    CongratulationsScreen(
        phoneNumber= "+919876543210",
        onExploreSurveys = {},
        onGoToWallet = {}
    )
}

@Composable
fun CongratulationsScreen(
    phoneNumber: String,
    onExploreSurveys: () -> Unit,
    onGoToWallet: () -> Unit
) {
    val context = LocalContext.current
    val databaseProvider = RemoteDatabaseProvider.getInstance(context)
    val authService = remember { AuthenticationService(databaseProvider) }
    val scope = rememberCoroutineScope()

    // ✅ Complete onboarding when this screen is reached
    LaunchedEffect(Unit) {
        scope.launch {
            authService.completeUserOnboarding(phoneNumber)
            println("✅ Onboarding completed for $phoneNumber")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F4F8))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Title
        Text(
            text = "Congratulations!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subtitle
        Text(
            text = "You've earned your first reward.",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // ✅ Use your 3D wallet PNG image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp)
        ) {
            // 3D Wallet PNG Image
            Image(
                painter = painterResource(id = com.example.insightsapp.R.drawable.ic_wallet), // Your PNG
                contentDescription = "3D Wallet with Coins",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Amount Badge positioned over the image
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF57C6A9)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "₹500 Added",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Description
        Text(
            text = "Your reward has been credited to your wallet.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Explore Surveys Button
        Button(
            onClick = onExploreSurveys,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
        ) {
            Text(
                text = "Explore Surveys",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Go to Wallet Button
        TextButton(
            onClick = onGoToWallet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Go to Wallet",
                fontSize = 18.sp,
                color = Color(0xFF57C6A9),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}


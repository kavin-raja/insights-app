package com.example.insightsapp.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightsapp.R

val Montserat = FontFamily(Font(R.font.montserrat_bold, weight = FontWeight.Bold))
val QuickSandFontSemiBold600 = FontFamily(Font(R.font.quicksand_variable_font, weight = FontWeight.SemiBold))


@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onFinished = {})
}

@Composable
fun WelcomeScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onFinished()
    }

    val backgroundColor = Color(0xFFF5F5F8) // Soft gray background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon and Title in a row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bulb),
                    contentDescription = "Bulb Icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Insights.X",
                    style = TextStyle(
                        fontFamily = Montserat, // Custom rounded font
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        color = Color(0xFF181818)
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            // Orange logo
            Image(
                painter = painterResource(id = R.drawable.logo2 ),
                contentDescription = "Loading Icon",
                modifier = Modifier.size(60.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Loading...",
                style = TextStyle(
                    fontFamily = QuickSandFontSemiBold600,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = Color(0xFF181818),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

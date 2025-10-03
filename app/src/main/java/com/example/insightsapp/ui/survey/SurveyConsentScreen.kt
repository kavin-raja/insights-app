package com.example.insightsapp.ui.survey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightsapp.data.models.ApiSurvey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyConsentScreen(
    survey: ApiSurvey,
    onBack: () -> Unit,
    onConsent: () -> Unit
) {
    var consentGiven by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consent Required") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Please review before starting",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Survey info
            Text(
                text = "For this survey, the following information will be collected and shared with the brand for the stated purpose.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            // Data points with checkmarks
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                survey.dataPoints.forEach { dataPoint ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF57C6A9),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dataPoint,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Purpose section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF57C6A9).copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Purpose",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = survey.purpose,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Three-party agreement
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Agreement",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Three parties illustration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👤", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("You", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Text("———", color = Color.Gray)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏢", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Brand", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Text("———", color = Color.Gray)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛡️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Insights.X", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Text(
                        text = "Survey Data Agreement",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "This agreement outlines how your data will be used, stored, and protected for this survey.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Agreement points
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✓", color = Color(0xFF57C6A9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Your consent is required", fontSize = 14.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✓", color = Color(0xFF57C6A9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Data will be used only for stated purposes", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Consent checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = consentGiven,
                    onCheckedChange = { consentGiven = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF57C6A9))
                )
                Text(
                    text = "I agree to share this data for this survey",
                    fontSize = 16.sp
                )
            }

            // Continue button
            Button(
                onClick = onConsent,
                enabled = consentGiven,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Privacy notice
            Text(
                text = "You can withdraw this consent anytime in Privacy Settings.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

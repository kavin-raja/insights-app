package com.example.insightsapp.ui.surveys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.insightsapp.data.database.Survey

@Preview(showBackground = true)
@Composable
fun SurveysScreenPreview() {
    val mockSurveys = listOf(
        Survey(
            id = "1",
            title = "Brand 1 Survey",
            description = "Help us improve our product by sharing your thoughts.",
            brandName = "Brand 1",
            reward = 250.0,
            duration = 3
        ),
        Survey(
            id = "2",
            title = "Brand 2 Survey",
            description = "Discuss your preferences and help us improve our products.",
            brandName = "Brand 2",
            reward = 250.0,
            duration = 5
        )
    )

    SurveysScreen(
        userName = "Jessica",
        surveys = mockSurveys,
        onSurveyClick = {}
    )
}

@Composable
fun SurveysScreen(
    userName: String,
    surveys: List<Survey>,
    onSurveyClick: (Survey) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F8))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Profile Picture
            AsyncImage(
                model = "https://via.placeholder.com/50", // Replace with actual user image
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Surveys List
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(surveys) { survey ->
                SurveyCard(
                    survey = survey,
                    onSurveyClick = { onSurveyClick(survey) }
                )
            }
        }
    }
}

@Composable
fun SurveyCard(
    survey: Survey,
    onSurveyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Survey Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = survey.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Reward Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF57C6A9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "₹${survey.reward.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = com.example.insightsapp.R.drawable.ic_time),
                    contentDescription = "Duration",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${survey.duration} min",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Description
            Text(
                text = survey.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Start Button
            Button(
                onClick = onSurveyClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF57C6A9))
            ) {
                Text(
                    text = "Start",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

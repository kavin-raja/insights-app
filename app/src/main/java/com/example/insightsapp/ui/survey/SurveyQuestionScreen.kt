package com.example.insightsapp.ui.survey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyQuestionScreen(
    surveyId: String,
    userId: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onComplete: (Int) -> Unit, // ✅ Pass reward points to completion
    viewModel: SurveyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(surveyId) {
        viewModel.loadSurvey(surveyId, userId)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete(uiState.rewardPoints) // ✅ Pass reward points
        }
    }

    // Show consent screen first
    if (uiState.showConsent && uiState.surveyWithQuestions != null) {
        SurveyConsentScreen(
            survey = uiState.surveyWithQuestions!!.survey,
            onBack = onBack,
            onConsent = { viewModel.giveConsentAndStartSurvey(userId) }
        )
        return
    }

    // Loading state
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF57C6A9))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading survey...")
            }
        }
        return
    }

    // Error state
    if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: ${uiState.error}", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val surveyWithQuestions = uiState.surveyWithQuestions ?: return
    val questions = surveyWithQuestions.questions

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Question ${uiState.currentQuestionIndex + 1} of ${questions.size}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { viewModel.getProgress() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF57C6A9),
                trackColor = Color(0xFF57C6A9).copy(alpha = 0.2f)
            )

            // Question content
            val currentQuestion = viewModel.getCurrentQuestion()

            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Question text card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = currentQuestion.questionText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Answer options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentQuestion.options.forEach { option ->
                            val isSelected = uiState.answers[currentQuestion.questionId] == option.optionId

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.selectAnswer(currentQuestion.questionId, option.optionId)
                                        }
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        Color(0xFF57C6A9).copy(alpha = 0.1f)
                                    else
                                        Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.selectAnswer(currentQuestion.questionId, option.optionId)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF57C6A9)
                                        )
                                    )

                                    Text(
                                        text = option.optionText,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = viewModel::previousQuestion,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                if (viewModel.isLastQuestion()) {
                                    viewModel.submitSurvey()
                                } else {
                                    viewModel.nextQuestion()
                                }
                            },
                            enabled = viewModel.canProceed(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF57C6A9)
                            )
                        ) {
                            Text(
                                if (viewModel.isLastQuestion()) "Submit" else "Next"
                            )
                        }
                    }

                    Text(
                        text = "Your progress is auto-saved.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

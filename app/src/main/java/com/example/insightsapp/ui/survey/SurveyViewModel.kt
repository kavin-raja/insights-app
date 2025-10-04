package com.example.insightsapp.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.models.*
import com.example.insightsapp.data.repository.SurveyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SurveyUiState(
    val surveyWithQuestions: SurveyWithQuestions? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val showConsent: Boolean = true,
    val consentGiven: Boolean = false,
    val agreementAccepted: Boolean = false,
    val responseId: String? = null,
    val isCompleted: Boolean = false,
    val rewardPoints: Int = 0, // ✅ Add this field
    val error: String? = null
)

class SurveyViewModel(
    private val repository: SurveyRepository = SurveyRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    fun loadSurvey(surveyId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val surveyWithQuestions = repository.getSurveyWithQuestions(surveyId)

                if (surveyWithQuestions != null) {
                    _uiState.value = _uiState.value.copy(
                        surveyWithQuestions = surveyWithQuestions,
                        rewardPoints = surveyWithQuestions.survey.rewardPoints, // ✅ Set reward points
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load survey"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun giveConsentAndStartSurvey(userId: String) {
        val surveyId = _uiState.value.surveyWithQuestions?.survey?.surveyId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {

                val response = repository.startSurvey(userId, surveyId)
                println("response: $response")
                if (response != null) {
                    val consentGiven = response.consentGiven ?: true
                    val agreementAccepted = response.agreementAccepted ?: true
                    _uiState.value = _uiState.value.copy(
                        responseId = response.responseId,
                        consentGiven = consentGiven,
                        agreementAccepted = agreementAccepted,
                        showConsent = false,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to start survey"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to start survey"
                )
            }
        }
    }

    fun selectAnswer(questionId: String, optionId: String) {
        val updatedAnswers = _uiState.value.answers.toMutableMap()
        updatedAnswers[questionId] = optionId

        _uiState.value = _uiState.value.copy(answers = updatedAnswers)
    }

    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val maxIndex = (_uiState.value.surveyWithQuestions?.questions?.size ?: 1) - 1

        if (currentIndex < maxIndex) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = currentIndex + 1
            )
        }
    }

    fun previousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex > 0) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = currentIndex - 1
            )
        }
    }

    fun submitSurvey() {
        val state = _uiState.value
        val responseId = state.responseId ?: return

        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val success = repository.submitAnswers(responseId, state.answers)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = success,
                    error = if (success) null else "Failed to submit survey"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to submit survey"
                )
            }
        }
    }

    fun getCurrentQuestion(): SurveyQuestion? =
        _uiState.value.surveyWithQuestions?.questions?.getOrNull(_uiState.value.currentQuestionIndex)

    fun getProgress(): Float {
        val questions = _uiState.value.surveyWithQuestions?.questions
        return if (questions?.isEmpty() != false) 0f
        else (_uiState.value.currentQuestionIndex + 1).toFloat() / questions.size.toFloat()
    }

    fun canProceed(): Boolean {
        val currentQuestion = getCurrentQuestion() ?: return false
        return _uiState.value.answers.containsKey(currentQuestion.questionId)
    }

    fun isLastQuestion(): Boolean {
        val questions = _uiState.value.surveyWithQuestions?.questions ?: return true
        return _uiState.value.currentQuestionIndex == questions.size - 1
    }
}

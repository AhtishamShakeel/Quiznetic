package com.example.quiznetic.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quiznetic.data.Question
import com.example.quiznetic.data.Quiz

class QuizViewModel : ViewModel() {
    private lateinit var quiz: Quiz
    private var currentQuestionIndex = 0
    private val MAX_QUESTIONS = 10

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _isQuizComplete = MutableLiveData<Boolean>()
    val isQuizComplete: LiveData<Boolean> = _isQuizComplete
    
    private val _correctAnswers = MutableLiveData<Int>(0)
    val correctAnswers: LiveData<Int> = _correctAnswers
    
    private val _totalQuestions = MutableLiveData<Int>(0)
    val totalQuestions: LiveData<Int> = _totalQuestions

    fun setQuiz(quiz: Quiz) {
        this.quiz = quiz
        // Limit questions to MAX_QUESTIONS or the available count, whichever is smaller
        val limitedQuestions = if (quiz.questions.size > MAX_QUESTIONS) {
            quiz.questions.shuffled().take(MAX_QUESTIONS)
        } else {
            quiz.questions
        }
        // Create a new quiz with limited questions
        this.quiz = Quiz(
            id = quiz.id,
            title = quiz.title,
            questions = limitedQuestions
        )
        _totalQuestions.value = this.quiz.questions.size
        showCurrentQuestion()
    }

    fun submitAnswer(selectedAnswerIndex: Int) {
        // Update count if answer is correct
        val currentQuestion = quiz.questions[currentQuestionIndex]
        if (selectedAnswerIndex == currentQuestion.correctAnswer) {
            _correctAnswers.value = (_correctAnswers.value ?: 0) + 1
        }
        
        if (currentQuestionIndex < quiz.questions.size - 1 && currentQuestionIndex < MAX_QUESTIONS - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
        } else {
            _isQuizComplete.postValue(true)
        }
    }

    private fun showCurrentQuestion() {
        _currentQuestion.value = quiz.questions[currentQuestionIndex]
    }
}
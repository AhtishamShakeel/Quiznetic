package com.example.quiznetic.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quiznetic.data.Questions
import com.example.quiznetic.data.Quiz

class QuizViewModel : ViewModel() {
    private lateinit var quiz: Quiz
    private var currentQuestionIndex = 0
    private var score = 0

    private val _currentQuestion = MutableLiveData<Questions>()
    val currentQuestion: LiveData<Questions> = _currentQuestion

    private val _isQuizComplete = MutableLiveData<Boolean>()
    val isQuizComplete: LiveData<Boolean> = _isQuizComplete

    private val _score = MutableLiveData<Int>()
    val scoreLiveData: LiveData<Int> = _score

    fun setQuiz(quiz: Quiz) {
        this.quiz = quiz
        currentQuestionIndex = 0
        score = 0
        _score.value = score
        _isQuizComplete.value = false
        showCurrentQuestion()
    }

    fun submitAnswer(selectedAnswerIndex: Int) {
        val currentQuestion = quiz.questions[currentQuestionIndex]
        if (selectedAnswerIndex == currentQuestion.correctAnswer) {
            score += 1
            _score.value = score
        }

        if (currentQuestionIndex < quiz.questions.size - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
        } else {
            _isQuizComplete.value = true
        }
    }

    private fun showCurrentQuestion() {
        _currentQuestion.value = quiz.questions[currentQuestionIndex]
    }
}

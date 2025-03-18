package com.example.quiznetic.quiz

import com.example.quiznetic.data.Quiz

data class QuizData(
    val version: Int,
    val categories: List<Category>
)

data class Category(
    val name: String,
    val quizzes: List<Quiz>
)

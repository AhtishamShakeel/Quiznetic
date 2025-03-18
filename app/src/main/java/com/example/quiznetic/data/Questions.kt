package com.example.quiznetic.data

import java.io.Serializable

data class Questions(
    val text: String,
    val options: String,
    val correctAnswer: Int
) : Serializable
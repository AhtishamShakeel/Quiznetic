package com.aunix.quiznetic.data

import java.io.Serializable

data class QuizCategory(
    val name: String,
    val lottieAnimationResId: Int,
    val description: String
) : Serializable
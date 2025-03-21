package com.aunix.quiznetic.data

import java.io.Serializable

data class Quiz (
    val id: String,
    val title: String,
    val questions: List<Question>
) : Serializable
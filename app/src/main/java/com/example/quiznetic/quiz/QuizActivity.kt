package com.example.quiznetic.quiz

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quiznetic.R
import com.example.quiznetic.data.Quiz

class QuizActivity : AppCompatActivity() {
    private val viewModel: QuizViewModel by viewModels()

    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionText = findViewById(R.id.questionText)
        optionsGroup = findViewById(R.id.optionsGroup)
        submitButton = findViewById(R.id.submitButton)

        val quiz = intent.getSerializableExtra(EXTRA_QUIZ) as? Quiz
        if (quiz != null) {
            viewModel.setQuiz(quiz)
        } else {
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.currentQuestion.observe(this) { question ->
            displayQuestion(question)
        }

        viewModel.isQuizComplete.observe(this) { isComplete ->
            if (isComplete) {
                Toast.makeText(this, "Quiz Complete! Score: ${viewModel.scoreLiveData.value}", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        submitButton.setOnClickListener {
            val selectedOptionIndex = optionsGroup.indexOfChild(findViewById(optionsGroup.checkedRadioButtonId))
            if (selectedOptionIndex != -1) {
                viewModel.submitAnswer(selectedOptionIndex)
            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayQuestion(question: com.example.quiznetic.data.Questions) {
        questionText.text = question.text
        optionsGroup.removeAllViews()

        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.text = option.toString()
            optionsGroup.addView(radioButton)
        }
    }

    companion object {
        const val EXTRA_QUIZ = "extra_quiz"
    }
}

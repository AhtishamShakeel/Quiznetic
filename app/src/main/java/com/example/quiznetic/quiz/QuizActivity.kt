package com.example.quiznetic.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.text.Html
import android.os.Build
import android.util.Log
import com.example.quiznetic.R
import com.example.quiznetic.data.Question
import com.example.quiznetic.data.Quiz
import com.example.quiznetic.databinding.ActivityQuizBinding
import com.example.quiznetic.utils.AdManager

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding
    private val viewModel: QuizViewModel by viewModels()
    private var timer: CountDownTimer? = null
    private var selectedAnswerIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preload interstitial ad that will be shown after quiz completion
        AdManager.preloadInterstitialAd(this)
        
        // Load banner ad
        AdManager.loadBannerAd(this, binding.adContainer)

        val quiz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_QUIZ, Quiz::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_QUIZ)
        }

        if (quiz != null) {
            Log.d("QuizActivity", "Starting quiz with ${quiz.questions.size} questions")
            viewModel.setQuiz(quiz)
        } else {
            Log.e("QuizActivity", "Quiz data was null")
            finish()
        }

        setupViews()
        observeViewModel()
        startTimer()
    }

    private fun setupViews() {
        binding.submitButton.setOnClickListener {
            if (selectedAnswerIndex == -1) {
                Toast.makeText(this, R.string.select_answer, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.submitButton.isEnabled = false
            checkAnswer()
        }
    }

    private fun observeViewModel() {
        viewModel.currentQuestion.observe(this) { question ->
            displayQuestion(question)
            // Restart timer for new question
            startTimer()
        }

        viewModel.isQuizComplete.observe(this) { isComplete ->
            if (isComplete) {
                timer?.cancel() // Stop timer when quiz is complete
                showQuizCompleteDialog()
            }
        }

        viewModel.totalQuestions.observe(this) { total ->
            Log.d("QuizActivity", "Total questions: $total")
        }
    }

    private fun displayQuestion(question: Question) {
        binding.apply {
            val decodedQuestion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(question.text, Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(question.text).toString()
            }
            questionText.text = decodedQuestion

            // Clear previous answers
            optionsContainer.removeAllViews()
            selectedAnswerIndex = -1  // Reset selection

            // Use options directly as a list instead of splitting
            question.options.forEachIndexed { index, option ->
                val optionView = LayoutInflater.from(this@QuizActivity)
                    .inflate(R.layout.item_option, optionsContainer, false)
                val textView = optionView.findViewById<TextView>(R.id.optionText)
                val cardView = optionView.findViewById<CardView>(R.id.optionCard)

                textView.text = option.trim()
                cardView.setBackgroundResource(R.drawable.default_option_background)

                optionView.setOnClickListener {
                    resetOptions() // Reset all options before selecting a new one
                    selectedAnswerIndex = index
                    cardView.setBackgroundResource(R.drawable.selected_option_background)
                }

                optionsContainer.addView(optionView)
            }
        }
    }

    private fun resetOptions() {
        for (i in 0 until binding.optionsContainer.childCount) {
            val child = binding.optionsContainer.getChildAt(i)
            val cardView = child.findViewById<CardView>(R.id.optionCard)
            cardView.setBackgroundResource(R.drawable.default_option_background)
        }
    }

    private fun checkAnswer() {
        // Cancel timer
        timer?.cancel()
        
        val correctIndex = viewModel.currentQuestion.value?.correctAnswer ?: -1

        for (i in 0 until binding.optionsContainer.childCount) {
            val child = binding.optionsContainer.getChildAt(i)
            val cardView = child.findViewById<CardView>(R.id.optionCard)

            if (i == correctIndex) {
                cardView.setBackgroundResource(R.drawable.correct_option_background)
            } else if (i == selectedAnswerIndex) {
                cardView.setBackgroundResource(R.drawable.wrong_option_background)
            }
        }

        // Enable button after delay and move to next question
        binding.root.postDelayed({
            binding.submitButton.isEnabled = true
            viewModel.submitAnswer(selectedAnswerIndex)
        }, 1000)
    }

    private fun startTimer() {
        // Cancel any existing timer
        timer?.cancel()
        
        // Start a new 10-second timer
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timerText.text = getString(R.string.timer_format, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                // Time's up, mark as wrong and move to next question
                viewModel.submitAnswer(-1)
                binding.submitButton.isEnabled = true
            }
        }.start()
    }

    private fun showQuizCompleteDialog() {
        // Show quiz results dialog
        QuizResultsDialog.show(
            fragmentManager = supportFragmentManager,
            correctAnswers = viewModel.correctAnswers.value ?: 0,
            totalQuestions = viewModel.totalQuestions.value ?: 0,
            onDismiss = {
                // If 10 or more questions were completed, show an interstitial ad
                if ((viewModel.totalQuestions.value ?: 0) >= 10) {
                    AdManager.showInterstitialAd(this) {
                        // This callback runs after the ad is closed
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    // Just finish if less than 10 questions
                    setResult(RESULT_OK)
                    finish()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    companion object {
        const val EXTRA_QUIZ = "extra_quiz"
    }
}

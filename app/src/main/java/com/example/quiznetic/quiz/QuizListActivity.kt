package com.example.quiznetic.quiz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.quiznetic.data.Quiz
import com.example.quiznetic.data.QuizCategory
import com.aunix.quiznetic.databinding.ActivityQuizListBinding
import com.example.quiznetic.utils.AdManager
import com.example.quiznetic.utils.SpacingItemDecoration

class QuizListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizListBinding
    private val viewModel: QuizListViewModel by viewModels()

    // Add activity result launcher to listen for quiz completion
    private val quizLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            
        }
    }

    private lateinit var quizAdapter: QuizAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityQuizListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load banner ad
        AdManager.loadBannerAd(this, binding.adContainer)

        setupRecyclerView()
        observeViewModel()

        // Set header text
        binding.tvQuizzesLeft.text = "Choose a Quiz Category"
        binding.tvResetTimer.text = "Select a category below to start a quiz"
        
        // Analyze cache first for debugging
        com.example.quiznetic.utils.QuizDataManager.analyzeCachedFile(this)
        
        // First, try to load cached quizzes
        viewModel.loadCachedQuizzes(this)
        
        // Then check for updates from GitHub
        viewModel.checkAndUpdateQuizzes(this)
    }

    private fun setupRecyclerView() {
        
        quizAdapter = QuizAdapter(emptyList()) { category ->
            fetchQuizzesForCategory(category)
        }

        binding.recyclerView.apply {
            adapter = quizAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            addItemDecoration(SpacingItemDecoration(43))
        }
        
        // Add refresh capability when clicking on header
        binding.cardQuizzesLeft.setOnClickListener {
            
            binding.tvResetTimer.text = "Refreshing quiz data..."
            viewModel.checkAndUpdateQuizzes(this)
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categoryList ->
            
            quizAdapter = QuizAdapter(categoryList) { category ->
                fetchQuizzesForCategory(category)
            }
            binding.recyclerView.adapter = quizAdapter
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.tvResetTimer.text = if (isLoading) {
                "Loading quiz data..."
            } else {
                "Select a category below to start a quiz"
            }
        }
        
        viewModel.error.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                binding.tvResetTimer.text = "Error loading quizzes. Pull down to refresh."
            }
        }
    }

    private fun fetchQuizzesForCategory(category: QuizCategory) {
        
        val selectedQuiz = viewModel.getQuizByCategory(category.name)
        if (selectedQuiz != null) {
            if (selectedQuiz.questions.isNotEmpty()) {
                startQuiz(selectedQuiz)
            } else {
                Toast.makeText(this, "No questions found for ${category.name} quiz", Toast.LENGTH_SHORT).show()
                // Try refreshing the data
                refreshQuizData()
            }
        } else {
            Toast.makeText(this, "No quizzes found for ${category.name}. Refreshing data...", Toast.LENGTH_SHORT).show()
            refreshQuizData()
        }
    }

    private fun refreshQuizData() {
        // Show loading indicator
        binding.tvResetTimer.text = "Refreshing quiz data..."
        
        // Force a reload from cache first
        viewModel.loadCachedQuizzes(this)
        
        // Then check for updates
        viewModel.checkAndUpdateQuizzes(this)
    }

    private fun startQuiz(quiz: Quiz) {
        if (quiz.questions.isEmpty() || quiz.questions.any { it.text.isNullOrEmpty() }) {
            Toast.makeText(this, "Quiz data is incomplete!", Toast.LENGTH_SHORT).show()
            return
        }

        
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_QUIZ, quiz)
        }
        // Use the launcher instead of startActivity to get the result
        quizLauncher.launch(intent)
    }
} 
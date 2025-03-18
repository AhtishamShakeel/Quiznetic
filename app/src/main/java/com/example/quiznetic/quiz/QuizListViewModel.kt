package com.example.quiznetic.quiz

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiznetic.R
import com.example.quiznetic.data.Quiz
import com.example.quiznetic.data.QuizCategory
import com.example.quiznetic.utils.QuizDataManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class QuizListViewModel : ViewModel() {
    private val _quizzes = MutableLiveData<List<Quiz>>()
    private val _isLoading = MutableLiveData<Boolean>(false)
    private val _error = MutableLiveData<String?>(null)

    val isLoading: LiveData<Boolean> = _isLoading
    val error: LiveData<String?> = _error

    // Define defaultCategories before using it
    private val defaultCategories = listOf(
        QuizCategory("Animals", R.raw.animal_quiz_animation, ""),
        QuizCategory("Sports", R.raw.sports_quiz_animation, ""),
        QuizCategory("Science", R.raw.science_quiz_animation, ""),
        QuizCategory("Riddles", R.raw.riddles_quiz_animation, ""),
        QuizCategory("Geography", R.raw.geography_quiz_animation, ""),
        QuizCategory("Math Fun", R.raw.math_quiz_animation, ""),
        QuizCategory("Video Games", R.raw.games_quiz_animation, ""),
        QuizCategory("GK", R.raw.general_quiz_animation, "")
    )

    private val _categories = MutableLiveData<List<QuizCategory>>().apply {
        value = defaultCategories
    }

    val categories: LiveData<List<QuizCategory>> = _categories

    init {
        _categories.value = defaultCategories  // Ensure categories load immediately
    }

    fun loadCachedQuizzes(context: Context) {
        Log.d("QuizDebug", "Loading quizzes from cache...")
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val json = QuizDataManager.loadCachedQuizzes(context)
            
            if (!json.isNullOrEmpty()) {
                try {
                    Log.d("QuizDebug", "Parsing cached JSON data...")
                    val quizData = Gson().fromJson(json, QuizData::class.java)
                    
                    Log.d("QuizDebug", "Successfully parsed JSON. Categories: ${quizData.categories.size}")
                    for (category in quizData.categories) {
                        Log.d("QuizDebug", "Category: ${category.name}, Quizzes: ${category.quizzes.size}")
                        for (quiz in category.quizzes) {
                            Log.d("QuizDebug", "  Quiz: ${quiz.id}, Questions: ${quiz.questions.size}")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        // Map cached categories to our default categories to maintain consistent images
                        _categories.value = quizData.categories.map { category ->
                            defaultCategories.find { it.name.equals(category.name, ignoreCase = true) }
                                ?: QuizCategory(category.name, R.raw.default_quiz_animation, "")
                        }
                        
                        // Create quiz list with proper category names
                        val allQuizzes = mutableListOf<Quiz>()
                        quizData.categories.forEach { category ->
                            category.quizzes.forEach { quiz ->
                                // Use the quiz's ID and questions, but set the title to the category name
                                val newQuiz = Quiz(
                                    id = quiz.id,
                                    title = category.name,
                                    questions = quiz.questions
                                )
                                allQuizzes.add(newQuiz)
                                Log.d("QuizDebug", "Added quiz ${quiz.id} to category ${category.name}")
                            }
                        }
                        
                        _quizzes.value = allQuizzes
                        Log.d("QuizDebug", "Total quizzes loaded: ${allQuizzes.size}")
                        
                        // Debug each quiz
                        allQuizzes.forEach { quiz ->
                            Log.d("QuizDebug", "Loaded Quiz - ID: ${quiz.id}, Title: ${quiz.title}, Questions: ${quiz.questions.size}")
                        }
                        
                        // Debug which categories have quizzes
                        val categoriesWithQuizzes = allQuizzes.map { it.title }.toSet()
                        Log.d("QuizDebug", "Categories with quizzes: $categoriesWithQuizzes")
                        
                        _isLoading.value = false
                    }
                } catch (e: JsonSyntaxException) {
                    Log.e("QuizDebug", "Error parsing JSON: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        _categories.value = defaultCategories
                        _error.value = "Error parsing quiz data. Please try again."
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    Log.e("QuizDebug", "Error processing quiz data: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        _categories.value = defaultCategories
                        _error.value = "Error loading quiz data. Please try again."
                        _isLoading.value = false
                    }
                }
            } else {
                Log.d("QuizDebug", "No cached quizzes found. Fetching from GitHub...")
                withContext(Dispatchers.Main) {
                    _categories.value = defaultCategories
                    _isLoading.value = false
                    // Attempt to fetch from GitHub when cache is empty
                    checkAndUpdateQuizzes(context)
                }
            }
        }
    }

    fun checkAndUpdateQuizzes(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = true
            Log.d("QuizDebug", "Checking for quiz updates...")
        }
        
        QuizDataManager.fetchQuizzesFromGitHub(context) { isUpdated ->
            viewModelScope.launch(Dispatchers.Main) {
                if (isUpdated) {
                    Log.d("QuizDebug", "Quizzes updated, reloading from cache")
                    loadCachedQuizzes(context)
                } else {
                    Log.d("QuizDebug", "No updates detected, still loading cache to ensure data is available")
                    // Check if quizzes are already loaded
                    if (_quizzes.value.isNullOrEmpty()) {
                        loadCachedQuizzes(context)
                    } else {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun getQuizByCategory(categoryName: String): Quiz? {
        // Log all quiz titles to check what we have
        Log.d("QuizDebug", "All quiz titles: ${_quizzes.value?.map { it.title } ?: "No quizzes loaded"}")
        
        // Do case-insensitive matching
        val categoryQuizzes = _quizzes.value?.filter { 
            it.title.equals(categoryName, ignoreCase = true) 
        }

        Log.d("QuizDebug", "Searching for quizzes in category: $categoryName")
        Log.d("QuizDebug", "Found ${categoryQuizzes?.size ?: 0} quizzes in this category")

        val validQuizzes = categoryQuizzes?.filter { quiz ->
            quiz.questions.isNotEmpty() && quiz.questions.all { !it.text.isNullOrEmpty() }
        }

        Log.d("QuizDebug", "Valid quizzes count: ${validQuizzes?.size ?: 0}")

        return if (!validQuizzes.isNullOrEmpty()) {
            val selectedQuiz = validQuizzes.random()
            Log.d("QuizDebug", "Selected quiz: ${selectedQuiz.id} with ${selectedQuiz.questions.size} questions")
            selectedQuiz
        } else {
            Log.d("QuizDebug", "No valid quizzes found for category: $categoryName")
            null
        }
    }
}

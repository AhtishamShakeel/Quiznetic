package com.example.quiznetic.quiz

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aunix.quiznetic.R
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
        
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val json = QuizDataManager.loadCachedQuizzes(context)
            
            if (!json.isNullOrEmpty()) {
                try {
                    
                    val quizData = Gson().fromJson(json, QuizData::class.java)
                    
                    
                    for (category in quizData.categories) {
                        
                        for (quiz in category.quizzes) {
                            
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
                                
                            }
                        }
                        
                        _quizzes.value = allQuizzes
                        
                        
                        // Debug each quiz
                        allQuizzes.forEach { quiz ->
                            
                        }
                        
                        // Debug which categories have quizzes
                        val categoriesWithQuizzes = allQuizzes.map { it.title }.toSet()
                        
                        
                        _isLoading.value = false
                    }
                } catch (e: JsonSyntaxException) {
                    
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        _categories.value = defaultCategories
                        _error.value = "Error parsing quiz data. Please try again."
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        _categories.value = defaultCategories
                        _error.value = "Error loading quiz data. Please try again."
                        _isLoading.value = false
                    }
                }
            } else {
                
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
            
        }
        
        QuizDataManager.fetchQuizzesFromGitHub(context) { isUpdated ->
            viewModelScope.launch(Dispatchers.Main) {
                if (isUpdated) {
                    
                    loadCachedQuizzes(context)
                } else {
                    
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
        
        
        // Do case-insensitive matching
        val categoryQuizzes = _quizzes.value?.filter { 
            it.title.equals(categoryName, ignoreCase = true) 
        }

        
        

        val validQuizzes = categoryQuizzes?.filter { quiz ->
            quiz.questions.isNotEmpty() && quiz.questions.all { !it.text.isNullOrEmpty() }
        }

        

        return if (!validQuizzes.isNullOrEmpty()) {
            val selectedQuiz = validQuizzes.random()
            
            selectedQuiz
        } else {
            
            null
        }
    }
}

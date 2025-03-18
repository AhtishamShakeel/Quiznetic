package com.example.quiznetic.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.example.quiznetic.quiz.QuizData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit

object QuizDataManager {
    private const val GITHUB_URL = "https://raw.githubusercontent.com/AhtishamShakeel/quizzes/refs/heads/main/quizzes.json"
    private const val CACHE_FILE_NAME = "quizzes.json"
    private const val VERSION_PREFS = "quiz_version"

    // Create OkHttpClient with longer timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ✅ Fetch JSON from GitHub
    fun fetchQuizzesFromGitHub(context: Context, onComplete: (Boolean) -> Unit) {
        Log.d("QuizDebug", "Fetching quizzes from GitHub URL: $GITHUB_URL")

        // Use CoroutineScope instead of raw Thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(GITHUB_URL).build()
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    Log.e("QuizDebug", "Error response: ${response.code} - ${response.message}")
                    onComplete(false)
                    return@launch
                }
                
                val json = response.body?.string()

                if (json != null && json.isNotEmpty()) {
                    Log.d("QuizDebug", "Received JSON data (first 100 chars): ${json.take(100)}...")
                    
                    try {
                        // Parse the JSON to get the version
                        val quizData = Gson().fromJson(json, QuizData::class.java)
                        Log.d("QuizDebug", "Parsed JSON successfully, version: ${quizData.version}, categories: ${quizData.categories.size}")
                        
                        val newVersion = quizData.version
                        val currentVersion = getCachedVersion(context)

                        Log.d("QuizDebug", "Current version: $currentVersion, New version: $newVersion")

                        if (newVersion > currentVersion) {
                            // New version available, update cache
                            saveJsonToCache(context, json)
                            saveVersionToCache(context, newVersion)
                            Log.d("QuizDebug", "Updated to new version: $newVersion")
                            onComplete(true)
                        } else {
                            Log.d("QuizDebug", "Using cached version: $currentVersion")
                            onComplete(false)
                        }
                    } catch (e: Exception) {
                        Log.e("QuizDebug", "JSON parsing error: ${e.message}")
                        e.printStackTrace()
                        // Even if parsing fails, save the raw JSON for debugging
                        saveJsonToCache(context, json)
                        onComplete(false)
                    }
                } else {
                    Log.d("QuizDebug", "GitHub response was empty!")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("QuizDebug", "Error fetching quizzes: ${e.message}")
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    // ✅ Check if cached version exists
    private fun isCachedVersionAvailable(context: Context): Boolean {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        val exists = file.exists() && file.length() > 0 && getCachedVersion(context) > 0
        Log.d("QuizDebug", "Cache available: $exists")
        return exists
    }

    // ✅ Load JSON from local storage
    fun loadCachedQuizzes(context: Context): String? {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        return if (file.exists() && file.length() > 0) {
            val content = file.readText()
            Log.d("QuizDebug", "Loaded cached JSON (first 100 chars): ${content.take(100)}...")
            content
        } else {
            Log.d("QuizDebug", "No cached file found or file is empty")
            null
        }
    }

    // ✅ Save JSON to local storage
    private fun saveJsonToCache(context: Context, json: String) {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            FileWriter(file).use { it.write(json) }
            Log.d("QuizDebug", "Saved JSON to cache: ${file.absolutePath}, size: ${json.length} chars")
        } catch (e: Exception) {
            Log.e("QuizDebug", "Error saving JSON to cache: ${e.message}")
            e.printStackTrace()
        }
    }

    // ✅ Get cached version number
    private fun getCachedVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(VERSION_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt("version", 0)
    }

    // ✅ Save new version number
    private fun saveVersionToCache(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(VERSION_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt("version", version).apply()
    }

    // Debug function to analyze cached file
    fun analyzeCachedFile(context: Context) {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (file.exists() && file.length() > 0) {
            try {
                val content = file.readText()
                val quizData = Gson().fromJson(content, QuizData::class.java)
                
                Log.d("QuizDebug", "==== CACHE ANALYSIS ====")
                Log.d("QuizDebug", "Cache file size: ${content.length} chars")
                Log.d("QuizDebug", "Version: ${quizData.version}")
                Log.d("QuizDebug", "Categories (${quizData.categories.size}): ${quizData.categories.map { it.name }}")
                
                quizData.categories.forEach { category ->
                    Log.d("QuizDebug", "Category '${category.name}' has ${category.quizzes.size} quizzes")
                    category.quizzes.forEach { quiz ->
                        Log.d("QuizDebug", "  - Quiz ID: ${quiz.id}, Questions: ${quiz.questions.size}")
                    }
                }
                
                // Check for science category specifically
                val scienceCategory = quizData.categories.find { it.name.equals("Science", ignoreCase = true) }
                if (scienceCategory != null) {
                    Log.d("QuizDebug", "Science category exists with ${scienceCategory.quizzes.size} quizzes")
                } else {
                    Log.d("QuizDebug", "Science category does not exist in cache")
                }
                
                Log.d("QuizDebug", "==== END ANALYSIS ====")
            } catch (e: Exception) {
                Log.e("QuizDebug", "Error analyzing cache: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.d("QuizDebug", "No cache file exists or it's empty")
        }
    }
}

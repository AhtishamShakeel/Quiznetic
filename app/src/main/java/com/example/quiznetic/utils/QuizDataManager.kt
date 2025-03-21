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
        

        // Use CoroutineScope instead of raw Thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(GITHUB_URL).build()
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    
                    onComplete(false)
                    return@launch
                }
                
                val json = response.body?.string()

                if (json != null && json.isNotEmpty()) {
                    
                    try {
                        // Parse the JSON to get the version
                        val quizData = Gson().fromJson(json, QuizData::class.java)
                        
                        
                        val newVersion = quizData.version
                        val currentVersion = getCachedVersion(context)

                        

                        if (newVersion > currentVersion) {
                            // New version available, update cache
                            saveJsonToCache(context, json)
                            saveVersionToCache(context, newVersion)
                            
                            onComplete(true)
                        } else {
                            
                            onComplete(false)
                        }
                    } catch (e: Exception) {
                        
                        e.printStackTrace()
                        // Even if parsing fails, save the raw JSON for debugging
                        saveJsonToCache(context, json)
                        onComplete(false)
                    }
                } else {
                    
                    onComplete(false)
                }
            } catch (e: Exception) {
                
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    // ✅ Check if cached version exists
    private fun isCachedVersionAvailable(context: Context): Boolean {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        val exists = file.exists() && file.length() > 0 && getCachedVersion(context) > 0
        
        return exists
    }

    // ✅ Load JSON from local storage
    fun loadCachedQuizzes(context: Context): String? {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        return if (file.exists() && file.length() > 0) {
            val content = file.readText()

            content
        } else {
            
            null
        }
    }

    // ✅ Save JSON to local storage
    private fun saveJsonToCache(context: Context, json: String) {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            FileWriter(file).use { it.write(json) }
            
        } catch (e: Exception) {
            
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
                quizData.categories.forEach { category ->
                    
                    category.quizzes.forEach { quiz ->
                        
                    }
                }
                
                // Check for science category specifically
                val scienceCategory = quizData.categories.find { it.name.equals("Science", ignoreCase = true) }
                if (scienceCategory != null) {
                    
                } else {
                    
                }
                
                
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
        } else {
            
        }
    }
}

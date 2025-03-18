package com.example.quiznetic.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiznetic.R
import com.example.quiznetic.data.QuizCategory

class QuizListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_list)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        // Sample categories (replace with real data later)
        val sampleCategories = listOf(
            QuizCategory("General Knowledge", R.raw.general_quiz_animation, ""),
            QuizCategory("Science Quiz", R.raw.science_quiz_animation, ""),
            QuizCategory("Math Quiz", R.raw.math_quiz_animation, "")
        )

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = QuizListAdapter(this, sampleCategories) { category ->
            Toast.makeText(this, "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
            startQuiz(category)
        }
        recyclerView.adapter = adapter
    }

    private fun startQuiz(category: QuizCategory) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("CATEGORY_NAME", category.name) // Pass category name for filtering
        startActivity(intent)
    }
}

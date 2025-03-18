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

        // Set up RecyclerView asasasassasa
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

package com.example.quiznetic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.quiznetic.quiz.QuizListActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val getStartedButton: Button = findViewById(R.id.getStartedButton)

        getStartedButton.setOnClickListener {
            Log.d("MainActivity", "Get Started button clicked, launching QuizListActivity")
            val intent = Intent(this, QuizListActivity::class.java)
            startActivity(intent)
        }
    }
}

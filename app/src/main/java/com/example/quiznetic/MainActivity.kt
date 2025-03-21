package com.example.quiznetic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.aunix.quiznetic.R
import com.example.quiznetic.quiz.QuizListActivity
import com.example.quiznetic.utils.AdManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize the Ad Manager
        AdManager.initialize(this)
        
        val getStartedButton: Button = findViewById(R.id.getStartedButton)

        getStartedButton.setOnClickListener {
            
            val intent = Intent(this, QuizListActivity::class.java)
            startActivity(intent)
        }
        
        // Set up privacy policy button
        val privacyPolicyButton: TextView = findViewById(R.id.privacyPolicyButton)
        privacyPolicyButton.setOnClickListener {
            
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }
}

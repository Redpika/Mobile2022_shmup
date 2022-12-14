package com.example.shmupproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val score = findViewById<TextView>(R.id.score)

        val sharedPreferences = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)
        var highScore = sharedPreferences.getInt("HIGH_SCORE", 0)

        score.text = "High Score: " + highScore

        val startButton = findViewById<Button>(R.id.start_button)
        startButton?.setOnClickListener{
            val intent =  Intent(this, GameActivity::class.java)

            startActivity(intent)

            score.text = "High Score: " + highScore
        }
    }
}
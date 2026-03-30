package com.sala.patupi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        
        val tvWelcomeName = findViewById<TextView>(R.id.tvWelcomeName)
        val name = intent.getStringExtra("USER_NAME")


        tvWelcomeName.text = if (!name.isNullOrEmpty()) "Welcome, $name" else "Welcome back!"
    }
}
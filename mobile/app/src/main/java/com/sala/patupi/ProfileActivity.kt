package com.sala.patupi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvInitial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvName = findViewById(R.id.tvProfileFullName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvInitial = findViewById(R.id.tvUserInitial)

        loadUserData()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<TextView>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("user", null)

        // Debug Log to see exactly what's in storage
        Log.d("PATUPI_DEBUG", "Loaded Prefs: $userJson")

        if (userJson != null) {
            try {
                val user = JSONObject(userJson)
                val fullName = user.optString("fullName", "User")
                tvName.text = fullName
                tvEmail.text = user.optString("email", "")
                tvInitial.text = fullName.take(1).uppercase()
            } catch (e: Exception) {
                Log.e("PATUPI_DEBUG", "JSON Parsing error: ${e.message}")
                redirectToLogin()
            }
        } else {
            Log.w("PATUPI_DEBUG", "No user found in Prefs - Redirecting to Login")
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        // Clear stack so user can't "Back" into the empty profile
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Ready to head out?")
            .setPositiveButton("Logout") { _, _ ->
                val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove("user")
                    apply()
                }
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Stay", null)
            .show()
    }
}
package com.sala.patupi

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView

    // Ensure this IP matches your current machine IP (192.168.1.9)
    private val loginUrl = "http://192.168.1.2:8080/api/auth/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- SPOT 1: NEW IN-MEMORY AUTO-LOGIN CHECK ---
        // SharedPreferences check completely removed. Now it checks RAM.
        // When the app closes, this automatically clears.
        if (SessionManager.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        setupSignUpSpan()
        btnLogin.setOnClickListener { loginUser() }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupSignUpSpan() {
        val text = "Don't have an account? Sign up"
        val ss = SpannableString(text)
        val start = text.indexOf("Sign up")

        if (start != -1) {
            val end = start + "Sign up".length
            val goldColor = ContextCompat.getColor(this, R.color.patupi_gold)
            ss.setSpan(ForegroundColorSpan(goldColor), start, end, 0)
            ss.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
        }
        tvSignUp.text = ss
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your credentials")
            return
        }

        val loginData = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(Request.Method.POST, loginUrl, loginData,
            { response ->
                // --- SPOT 2: SAVE TO RAM INSTEAD OF DISK ---
                saveUserSession(response)
                showSuccessDialog(response)
            },
            { error ->
                Log.e("PATUPI_DEBUG", "Login Failed: $error")
                val status = error.networkResponse?.statusCode
                val message = when(status) {
                    401 -> "Invalid email or password."
                    else -> "Cannot connect to server at $loginUrl. Check your IP/Firewall."
                }
                showError(message)
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    // --- SPOT 3: REWRITTEN TO USE SESSION MANAGER ---
    private fun saveUserSession(response: JSONObject) {
        // Stores the incoming full user data response block safely inside your RAM container
        SessionManager.currentUserJson = response
        SessionManager.authToken = response.optString("token", "dummy_token")
        SessionManager.userEmail = response.optString("email", "")

        Log.d("PATUPI_DEBUG", "Session saved to temporary memory (RAM)")
    }

    private fun showSuccessDialog(response: JSONObject) {
        val name = response.optString("fullName", "User")
        // Handle fallback parsing rules standard inside your project mapping structure
        val roleId = response.optInt("roleId", 3) 

        if (roleId == 1) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Access Restricted")
                .setMessage("Admin Page only accessible through web browser.")
                .setPositiveButton("Understood", null)
                .show()
            SessionManager.logout() // Flush temporary cache memory container instantly
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Login Successful")
            .setMessage("Welcome back to Patupi, $name!")
            .setPositiveButton("Let's Go") { _, _ ->
                val intent = when (roleId) {
                    2 -> Intent(this, BarberPageActivity::class.java)
                    else -> Intent(this, DashboardActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Login Error")
            .setMessage(message)
            .setPositiveButton("Try Again", null)
            .show()
    }
}
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
    private val loginUrl = "http://192.168.1.4:8080/api/auth/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Views - IDs match activity_login.xml
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        // Make "Sign up" text gold and bold
        setupSignUpSpan()

        btnLogin.setOnClickListener { loginUser() }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSignUpSpan() {
        val text = "Don't have an account? Sign up"
        val ss = SpannableString(text)
        val start = text.indexOf("Sign up")

        if (start != -1) {
            val end = start + "Sign up".length
            val goldColor = ContextCompat.getColor(this, R.color.patupi_gold)

            // Apply Gold Color
            ss.setSpan(ForegroundColorSpan(goldColor), start, end, 0)
            // Apply Bold Style
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
                // SUCCESS POPUP
                showSuccessDialog(response)
            },
            { error ->
                Log.e("PATUPI_DEBUG", "Login Failed: $error")
                val status = error.networkResponse?.statusCode
                val message = when(status) {
                    401 -> "Invalid email or password."
                    else -> "Cannot connect to server. Check your connection."
                }
                showError(message)
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun showSuccessDialog(response: JSONObject) {
        val name = response.optString("fullName", "User")

        MaterialAlertDialogBuilder(this)
            .setTitle("Login Successful")
            .setMessage("Welcome back to Patupi, $name!")
            .setPositiveButton("Go to Dashboard") { _, _ ->
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_NAME", name)
                startActivity(intent)
                finish() // Prevents user from going back to login screen
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
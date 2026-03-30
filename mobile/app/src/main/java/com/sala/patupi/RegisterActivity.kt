package com.sala.patupi

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginNow: TextView

    private val url = "http://192.168.1.4:8080/api/auth/register"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etLocation = findViewById(R.id.etLocation)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginNow = findViewById(R.id.tvLogin)

        // The critical fix: ensuring we find the ImageButton correctly
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        setupLoginNowSpan()

        btnRegister.setOnClickListener { registerUser() }
        tvLoginNow.setOnClickListener { finish() }
    }

    private fun setupLoginNowSpan() {
        val text = "Already have an account? Login Now"
        val ss = SpannableString(text)
        val start = text.indexOf("Login Now")
        if (start != -1) {
            val end = start + "Login Now".length
            val goldColor = ContextCompat.getColor(this, R.color.patupi_gold)
            ss.setSpan(ForegroundColorSpan(goldColor), start, end, 0)
            ss.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
        }
        tvLoginNow.text = ss
    }

    private fun registerUser() {
        val name = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || location.isEmpty() || password.isEmpty()) {
            showErrorDialog("Please fill out all fields.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorDialog("Please enter a valid email address.")
            return
        }

        if (password != confirmPassword) {
            showErrorDialog("Passwords do not match!")
            return
        }

        sendRequest(name, email, location, password)
    }

    private fun sendRequest(name: String, email: String, loc: String, pass: String) {
        val postData = JSONObject().apply {
            put("fullName", name)
            put("email", email)
            put("address", loc)
            put("password", pass)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, postData,
            { showSuccessDialog() },
            { error ->
                Log.e("PATUPI_DEBUG", "Error: $error")
                showErrorDialog("Registration failed. Check connection or try a different email.")
            }
        )

        request.retryPolicy = DefaultRetryPolicy(10000, 1, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage("Account created for Patupi!")
            .setPositiveButton("Login Now") { _, _ -> finish() }
            .show()
    }

    private fun showErrorDialog(msg: String) {
        MaterialAlertDialogBuilder(this).setMessage(msg).setPositiveButton("OK", null).show()
    }
}
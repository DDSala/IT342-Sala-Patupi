package com.sala.patupi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvInitial: TextView

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText

    private lateinit var btnEditProfile: TextView
    private lateinit var btnCancelEdit: TextView
    private lateinit var btnUpdatePassword: MaterialButton

    private var isEditing = false

    private var currentUserJson: JSONObject?
        get() = SessionManager.currentUserJson
        set(value) { SessionManager.currentUserJson = value }

    private val apiBase = "http://192.168.1.2:8080/api/users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupListeners()
        loadUserData()
    }

    private fun initializeViews() {
        tvName = findViewById(R.id.tvProfileFullName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvAddress = findViewById(R.id.tvProfileAddress)
        tvInitial = findViewById(R.id.tvUserInitial)

        etName = findViewById(R.id.etProfileFullName)
        etEmail = findViewById(R.id.etProfileEmail)
        etAddress = findViewById(R.id.etProfileAddress)

        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        btnUpdatePassword = findViewById(R.id.updatePwdButton)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            if (isEditing) handleCancelEdit() else finish()
        }

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }

        btnEditProfile.setOnClickListener {
            if (!isEditing) {
                handleEditToggle()
            } else {
                handleSaveProfile()
            }
        }

        btnCancelEdit.setOnClickListener {
            handleCancelEdit()
        }

        btnUpdatePassword.setOnClickListener {
            showPasswordModalDialog()
        }
    }

    private fun loadUserData() {
        val userObj = currentUserJson
        if (userObj != null) {
            try {
                populateUiFields(userObj)
            } catch (e: Exception) {
                Log.e("PATUPI_PROFILE", "Parsing layout fields problem: ${e.message}")
            }
        } else {
            Log.e("PATUPI_PROFILE", "Session token or JSON missing inside memory.")
            redirectToLogin()
        }
    }

    private fun populateUiFields(user: JSONObject) {
        val fullName = user.optString("fullName", "User")
        tvName.text = fullName
        tvEmail.text = user.optString("email", "")

        val addressData = user.optString("address", "")
        tvAddress.text = if (addressData.isEmpty() || addressData == "null") "No Address Set" else addressData

        tvInitial.text = if (fullName.isNotEmpty()) fullName.take(1).uppercase(Locale.getDefault()) else "U"

        etName.setText(fullName)
        etEmail.setText(user.optString("email", ""))
        etAddress.setText(if (addressData == "null") "" else addressData)
    }

    private fun handleEditToggle() {
        isEditing = true
        btnEditProfile.text = "Save Changes"
        btnCancelEdit.visibility = View.VISIBLE

        tvName.visibility = View.GONE
        tvEmail.visibility = View.GONE
        tvAddress.visibility = View.GONE

        etName.visibility = View.VISIBLE
        etEmail.visibility = View.VISIBLE
        etAddress.visibility = View.VISIBLE
    }

    private fun handleCancelEdit() {
        isEditing = false
        btnEditProfile.text = "Edit Profile"
        btnCancelEdit.visibility = View.GONE

        etName.visibility = View.GONE
        etEmail.visibility = View.GONE
        etAddress.visibility = View.GONE

        tvName.visibility = View.VISIBLE
        tvEmail.visibility = View.VISIBLE
        tvAddress.visibility = View.VISIBLE

        currentUserJson?.let { populateUiFields(it) }
    }

    private fun handleSaveProfile() {
        val userObj = currentUserJson ?: return
        val userId = userObj.optInt("userId", userObj.optInt("id", -1))

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUserPayload = JSONObject().apply {
            put("userId", userId)
            put("fullName", etName?.text.toString().trim())
            put("email", etEmail?.text.toString().trim())
            put("address", etAddress?.text.toString().trim())

            if (userObj.has("roleId") && !userObj.isNull("roleId")) {
                put("roleId", userObj.optInt("roleId"))
            } else {
                put("roleId", JSONObject.NULL)
            }
        }

        // Lock UI during transaction
        btnEditProfile?.text = "Saving..."
        btnEditProfile?.isEnabled = false

        val request = JsonObjectRequest(Request.Method.PUT, "$apiBase/$userId", updatedUserPayload,
            { response ->
                // SUCCESS CALLBACK
                btnEditProfile?.isEnabled = true
                btnEditProfile?.text = "Edit Profile" // Reset button text
                currentUserJson = response

                isEditing = false
                btnCancelEdit?.visibility = View.GONE
                populateUiFields(response)

                etName?.visibility = View.GONE
                etEmail?.visibility = View.GONE
                etAddress?.visibility = View.GONE

                tvName?.visibility = View.VISIBLE
                tvEmail?.visibility = View.VISIBLE
                tvAddress?.visibility = View.VISIBLE

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // FAILURE CALLBACK (Prevents getting stuck on "Saving...")
                btnEditProfile?.isEnabled = true
                btnEditProfile?.text = "Save Changes"

                val response = error.networkResponse
                val statusCode = response?.statusCode

                if (response != null) {
                    Log.e("PATUPI_PROFILE", "Server Error Status Code: $statusCode")
                    try {
                        val serverBody = String(response.data)
                        Log.e("PATUPI_PROFILE", "Server Error Body: $serverBody")
                    } catch (_: Exception) {}
                } else {
                    Log.e("PATUPI_PROFILE", "Network Error / Timeout: ${error.message}")
                }

                // Provide a clear context message to the user
                val errorMsg = when (statusCode) {
                    400 -> "Bad Request (400): Check data payload alignment."
                    404 -> "Not Found (404): Endpoint URL is incorrect."
                    500 -> "Internal Server Error (500): Backend crashed."
                    else -> "Network issue or connection timeout."
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun showPasswordModalDialog() {
        val userObj = currentUserJson ?: return
        val userId = userObj.optInt("userId", userObj.optInt("id", -1))

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val etCurrentPwd = EditText(this).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etNewPwd = EditText(this).apply {
            hint = "New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etConfirmPwd = EditText(this).apply {
            hint = "Confirm New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        container.addView(etCurrentPwd)
        container.addView(etNewPwd)
        container.addView(etConfirmPwd)

        AlertDialog.Builder(this)
            .setTitle("Update Password")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Update") { dialog, _ ->
                val current = etCurrentPwd.text.toString()
                val new = etNewPwd.text.toString()
                val confirm = etConfirmPwd.text.toString()

                if (new != confirm) {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val payload = JSONObject().apply {
                    put("currentPassword", current)
                    put("newPassword", new)
                }

                val req = JsonObjectRequest(Request.Method.PUT, "$apiBase/$userId/password", payload,
                    { _ ->
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    },
                    { error ->
                        val resp = error.networkResponse
                        if (resp != null && resp.data != null) {
                            try {
                                val errJson = JSONObject(String(resp.data))
                                Toast.makeText(this, errJson.optString("message", "Incorrect current password."), Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {
                                Toast.makeText(this, "Incorrect current password.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Incorrect current password.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ).apply {
                    retryPolicy = DefaultRetryPolicy(
                        5000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
                }
                Volley.newRequestQueue(this).add(req)
            }
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Ready to head out?")
            .setPositiveButton("Logout") { _, _ ->
                SessionManager.logout()
                redirectToLogin()
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
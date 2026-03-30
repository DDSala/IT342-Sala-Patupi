package com.sala.patupi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private final String url = "http://192.168.1.4:8080/api/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        setupSignUpSpan();

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }
    }

    private void setupSignUpSpan() {
        String fullText = "Don't have an account? Sign up";
        SpannableString ss = new SpannableString(fullText);
        int goldColor = ContextCompat.getColor(this, R.color.patupi_gold);

        int start = fullText.indexOf("Sign up");
        int end = start + "Sign up".length();

        if (start != -1) {
            ss.setSpan(new ForegroundColorSpan(goldColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvSignUp.setText(ss);
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("email", email);
            loginData.put("password", password);
        } catch (JSONException e) {
            Log.e("PATUPI_LOGIN", "JSON Error: " + e.getMessage());
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, loginData,
                response -> {
                    String userName = response.optString("fullName", "User");
                    showWelcomeDialog(userName);
                },
                error -> {
                    String errorMessage = "Login Failed. Check Connection.";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        errorMessage = "Invalid Email or Password";
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        Volley.newRequestQueue(this).add(request);
    }

    private void showWelcomeDialog(String name) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Welcome Back!")
                .setMessage("Ready for your next premium cut?")
                .setPositiveButton("Let's Go", (dialog, which) -> {

                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_NAME", name);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
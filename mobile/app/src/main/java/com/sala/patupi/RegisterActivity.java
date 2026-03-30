package com.sala.patupi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etLocation, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginNow;
    private final String url = "http://192.168.1.4:8080/api/auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLogin);

        setupLoginNowSpan();

        btnRegister.setOnClickListener(v -> registerUser());

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        tvLoginNow.setOnClickListener(v -> finish());
    }

    private void setupLoginNowSpan() {
        String text = "Already have an account? Login Now";
        SpannableString ss = new SpannableString(text);

        int start = text.indexOf("Login Now");
        int end = start + "Login Now".length();

        if (start != -1) {
            int goldColor = ContextCompat.getColor(this, R.color.patupi_gold);
            ss.setSpan(new ForegroundColorSpan(goldColor), start, end, 0);
            ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
        }
        tvLoginNow.setText(ss);
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || location.isEmpty() || password.isEmpty()) {
            showErrorDialog("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorDialog("Passwords do not match!");
            return;
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("fullName", name);
            postData.put("email", email);
            postData.put("address", location);
            postData.put("password", password);
        } catch (JSONException e) {
            Log.e("PATUPI_DEBUG", "JSON Error", e);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> showSuccessDialog(),
                error -> {
                    String message = "Connection Timeout. Check your Laptop IP and Firewall.";
                    if (error.networkResponse != null) {
                        message = "Error: " + error.networkResponse.statusCode + "\nEmail might already be taken.";
                    }
                    showErrorDialog(message);
                    Log.e("PATUPI_DEBUG", "Volley Error: " + error.toString());
                }
        );


        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Success!")
                .setMessage("Your Patupi account is ready.\n\nLogin to start booking.")
                .setPositiveButton("Login Now", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Registration Failed")
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }
}
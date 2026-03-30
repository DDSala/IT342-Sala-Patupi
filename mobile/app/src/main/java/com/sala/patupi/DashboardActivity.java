package com.sala.patupi;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvWelcomeName = findViewById(R.id.tvWelcomeName);

        String name = getIntent().getStringExtra("USER_NAME");

        if (name != null && !name.isEmpty()) {
            tvWelcomeName.setText("Welcome, " + name);
        } else {
            tvWelcomeName.setText("Welcome back!");
        }
    }
}
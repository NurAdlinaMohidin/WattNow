package com.example.wattnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView textWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialise Firebase authenticatopm
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Welcome text view
        textWelcome = findViewById(R.id.textTitle);

        // If user is logged in, greet with their email
        if (user != null) {
            textWelcome.setText("Welcome, " + user.getEmail() + "!");
        }

        // Logout button
        ImageButton buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Navigation buttons
        Button buttonCalculate = findViewById(R.id.buttonCalculate);
        Button buttonList = findViewById(R.id.buttonList);
        Button buttonAbout = findViewById(R.id.buttonAbout);

        buttonCalculate.setOnClickListener(v ->
                startActivity(new Intent(this, CalculationActivity.class)));

        buttonList.setOnClickListener(v ->
                startActivity(new Intent(this, ListActivity.class)));

        buttonAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
    }

    // Confirmation dialog before logging out
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

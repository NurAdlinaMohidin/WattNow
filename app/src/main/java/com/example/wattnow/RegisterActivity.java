package com.example.wattnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);

        // Initialise Firebase authentication
        mAuth = FirebaseAuth.getInstance();
    }

    public void createUser(View v) {
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        // Input validation:
        if (email.isEmpty() || password.isEmpty()) {
            // Empty fields
            Toast.makeText(getApplicationContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (!email.contains("@")) {
            // Invalid email
            Toast.makeText(getApplicationContext(), "Please enter a valid email address with '@'", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            // Weak password
            Toast.makeText(getApplicationContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Registration successful
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                // Registration failed
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "This email is already registered", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

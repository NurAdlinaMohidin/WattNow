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

public class LoginActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        e1 = findViewById(R.id.editTextEmail);
        e2 = findViewById(R.id.editTextPassword);

        // Firebase authentication
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(View v) {
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        // Input validation: fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fields cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase login process
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Input validation: email and password
                            Toast.makeText(getApplicationContext(), "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Navigate to RegisterActivity
    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

}
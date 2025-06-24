package com.example.pickupsampah;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pickupsampah.helpers.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends BaseActivity {

    private EditText TxtEmail,TxtPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();
        TxtEmail = findViewById(R.id.RegEmail);
        TxtPassword = findViewById(R.id.RegPassword);
        Button buttonReg = findViewById(R.id.RegBtn);
        progressBar = findViewById(R.id.progressBar);

        buttonReg.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(TxtEmail.getText());
            password = String.valueOf(TxtPassword.getText());

            if (TextUtils.isEmpty((email))){
                Toast.makeText(RegisterActivity.this, "Enter Email",Toast.LENGTH_SHORT).show();

            }
            if (TextUtils.isEmpty((password))){
                Toast.makeText(RegisterActivity.this, "Enter Password",Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Account created.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
    public void callLandingActivity(View view) {
        Intent intent = new Intent(RegisterActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }
    public void goToLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
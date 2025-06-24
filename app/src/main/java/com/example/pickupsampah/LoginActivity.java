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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {
    private EditText TxtEmail,TxtPassword;
    ProgressBar progressBar;
    private Button buttonLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        TxtEmail = findViewById(R.id.TxtEmail);
        TxtPassword = findViewById(R.id.TxtPass);
        buttonLogin = findViewById(R.id.LgnBtn);
        progressBar = findViewById(R.id.progressBar);

    buttonLogin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(TxtEmail.getText());
            password = String.valueOf(TxtPassword.getText());
            if (TextUtils.isEmpty((email))) {
                Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty((password))) {
                Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    });
    }
    public void callLandingActivity(View view) {
        Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }
    public void goToRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
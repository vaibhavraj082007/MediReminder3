package com.example.medireminder3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText etEmail, etPassword;
    TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // already logged in? skip this screen
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);

        // ---------- FORGOT PASSWORD ----------
        findViewById(R.id.tvForgot).setOnClickListener(v -> {
            hideError();
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                showError("Type your email first, then tap Forgot password.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("This email address is not valid.");
                return;
            }
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(r -> Toast.makeText(this,
                            "Reset link sent to " + email + ". Check inbox and spam.",
                            Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> showError(friendly(e)));
        });

        // ---------- LOGIN ----------
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            hideError();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                showError("Please enter your email and password.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("This email address is not valid.");
                return;
            }
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(r -> {
                        AlertHelper.redToast(this, "Account accessed: " + email);
                        goToMain();
                    })
                    .addOnFailureListener(e -> showError(friendly(e)));
        });

        // ---------- REGISTER ----------
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            hideError();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                showError("Please enter an email and a password.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("This email address is not valid.");
                return;
            }
            if (pass.length() < 6) {
                showError("Password must be at least 6 characters.");
                return;
            }
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(r -> {
                        AlertHelper.redToast(this, "Account created: " + email);
                        goToMain();
                    })
                    .addOnFailureListener(e -> showError(friendly(e)));
        });
    }

    private void showError(String message) {
        tvError.setText("⚠  " + message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    // turns Firebase errors into simple sentences
    private String friendly(Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection. Check your network and try again.";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Too many attempts. Please wait a few minutes and try again.";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Use at least 6 characters.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "This email is already registered. Tap Login instead.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email. Tap Create new account.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            String code = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return "This email address is not valid.";
            }
            return "Wrong email or password. Please try again.";
        }
        return e.getMessage() == null ? "Something went wrong. Please try again." : e.getMessage();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
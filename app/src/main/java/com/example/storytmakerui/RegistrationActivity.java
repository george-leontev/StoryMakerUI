package com.example.storytmakerui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.Repositories;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private Button btnAuth;
    private ProgressBar progressBar;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnAuth = findViewById(R.id.btnAuth);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        btnAuth.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, AuthActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Валидация полей
        if (username.isEmpty()) {
            Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Некорректный email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        // Блокируем интерфейс
        setRegistering(true);

        // Запускаем регистрацию в фоновом потоке
        new Thread(() -> {
            Result<AuthResponse> result = Repositories.auth.register(username, email, password);

            mainHandler.post(() -> {
                setRegistering(false);

                if (result.isSuccess()) {
                    Toast.makeText(
                            RegistrationActivity.this,
                            "Регистрация успешна!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Переход на главный экран
                    startActivity(new Intent(RegistrationActivity.this, MainPageActivity.class));
                    finish();
                } else {
                    String errorMessage = result.getError() != null
                            ? result.getError().getMessage()
                            : "Неизвестная ошибка";
                    Toast.makeText(
                            RegistrationActivity.this,
                            "Ошибка регистрации: " + errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }).start();
    }

    private void setRegistering(boolean registering) {
        runOnUiThread(() -> {
            btnRegister.setEnabled(!registering);
            progressBar.setVisibility(registering ? View.VISIBLE : View.GONE);

            if (registering) {
                btnRegister.setText("REGISTERING...");
                btnRegister.setAlpha(0.6f);
            } else {
                btnRegister.setText("Register");
                btnRegister.setAlpha(1.0f);
            }
        });
    }
}

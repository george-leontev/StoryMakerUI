package com.example.storytmakerui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.repository.Repositories;
import com.example.storytmakerui.api.repository.Result;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegistration;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Инициализация элементов UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistration = findViewById(R.id.btnRegistration);

        // Обработчик кнопки "Log In"
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Блокируем кнопку
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging In...");

            // Запускаем запрос в фоновом потоке
            executor.execute(() -> {
                Result<AuthResponse> result = Repositories.auth.login(email, password);

                // Возвращаемся в главный поток для обновления UI
                mainHandler.post(() -> {
                    if (result.isSuccess()) {
                        // Переход на главный экран
                        startActivity(new Intent(AuthActivity.this, MainPageActivity.class));
                        finish();
                    } else {
                        String errorMessage = result.getError() != null 
                                ? result.getError().getMessage() 
                                : "Неизвестная ошибка";
                        Toast.makeText(AuthActivity.this, 
                                "Ошибка входа: " + errorMessage, 
                                Toast.LENGTH_SHORT).show();
                    }

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Log In");
                });
            });
        });

        // Обработчик кнопки "Registration"
        btnRegistration.setOnClickListener(v -> {
            startActivity(new Intent(AuthActivity.this, RegistrationActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

package com.example.storytmakerui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storytmakerui.api.models.ProfileResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.Repositories;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    private ImageView ivAvatarPreview;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnSave;
    private Button btnSelectAvatar;
    private ImageView btnChangeAvatar;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private String currentUsername;
    private String currentEmail;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadAvatar();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        loadCurrentProfile();
    }

    private void initViews() {
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnSelectAvatar = findViewById(R.id.btnSelectAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSelectAvatar.setOnClickListener(v -> openImagePicker());
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        showLoading(true);
        executor.execute(() -> {
            Result<ProfileResponse> result = Repositories.user.getProfile();
            
            mainHandler.post(() -> {
                showLoading(false);
                if (result.isSuccess()) {
                    ProfileResponse user = result.getData();
                    currentUsername = user.getUsername();
                    currentEmail = user.getEmail();
                    
                    etUsername.setText(currentUsername);
                    etEmail.setText(currentEmail);
                    
                    if (user.getAvatarImageUrl() != null && !user.getAvatarImageUrl().isEmpty()) {
                        String fullUrl = com.example.storytmakerui.api.ApiClient.getImageUrl(user.getAvatarImageUrl());
                        com.bumptech.glide.Glide.with(this)
                                .load(fullUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(ivAvatarPreview);
                    }
                } else {
                    String errorMessage = result.getError() != null
                            ? result.getError().getMessage()
                            : "Неизвестная ошибка";
                    Toast.makeText(this, "Ошибка загрузки профиля: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        imagePickerLauncher.launch(Intent.createChooser(intent, "Выберите аватар"));
    }

    private void uploadAvatar() {
        try {
            long fileSize = getContentResolver().openFileDescriptor(selectedImageUri, "r").getStatSize();
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                Toast.makeText(this, "Размер изображения превышает 10MB", Toast.LENGTH_SHORT).show();
                return;
            }

            File avatarFile = saveImageToFile();
            showLoading(true);

            executor.execute(() -> {
                Result<ProfileResponse> result = Repositories.user.uploadAvatar(avatarFile);

                mainHandler.post(() -> {
                    showLoading(false);
                    if (result.isSuccess()) {
                        ProfileResponse user = result.getData();
                        String fullUrl = com.example.storytmakerui.api.ApiClient.getImageUrl(user.getAvatarImageUrl());
                        com.bumptech.glide.Glide.with(this)
                                .load(fullUrl)
                                .into(ivAvatarPreview);
                        Toast.makeText(this, "Аватар успешно обновлён", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = result.getError() != null
                                ? result.getError().getMessage()
                                : "Ошибка загрузки аватара";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            });
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка обработки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File saveImageToFile() throws IOException {
        String fileName = UUID.randomUUID().toString() + getExtensionFromUri(selectedImageUri);
        File cacheFile = new File(getCacheDir(), "avatars/" + fileName);
        cacheFile.getParentFile().mkdirs();

        try (var inputStream = getContentResolver().openInputStream(selectedImageUri);
             var outputStream = new FileOutputStream(cacheFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        return cacheFile;
    }

    private String getExtensionFromUri(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) return ".jpg";
        if (mimeType.equals("image/png")) return ".png";
        if (mimeType.equals("image/webp")) return ".webp";
        return ".jpg";
    }

    private void saveProfile() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (newUsername.length() < 3) {
            Toast.makeText(this, "Имя пользователя должно быть минимум 3 символа", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Некорректный email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        // Валидация пароля (если меняем)
        boolean passwordChanged = !newPassword.isEmpty() || !confirmPassword.isEmpty() || !currentPassword.isEmpty();
        if (passwordChanged) {
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Введите текущий пароль для смены", Toast.LENGTH_SHORT).show();
                etCurrentPassword.requestFocus();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show();
                etNewPassword.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Подтвердите новый пароль", Toast.LENGTH_SHORT).show();
                etConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                etConfirmPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Пароль должен быть минимум 6 символов", Toast.LENGTH_SHORT).show();
                etNewPassword.requestFocus();
                return;
            }
        }

        showLoading(true);

        executor.execute(() -> {
            // Шаг 1: Обновляем профиль (username/email)
            Result<ProfileResponse> updateResult = Repositories.user.updateProfile(newUsername, newEmail);

            if (updateResult.isFailure()) {
                mainHandler.post(() -> {
                    showLoading(false);
                    String errorMessage = updateResult.getError() != null
                            ? updateResult.getError().getMessage()
                            : "Ошибка обновления профиля";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
                return;
            }

            // Шаг 2: Если указан новый пароль, меняем его
            if (passwordChanged) {
                Result<ProfileResponse> passwordResult = Repositories.user.changePassword(currentPassword, newPassword);
                if (passwordResult.isFailure()) {
                    mainHandler.post(() -> {
                        showLoading(false);
                        String errorMessage = passwordResult.getError() != null
                                ? passwordResult.getError().getMessage()
                                : "Ошибка смены пароля";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
            }

            // Успех
            mainHandler.post(() -> {
                showLoading(false);
                Toast.makeText(this, "Профиль успешно обновлён", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showLoading(boolean loading) {
        runOnUiThread(() -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
            etUsername.setEnabled(!loading);
            etEmail.setEnabled(!loading);
            etNewPassword.setEnabled(!loading);
            etConfirmPassword.setEnabled(!loading);
            btnSelectAvatar.setEnabled(!loading);
            btnChangeAvatar.setEnabled(!loading);

            if (loading) {
                btnSave.setText("SAVING...");
                btnSave.setAlpha(0.6f);
            } else {
                btnSave.setText("Save");
                btnSave.setAlpha(1.0f);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storytmakerui.api.models.ChapterResponse;
import com.example.storytmakerui.api.models.ChoiceResponse;
import com.example.storytmakerui.api.models.StoryResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.Repositories;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    private EditText etTitle;
    private EditText etDescription;
    private EditText etChapterContent;
    private EditText etOption1;
    private EditText etOption2;
    private EditText etTimerMinutes;
    private Button btnPublish;
    private Button btnSelectCover;
    private ImageView ivCoverPreview;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private File cachedCoverFile;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        previewSelectedImage();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etChapterContent = findViewById(R.id.etChapterContent);
        etOption1 = findViewById(R.id.etOption1);
        etOption2 = findViewById(R.id.etOption2);
        etTimerMinutes = findViewById(R.id.etTimerMinutes);
        btnPublish = findViewById(R.id.btnPublish);
        btnSelectCover = findViewById(R.id.btnSelectCover);
        ivCoverPreview = findViewById(R.id.ivCoverPreview);
        progressBar = findViewById(R.id.progressBar);

        etTimerMinutes.setText("60");
    }

    private void setupListeners() {
        btnSelectCover.setOnClickListener(v -> openImagePicker());
        btnPublish.setOnClickListener(v -> publishStory());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Cover Image"));
    }

    private void previewSelectedImage() {
        if (selectedImageUri == null) return;

        try {
            // Проверка размера файла
            long fileSize = getContentResolver().openFileDescriptor(selectedImageUri, "r").getStatSize();
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                Toast.makeText(this, "Размер изображения превышает 10MB", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
                ivCoverPreview.setVisibility(View.GONE);
                return;
            }

            // Загрузка и отображение превью
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // уменьшаем в 4x для превью
            Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(selectedImageUri), null, options);
            ivCoverPreview.setImageBitmap(bitmap);
            ivCoverPreview.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            selectedImageUri = null;
            ivCoverPreview.setVisibility(View.GONE);
        }
    }

    private File saveSelectedImageToFile() throws IOException {
        if (selectedImageUri == null) return null;

        String fileName = UUID.randomUUID().toString() + getExtensionFromUri(selectedImageUri);
        File cacheFile = new File(getCacheDir(), "covers/" + fileName);
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

    private void publishStory() {
        // Валидация полей
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String chapterContent = etChapterContent.getText().toString().trim();
        String option1 = etOption1.getText().toString().trim();
        String option2 = etOption2.getText().toString().trim();
        String timerText = etTimerMinutes.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название истории", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Введите описание истории", Toast.LENGTH_SHORT).show();
            etDescription.requestFocus();
            return;
        }

        if (chapterContent.isEmpty()) {
            Toast.makeText(this, "Введите содержание первой главы", Toast.LENGTH_SHORT).show();
            etChapterContent.requestFocus();
            return;
        }

        if (option1.isEmpty()) {
            Toast.makeText(this, "Введите вариант выбора 1", Toast.LENGTH_SHORT).show();
            etOption1.requestFocus();
            return;
        }

        if (option2.isEmpty()) {
            Toast.makeText(this, "Введите вариант выбора 2", Toast.LENGTH_SHORT).show();
            etOption2.requestFocus();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(timerText);
            if (duration <= 0) {
                Toast.makeText(this, "Длительность голосования должна быть больше 0", Toast.LENGTH_SHORT).show();
                etTimerMinutes.requestFocus();
                return;
            }
            if (duration > 1440) { // Максимум 24 часа
                Toast.makeText(this, "Длительность не может превышать 24 часа", Toast.LENGTH_SHORT).show();
                etTimerMinutes.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число минут", Toast.LENGTH_SHORT).show();
            etTimerMinutes.requestFocus();
            return;
        }

        // Блокируем интерфейс
        setPublishing(true);

        final String fTitle = title;
        final String fDesc = description;
        final String fContent = chapterContent;
        final String fOpt1 = option1;
        final String fOpt2 = option2;
        final int fDuration = duration;

        executor.execute(() -> {
            File coverFile = null;
            
            // Сохраняем обложку в файл, если выбрана
            if (selectedImageUri != null) {
                try {
                    coverFile = saveSelectedImageToFile();
                } catch (IOException e) {
                    mainHandler.post(() -> {
                        setPublishing(false);
                        showError("Ошибка подготовки изображения: " + e.getMessage());
                    });
                    return;
                }
            }

            try {
                // Шаг 1: создаём историю (всегда multipart, обложка опциональна)
                Result<StoryResponse> storyResult = Repositories.story.createStory(fTitle, fDesc, coverFile);

                if (storyResult.isFailure()) {
                    mainHandler.post(() -> {
                        setPublishing(false);
                        showError("Ошибка создания истории: " + storyResult.getError().getMessage());
                    });
                    return;
                }

                int storyId = storyResult.getData().getId();

                // Шаг 2: создаём главу
                Result<ChapterResponse> chapterResult =
                        Repositories.chapter.createChapter(storyId, fContent, 1);

                if (chapterResult.isFailure()) {
                    Repositories.story.deleteStory(storyId); // откат
                    mainHandler.post(() -> {
                        setPublishing(false);
                        showError("Ошибка создания главы: " + chapterResult.getError().getMessage());
                    });
                    return;
                }

                int chapterId = chapterResult.getData().getId();

                // Шаг 3: создаём выбор
                Result<ChoiceResponse> choiceResult =
                        Repositories.choice.createChoice(chapterId, fOpt1, fOpt2, fDuration);

                if (choiceResult.isFailure()) {
                    Repositories.story.deleteStory(storyId); // откат
                    mainHandler.post(() -> {
                        setPublishing(false);
                        showError("Ошибка создания выбора: " + choiceResult.getError().getMessage());
                    });
                    return;
                }

                // Всё успешно
                mainHandler.post(() -> {
                    setPublishing(false);
                    Toast.makeText(CreateStoryActivity.this,
                            "История опубликована!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(CreateStoryActivity.this, MainPageActivity.class));
                    finish();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    setPublishing(false);
                    showError("Непредвиденная ошибка: " + e.getMessage());
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void cleanupStory(int storyId) {
        // Пытаемся удалить созданную историю при неудаче
        new Thread(() -> {
            Repositories.story.deleteStory(storyId);
        }).start();
    }

    private void setPublishing(boolean publishing) {
        runOnUiThread(() -> {
            btnPublish.setEnabled(!publishing);
            progressBar.setVisibility(publishing ? View.VISIBLE : View.GONE);
            
            if (publishing) {
                btnPublish.setText("PUBLISHING...");
                btnPublish.setAlpha(0.6f);
            } else {
                btnPublish.setText("PUBLISH STORY");
                btnPublish.setAlpha(1.0f);
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

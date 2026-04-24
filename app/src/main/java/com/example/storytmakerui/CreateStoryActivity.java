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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storytmakerui.api.models.ChapterResponse;
import com.example.storytmakerui.api.models.ChoiceResponse;
import com.example.storytmakerui.api.models.StoryResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.Repositories;

public class CreateStoryActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private EditText etChapterContent;
    private EditText etOption1;
    private EditText etOption2;
    private EditText etTimerMinutes;
    private Button btnPublish;
    private ProgressBar progressBar;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        progressBar = findViewById(R.id.progressBar);

        // Устанавливаем значение по умолчанию для таймера (60 минут)
        etTimerMinutes.setText("60");
    }

    private void setupListeners() {
        btnPublish.setOnClickListener(v -> publishStory());
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

        // Запускаем создание истории в фоновом потоке
        new Thread(() -> {
            try {
                // Шаг 1: Создаем историю
                Result<StoryResponse> storyResult = Repositories.story.createStory(title, description);

                mainHandler.post(() -> {
                    if (storyResult.isFailure()) {
                        showError("Ошибка создания истории: " + storyResult.getError().getMessage());
                        return;
                    }

                    StoryResponse story = storyResult.getData();
                    int storyId = story.getId();

                    // Шаг 2: Создаем первую главу
                    new Thread(() -> {
                        Result<ChapterResponse> chapterResult = Repositories.chapter
                                .createChapter(storyId, chapterContent, 1);

                        mainHandler.post(() -> {
                            if (chapterResult.isFailure()) {
                                showError("Ошибка создания главы: " + chapterResult.getError().getMessage());
                                // Пытаемся удалить созданную историю
                                cleanupStory(storyId);
                                return;
                            }

                            ChapterResponse chapter = chapterResult.getData();
                            int chapterId = chapter.getId();

                            // Шаг 3: Создаем выбор для главы
                            new Thread(() -> {
                                Result<ChoiceResponse> choiceResult = Repositories.choice
                                        .createChoice(chapterId, option1, option2, duration);

                                mainHandler.post(() -> {
                                    setPublishing(false);

                                    if (choiceResult.isFailure()) {
                                        showError("Ошибка создания выбора: " + choiceResult.getError().getMessage());
                                        // Пытаемся очистить созданные данные
                                        cleanupStory(storyId);
                                        return;
                                    }

                                    // Успех!
                                    Toast.makeText(
                                            CreateStoryActivity.this,
                                            "История успешно опубликована!",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    // Возвращаемся на главный экран
                                    startActivity(new Intent(CreateStoryActivity.this, MainPageActivity.class));
                                    finish();
                                });
                            }).start();
                        });
                    }).start();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    setPublishing(false);
                    showError("Непредвиденная ошибка: " + e.getMessage());
                });
            }
        }).start();
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

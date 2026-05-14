package com.example.storytmakerui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storytmakerui.api.repository.Repositories;
import com.example.storytmakerui.api.repository.Result;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateChapterActivity extends AppCompatActivity {

    public static final String EXTRA_STORY_ID = "extra_story_id";

    private EditText etChapterTitle;
    private EditText etChapterContent;
    private EditText etOption1;
    private EditText etOption2;
    private EditText etVotingDuration;
    private Button btnSave;
    private ProgressBar progressBar;
    private TextView tvStoryId;

    private int storyId = -1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_chapter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        storyId = getIntent().getIntExtra(EXTRA_STORY_ID, -1);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etChapterTitle = findViewById(R.id.etChapterTitle);
        etChapterContent = findViewById(R.id.etChapterContent);
        etOption1 = findViewById(R.id.etOption1);
        etOption2 = findViewById(R.id.etOption2);
        etVotingDuration = findViewById(R.id.etVotingDuration);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        tvStoryId = findViewById(R.id.tvStoryId);

        tvStoryId.setText("Story ID: " + storyId);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> createChapter());
    }

    private void createChapter() {
        final String chapterTitle = etChapterTitle.getText().toString().trim();
        final String chapterContent = etChapterContent.getText().toString().trim();
        final String option1 = etOption1.getText().toString().trim();
        final String option2 = etOption2.getText().toString().trim();
        final String durationText = etVotingDuration.getText().toString().trim();

        // Валидация
        if (chapterTitle.isEmpty()) {
            etChapterTitle.setError("Введите название главы");
            etChapterTitle.requestFocus();
            return;
        }

        if (chapterContent.isEmpty()) {
            etChapterContent.setError("Введите содержание главы");
            etChapterContent.requestFocus();
            return;
        }

        if (option1.isEmpty()) {
            etOption1.setError("Введите вариант 1");
            etOption1.requestFocus();
            return;
        }

        if (option2.isEmpty()) {
            etOption2.setError("Введите вариант 2");
            etOption2.requestFocus();
            return;
        }

        int durationValue;
        try {
            durationValue = Integer.parseInt(durationText);
            if (durationValue < 1) durationValue = 1;
        } catch (NumberFormatException e) {
            etVotingDuration.setError("Неверное число");
            etVotingDuration.requestFocus();
            return;
        }
        final int duration = durationValue;

        showLoading(true);

        executor.execute(() -> {
            android.util.Log.d("CreateChapter", "=== START CREATE CHAPTER ===");
            android.util.Log.d("CreateChapter", "storyId: " + storyId);
            android.util.Log.d("CreateChapter", "chapterTitle: " + chapterTitle);
            android.util.Log.d("CreateChapter", "chapterContent length: " + chapterContent.length());
            android.util.Log.d("CreateChapter", "option1: " + option1);
            android.util.Log.d("CreateChapter", "option2: " + option2);
            android.util.Log.d("CreateChapter", "duration: " + duration);
            
            // Шаг 1: Создаём главу с title и content (sequenceNumber = null для авто-определения)
            android.util.Log.d("CreateChapter", "Creating chapter via API...");
            Result<com.example.storytmakerui.api.models.ChapterResponse> chapterResult =
                    Repositories.chapter.createChapter(storyId, chapterTitle, chapterContent, null);

            android.util.Log.d("CreateChapter", "Chapter result: isSuccess=" + chapterResult.isSuccess());
            
            if (chapterResult.isFailure()) {
                android.util.Log.e("CreateChapter", "Chapter creation FAILED: " + chapterResult.getError());
                mainHandler.post(() -> {
                    showLoading(false);
                    String error = chapterResult.getError() != null
                            ? chapterResult.getError().getMessage()
                            : "Ошибка создания главы";
                    Toast.makeText(CreateChapterActivity.this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                });
                return;
            }

            int chapterId = chapterResult.getData().getId();
            android.util.Log.d("CreateChapter", "Chapter CREATED successfully! chapterId=" + chapterId);

            // Шаг 2: Создаём выбор
            android.util.Log.d("CreateChapter", "Creating choice for chapterId=" + chapterId + "...");
            Result<com.example.storytmakerui.api.models.ChoiceResponse> choiceResult =
                    Repositories.choice.createChoice(chapterId, option1, option2, duration);

            android.util.Log.d("CreateChapter", "Choice result: isSuccess=" + choiceResult.isSuccess());
            
            if (choiceResult.isFailure()) {
                android.util.Log.e("CreateChapter", "Choice creation FAILED: " + choiceResult.getError());
                mainHandler.post(() -> {
                    showLoading(false);
                    String error = choiceResult.getError() != null
                            ? choiceResult.getError().getMessage()
                            : "Ошибка создания выбора";
                    Toast.makeText(CreateChapterActivity.this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                });
                return;
            }

            android.util.Log.d("CreateChapter", "Choice CREATED successfully! choiceId=" + choiceResult.getData().getId());
            android.util.Log.d("CreateChapter", "=== CREATE CHAPTER COMPLETE ===");

            mainHandler.post(() -> {
                showLoading(false);
                Toast.makeText(CreateChapterActivity.this, "Глава создана!", Toast.LENGTH_SHORT).show();
                
                // Переход к чтению истории, чтобы сразу увидеть новую главу
                Intent intent = new Intent(CreateChapterActivity.this, ReaderActivity.class);
                intent.putExtra(ReaderActivity.EXTRA_STORY_ID, storyId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }
}

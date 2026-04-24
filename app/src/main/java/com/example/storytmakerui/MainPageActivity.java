package com.example.storytmakerui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storytmakerui.api.models.PagedResponse;
import com.example.storytmakerui.api.models.StoryResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.StoryRepository;

import java.util.ArrayList;
import java.util.List;

public class MainPageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStories;
    private TextView tvEmptyState;
    private StoryAdapter storyAdapter;
    private final StoryRepository storyRepository = new StoryRepository();
    private final List<StoryResponse> storiesList = new ArrayList<>();
    private Button btnCreate;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        loadStories();
    }

    private void initViews() {
        recyclerViewStories = findViewById(R.id.recyclerViewStories);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnCreate = findViewById(R.id.btnCreate);

        storyAdapter = new StoryAdapter(storiesList);
        recyclerViewStories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStories.setAdapter(storyAdapter);
    }

    private void setupListeners() {
        btnCreate.setOnClickListener(v -> {
            startActivity(new Intent(MainPageActivity.this, CreateStoryActivity.class));
        });
    }

    private void loadStories() {
        new Thread(() -> {
            Result<PagedResponse<StoryResponse>> result = storyRepository.getStories();
            
            mainHandler.post(() -> {
                if (result.isSuccess()) {
                    storiesList.clear();
                    storiesList.addAll(result.getData().getItems());
                    storyAdapter.notifyDataSetChanged();
                    
                    if (storiesList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerViewStories.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerViewStories.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyState.setText("Ошибка загрузки историй: " + result.getError().getMessage());
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewStories.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}
package com.example.storytmakerui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storytmakerui.api.models.RatingResponse;
import com.example.storytmakerui.api.repository.Repositories;
import com.example.storytmakerui.api.repository.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_STORY_ID = "extra_story_id";
    public static final String EXTRA_STORY_TITLE = "extra_story_title";
    public static final String EXTRA_STORY_DESCRIPTION = "extra_story_description";
    public static final String EXTRA_STORY_AUTHOR = "extra_story_author";
    public static final String EXTRA_STORY_COVER_URL = "extra_story_cover_url";
    public static final String EXTRA_STORY_CHAPTER_COUNT = "extra_story_chapter_count";
    public static final String EXTRA_STORY_CREATED_AT = "extra_story_created_at";

    private static final int STAR_ACTIVE_COLOR = 0xFFCD4631;
    private static final int STAR_INACTIVE_COLOR = 0xFFCCCCCC;

    private ImageView ivCover;
    private TextView tvStoryTitle;
    private TextView tvStoryAuthor;
    private TextView tvStoryDescription;
    private TextView tvStoryChapters;
    private TextView tvStoryCreatedAt;
    private TextView tvAverageRating;
    private TextView tvVoteCount;
    private TextView tvUserScore;
    private Button btnRead;
    private ProgressBar progressBar;
    private ImageView[] stars;

    private int storyId = -1;
    private Integer currentUserScore;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindStoryFromIntent();
        setupListeners();
        loadRating();
    }

    private void initViews() {
        ivCover = findViewById(R.id.ivCover);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        tvStoryAuthor = findViewById(R.id.tvStoryAuthor);
        tvStoryDescription = findViewById(R.id.tvStoryDescription);
        tvStoryChapters = findViewById(R.id.tvStoryChapters);
        tvStoryCreatedAt = findViewById(R.id.tvStoryCreatedAt);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvVoteCount = findViewById(R.id.tvVoteCount);
        tvUserScore = findViewById(R.id.tvUserScore);
        btnRead = findViewById(R.id.btnRead);
        progressBar = findViewById(R.id.progressBar);

        stars = new ImageView[]{
                findViewById(R.id.ivStar1),
                findViewById(R.id.ivStar2),
                findViewById(R.id.ivStar3),
                findViewById(R.id.ivStar4),
                findViewById(R.id.ivStar5)
        };
    }

    private void bindStoryFromIntent() {
        storyId = getIntent().getIntExtra(EXTRA_STORY_ID, -1);

        String title = getIntent().getStringExtra(EXTRA_STORY_TITLE);
        String description = getIntent().getStringExtra(EXTRA_STORY_DESCRIPTION);
        String author = getIntent().getStringExtra(EXTRA_STORY_AUTHOR);
        String coverUrl = getIntent().getStringExtra(EXTRA_STORY_COVER_URL);
        int chapterCount = getIntent().getIntExtra(EXTRA_STORY_CHAPTER_COUNT, 0);
        String createdAt = getIntent().getStringExtra(EXTRA_STORY_CREATED_AT);

        tvStoryTitle.setText(title != null ? title : "Untitled");
        tvStoryAuthor.setText("Author: " + (author != null ? author : "Unknown"));
        tvStoryDescription.setText(description != null && !description.isEmpty() ? description : "No description");
        tvStoryChapters.setText(String.valueOf(chapterCount));
        tvStoryCreatedAt.setText(formatDate(createdAt));

        if (coverUrl != null && !coverUrl.isEmpty()) {
            String fullUrl = "http://10.0.2.2:5157" + coverUrl;
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivCover);
            ivCover.setVisibility(View.VISIBLE);
        } else {
            ivCover.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        for (int i = 0; i < stars.length; i++) {
            final int score = i + 1;
            stars[i].setOnClickListener(v -> submitRating(score));
        }

        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReaderActivity.class);
            intent.putExtra(ReaderActivity.EXTRA_STORY_ID, storyId);
            intent.putExtra(ReaderActivity.EXTRA_STORY_TITLE,
                    getIntent().getStringExtra(EXTRA_STORY_TITLE));
            startActivity(intent);
        });
    }

    private void loadRating() {
        if (storyId <= 0) return;

        executor.execute(() -> {
            Result<RatingResponse> result = Repositories.rating.getRating(storyId);

            mainHandler.post(() -> {
                if (result.isSuccess()) {
                    applyRating(result.getData());
                } else {
                    tvAverageRating.setText("0.0");
                    tvVoteCount.setText("(0 votes)");
                    updateStars(0);
                }
            });
        });
    }

    private void submitRating(int score) {
        if (storyId <= 0) return;

        setVoting(true);
        updateStars(score);

        executor.execute(() -> {
            Result<RatingResponse> result = Repositories.rating.setRating(storyId, score);

            mainHandler.post(() -> {
                setVoting(false);
                if (result.isSuccess()) {
                    applyRating(result.getData());
                    Toast.makeText(this, "Thanks for your rating!", Toast.LENGTH_SHORT).show();
                } else {
                    String message = result.getError() != null
                            ? result.getError().getMessage()
                            : "Unknown error";
                    Toast.makeText(this, "Rating failed: " + message, Toast.LENGTH_LONG).show();
                    updateStars(currentUserScore != null ? currentUserScore : 0);
                }
            });
        });
    }

    private void applyRating(RatingResponse rating) {
        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", rating.getAverageRating()));
        tvVoteCount.setText(String.format(Locale.getDefault(), "(%d votes)", rating.getVoteCount()));

        currentUserScore = rating.getUserScore();
        if (currentUserScore != null && currentUserScore > 0) {
            updateStars(currentUserScore);
            tvUserScore.setText(String.format(Locale.getDefault(),
                    "You rated this story %d/5", currentUserScore));
        } else {
            updateStars(0);
            tvUserScore.setText("Tap a star to rate");
        }
    }

    private void updateStars(int score) {
        for (int i = 0; i < stars.length; i++) {
            int color = i < score ? STAR_ACTIVE_COLOR : STAR_INACTIVE_COLOR;
            stars[i].setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setVoting(boolean voting) {
        progressBar.setVisibility(voting ? View.VISIBLE : View.GONE);
        for (ImageView star : stars) {
            star.setEnabled(!voting);
            star.setAlpha(voting ? 0.6f : 1.0f);
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null) return "—";

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.getDefault());
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = fmt.parse(rawDate);
                if (date != null) return outputFormat.format(date);
            } catch (ParseException ignored) {
            }
        }

        return rawDate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

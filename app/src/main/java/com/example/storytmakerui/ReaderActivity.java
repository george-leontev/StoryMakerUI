package com.example.storytmakerui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.example.storytmakerui.api.models.ChapterResponse;
import com.example.storytmakerui.api.models.ChoiceResponse;
import com.example.storytmakerui.api.repository.Repositories;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.utils.PreferenceManager;
import android.util.JsonReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderActivity extends AppCompatActivity {

    public static final String EXTRA_STORY_ID = "reader_story_id";
    public static final String EXTRA_STORY_TITLE = "reader_story_title";

    private TextView tvStoryTitleHeader;
    private TextView tvChapterIndicator;
    private TextView tvChapterNumber;
    private TextView tvChapterContent;
    private TextView tvPageIndicator;
    private TextView tvVoteStatus;
    private TextView tvChoiceDeadline;
    private Button btnPrev;
    private Button btnNext;
    private Button btnOption1;
    private Button btnOption2;
    private LinearLayout llChoiceBlock;
    private LinearLayout llChoiceDivider;
    private ProgressBar progressBar;
    private ScrollView scrollContent;
    private ImageView btnBack;

    private int storyId;
    private String storyTitle;
    private int storyAuthorId;
    private List<ChapterResponse> chapters;
    private int currentIndex = 0;
    private ChoiceResponse currentChoice;
    private int currentChapterId;

    private PreferenceManager preferenceManager;
    private int currentUserId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reader);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        storyId = getIntent().getIntExtra(EXTRA_STORY_ID, -1);
        storyTitle = getIntent().getStringExtra(EXTRA_STORY_TITLE);
        storyAuthorId = getIntent().getIntExtra("extra_story_author_id", -1);
        preferenceManager = new PreferenceManager(this);
        
        // Получаем текущего пользователя из JWT токена

        initViews();
        setupListeners();
        loadChapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Перезагружаем главы при возвращении (например, после создания новой)
        if (storyId > 0) {
            loadChapters();
        }
    }

    private void initViews() {
        tvStoryTitleHeader = findViewById(R.id.tvStoryTitleHeader);
        tvChapterIndicator = findViewById(R.id.tvChapterIndicator);
        tvChapterNumber = findViewById(R.id.tvChapterNumber);
        tvChapterContent = findViewById(R.id.tvChapterContent);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);
        tvVoteStatus = findViewById(R.id.tvVoteStatus);
        tvChoiceDeadline = findViewById(R.id.tvChoiceDeadline);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        llChoiceBlock = findViewById(R.id.llChoiceBlock);
        llChoiceDivider = findViewById(R.id.llChoiceDivider);
        progressBar = findViewById(R.id.progressBar);
        scrollContent = findViewById(R.id.scrollContent);
        btnBack = findViewById(R.id.btnBack);

        tvStoryTitleHeader.setText(storyTitle != null ? storyTitle : "");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnPrev.setOnClickListener(v -> showChapter(currentIndex - 1));
        btnNext.setOnClickListener(v -> showChapter(currentIndex + 1));
        btnOption1.setOnClickListener(v -> submitVote(1));
        btnOption2.setOnClickListener(v -> submitVote(2));
    }

    private void loadChapters() {
        android.util.Log.d("ReaderActivity", "=== LOAD CHAPTERS START ===");
        android.util.Log.d("ReaderActivity", "storyId=" + storyId);
        setLoading(true);
        executor.execute(() -> {
            Result<com.example.storytmakerui.api.models.PagedResponse<ChapterResponse>> result =
                    Repositories.chapter.getChapters(storyId, 1, 100);

            android.util.Log.d("ReaderActivity", "API response: isSuccess=" + result.isSuccess());
            if (result.isSuccess()) {
                var data = result.getData();
                android.util.Log.d("ReaderActivity", "totalCount=" + data.getTotalCount());
                android.util.Log.d("ReaderActivity", "items.size()=" + (data.getItems() != null ? data.getItems().size() : 0));
                if (data.getItems() != null) {
                    for (int i = 0; i < data.getItems().size(); i++) {
                        var ch = data.getItems().get(i);
                        android.util.Log.d("ReaderActivity", "  Chapter[" + i + "]: id=" + ch.getId() + 
                                ", seq=" + ch.getSequenceNumber() + 
                                ", hasChoice=" + ch.hasChoice());
                    }
                }
            } else {
                android.util.Log.e("ReaderActivity", "API FAILED: " + result.getError());
            }
            
            mainHandler.post(() -> {
                setLoading(false);
                if (result.isSuccess() && result.getData() != null) {
                    List<ChapterResponse> items = result.getData().getItems();
                    if (items != null && !items.isEmpty()) {
                        items.sort((a, b) -> a.getSequenceNumber() - b.getSequenceNumber());
                        chapters = items;
                        android.util.Log.d("ReaderActivity", "Chapters loaded: " + chapters.size());
                        showChapter(0);
                    } else {
                        android.util.Log.w("ReaderActivity", "No chapters found for storyId=" + storyId);
                        tvChapterContent.setText("This story has no chapters yet.");
                    }
                } else {
                    String msg = result.getError() != null ? result.getError().getMessage() : "Unknown error";
                    android.util.Log.e("ReaderActivity", "Failed to load chapters: " + msg);
                    tvChapterContent.setText("Failed to load: " + msg);
                }
            });
        });
    }

    private void showChapter(int index) {
        if (chapters == null || index < 0 || index >= chapters.size()) return;
        currentIndex = index;
        currentChoice = null;

        ChapterResponse chapter = chapters.get(currentIndex);
        int total = chapters.size();

        tvChapterNumber.setText("Chapter " + chapter.getSequenceNumber());
        tvChapterContent.setText(chapter.getContent());
        tvChapterIndicator.setText("Chapter " + chapter.getSequenceNumber() + " of " + total);
        tvPageIndicator.setText((currentIndex + 1) + " / " + total);

        btnPrev.setEnabled(currentIndex > 0);
        btnPrev.setAlpha(currentIndex > 0 ? 1f : 0.4f);
        btnNext.setEnabled(currentIndex < total - 1);
        btnNext.setAlpha(currentIndex < total - 1 ? 1f : 0.4f);

        // Скрываем блок выбора пока грузим
        llChoiceDivider.setVisibility(View.GONE);
        llChoiceBlock.setVisibility(View.GONE);

        scrollContent.post(() -> scrollContent.scrollTo(0, 0));

        if (chapter.hasChoice()) {
            loadChoice(chapter.getId());
        }
    }

    private void loadChoice(int chapterId) {
        currentChapterId = chapterId;
        executor.execute(() -> {
            // Если автор истории — получаем результаты голосования
            Result<ChoiceResponse> result;
            if (storyAuthorId == currentUserId && currentUserId > 0) {
                // Сначала получаем выбор через public endpoint, чтобы узнать ID выбора
                Result<ChoiceResponse> publicResult = Repositories.choice.getChoice(chapterId);
                if (publicResult.isFailure()) {
                    mainHandler.post(() -> {
                        String err = publicResult.getError() != null ? publicResult.getError().getMessage() : "Failed to load choice";
                        Toast.makeText(this, "Choice error: " + err, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                int choiceId = publicResult.getData().getId();
                // Теперь получаем результаты для автора
                result = Repositories.choice.getChoiceForAuthor(chapterId, choiceId);
            } else {
                result = Repositories.choice.getChoice(chapterId);
            }
            
            mainHandler.post(() -> {
                if (result.isSuccess() && result.getData() != null) {
                    currentChoice = result.getData();
                    renderChoice(currentChoice);
                } else {
                    String err = result.getError() != null ? result.getError().getMessage() : "null body";
                    android.util.Log.e("ReaderChoice", "Choice load failed: " + err);
                    Toast.makeText(this, "Choice error: " + err, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void renderChoice(ChoiceResponse choice) {
        llChoiceDivider.setVisibility(View.VISIBLE);
        llChoiceBlock.setVisibility(View.VISIBLE);

        btnOption1.setText(choice.getOption1Text());
        btnOption2.setText(choice.getOption2Text());

        int savedVote = preferenceManager.getVotedOption(choice.getId());

        if (choice.isClosed()) {
            showClosedChoice(choice);
        } else if (savedVote > 0) {
            showVotedState(savedVote, false);
        } else {
            showActiveChoice();
        }

        if (choice.getExpiresAt() != null && !choice.isClosed()) {
            tvChoiceDeadline.setText("Voting ends: " + formatDate(choice.getExpiresAt()));
        } else if (choice.isClosed()) {
            tvChoiceDeadline.setText("Voting closed");
        } else {
            tvChoiceDeadline.setText("");
        }
    }

    private void showActiveChoice() {
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption1.setAlpha(1f);
        btnOption2.setAlpha(1f);
        tvVoteStatus.setVisibility(View.GONE);
        applyButtonStyle(btnOption1, true);
        applyButtonStyle(btnOption2, false);
    }

    private void showVotedState(int votedOption, boolean justVoted) {
        // Обе кнопки серые — голоса скрыты (per liveplot-concept)
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption1.setAlpha(0.45f);
        btnOption2.setAlpha(0.45f);
        applyButtonStyle(btnOption1, true);
        applyButtonStyle(btnOption2, false);

        tvVoteStatus.setVisibility(View.VISIBLE);
        if (justVoted) {
            tvVoteStatus.setText("Your vote is in! Results will appear when voting ends.");
        } else {
            tvVoteStatus.setText("You already voted. Results will appear when voting ends.");
        }
    }

    private void showClosedChoice(ChoiceResponse choice) {
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);

        Integer v1 = choice.getOption1Votes();
        Integer v2 = choice.getOption2Votes();

        String label1 = choice.getOption1Text();
        String label2 = choice.getOption2Text();

        if (v1 != null && v2 != null) {
            label1 = choice.getOption1Text() + "  — " + v1 + " votes";
            label2 = choice.getOption2Text() + "  — " + v2 + " votes";
        }

        btnOption1.setText(label1);
        btnOption2.setText(label2);

        Integer winner = choice.getWinningOption();
        if (winner != null && winner == 1) {
            btnOption1.setAlpha(1f);
            btnOption2.setAlpha(0.4f);
            applyButtonStyle(btnOption1, true);   // чёрный = победитель
            applyButtonStyle(btnOption2, false);
        } else if (winner != null && winner == 2) {
            btnOption1.setAlpha(0.4f);
            btnOption2.setAlpha(1f);
            applyButtonStyle(btnOption1, false);
            applyButtonStyle(btnOption2, true);   // чёрный = победитель
        } else {
            btnOption1.setAlpha(0.6f);
            btnOption2.setAlpha(0.6f);
            applyButtonStyle(btnOption1, true);
            applyButtonStyle(btnOption2, false);
        }

        tvVoteStatus.setVisibility(View.VISIBLE);
        if (winner != null) {
            String winnerText = winner == 1 ? choice.getOption1Text() : choice.getOption2Text();
            tvVoteStatus.setText("Voting closed. Winner: \"" + winnerText + "\"");
        } else {
            tvVoteStatus.setText("Voting closed.");
        }
    }

    private void submitVote(int option) {
        if (currentChoice == null) return;

        // Сразу блокируем кнопки
        showVotedState(option, true);

        int choiceId = currentChoice.getId();
        int chapterId = chapters.get(currentIndex).getId();

        executor.execute(() -> {
            Result<ChoiceResponse> result = Repositories.choice.vote(chapterId, choiceId, option);
            mainHandler.post(() -> {
                if (result.isSuccess()) {
                    preferenceManager.saveVotedOption(choiceId, option);
                    if (result.getData() != null && result.getData().isClosed()) {
                        currentChoice = result.getData();
                        showClosedChoice(currentChoice);
                    }
                } else {
                    // Сервер отклонил — если уже голосовал, просто оставляем заблокированным
                    String msg = result.getError() != null ? result.getError().getMessage() : "";
                    if (msg.contains("already") || msg.contains("409") || msg.contains("400")) {
                        preferenceManager.saveVotedOption(choiceId, option);
                    } else {
                        // Настоящая ошибка — разблокировать
                        showActiveChoice();
                        Toast.makeText(this, "Vote failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    // true = чёрная заливка (BlackSolid), false = белая с обводкой (Default)
    private void applyButtonStyle(Button btn, boolean solid) {
        if (solid) {
            btn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF000000));
            btn.setTextColor(0xFFFFFFFF);
        } else {
            btn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            btn.setTextColor(0xFF000000);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null) return "";
        SimpleDateFormat out = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(p, Locale.getDefault());
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = fmt.parse(rawDate);
                if (d != null) return out.format(d);
            } catch (ParseException ignored) {}
        }
        return rawDate;
    }

    private int extractUserIdFromJwt(String token) {
        android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: token=" + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null"));
        if (token == null || token.trim().isEmpty()) {
            android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: token is null or empty");
            return -1;
        }
        
        // Убираем префикс "Bearer " если он есть
        String jwt = token;
        if (token.startsWith("Bearer ")) {
            jwt = token.substring(7);
        }
        
        android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: jwt=" + jwt.substring(0, Math.min(30, jwt.length())) + "...");
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: invalid jwt parts count=" + parts.length);
            return -1;
        }
        
        try {
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
            android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: payload=" + payload);
            
            // .NET использует ClaimTypes.NameIdentifier = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"
            String claimKey = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";
            String searchPattern = "\"" + claimKey + "\"";
            
            int claimStart = payload.indexOf(searchPattern);
            if (claimStart != -1) {
                // Находим двоеточие после ключа
                int colonPos = payload.indexOf(':', claimStart + searchPattern.length());
                if (colonPos != -1) {
                    // Пропускаем пробелы после двоеточия
                    int valueStart = colonPos + 1;
                    while (valueStart < payload.length() && (payload.charAt(valueStart) == ' ' || payload.charAt(valueStart) == '\t')) {
                        valueStart++;
                    }
                    
                    // Проверяем, начинается ли значение с кавычки (string) или цифры (number)
                    if (valueStart < payload.length()) {
                        char firstChar = payload.charAt(valueStart);
                        String userIdStr;
                        
                        if (firstChar == '"') {
                            // Значение в кавычках (string)
                            int quoteEnd = payload.indexOf('"', valueStart + 1);
                            if (quoteEnd != -1) {
                                userIdStr = payload.substring(valueStart + 1, quoteEnd);
                            } else {
                                return -1;
                            }
                        } else if (Character.isDigit(firstChar) || firstChar == '-') {
                            // Числовое значение
                            int valueEnd = valueStart;
                            while (valueEnd < payload.length() && (Character.isDigit(payload.charAt(valueEnd)) || payload.charAt(valueEnd) == '-')) {
                                valueEnd++;
                            }
                            userIdStr = payload.substring(valueStart, valueEnd);
                        } else {
                            return -1;
                        }
                        
                        android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: found userId=" + userIdStr);
                        return Integer.parseInt(userIdStr.trim());
                    }
                }
            }
            
            android.util.Log.d("ReaderActivity", "extractUserIdFromJwt: no valid claim found");
            return -1;
        } catch (Exception e) {
            android.util.Log.e("ReaderActivity", "extractUserIdFromJwt: error=" + e.getMessage(), e);
            return -1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

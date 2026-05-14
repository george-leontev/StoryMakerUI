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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storytmakerui.api.models.ProfileResponse;
import com.example.storytmakerui.api.models.StoryResponse;
import com.example.storytmakerui.api.models.VoteHistoryResponse;
import com.example.storytmakerui.api.repository.Result;
import com.example.storytmakerui.api.repository.Repositories;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvMemberSince;
    private TextView tvStoriesCount;
    private TextView tvVotesCount;
    private TextView tvCommentsCount;
    private ImageView btnChangeAvatar;
    private ImageView btnBack;
    private ImageView btnEditProfile;
    private Button btnLogout;

    private ProgressBar progressBar;
    private RecyclerView recyclerViewMyStories;
    private RecyclerView recyclerViewMyVotes;
    private TextView tvNoStories;
    private TextView tvNoVotes;

    private Uri selectedImageUri;
    private List<StoryResponse> storiesList = new ArrayList<>();
    private List<VoteHistoryResponse> votesList = new ArrayList<>();
    private StoryAdapter storiesAdapter;
    private VoteHistoryAdapter votesAdapter;
    private ProfileResponse currentUser;

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
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        loadProfile();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvStoriesCount = findViewById(R.id.tvStoriesCount);
        tvVotesCount = findViewById(R.id.tvVotesCount);
        tvCommentsCount = findViewById(R.id.tvCommentsCount);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewMyStories = findViewById(R.id.recyclerViewMyStories);
        recyclerViewMyVotes = findViewById(R.id.recyclerViewMyVotes);
        tvNoStories = findViewById(R.id.tvNoStories);
        tvNoVotes = findViewById(R.id.tvNoVotes);

        storiesAdapter = new StoryAdapter(storiesList);
        recyclerViewMyStories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyStories.setAdapter(storiesAdapter);

        votesAdapter = new VoteHistoryAdapter(votesList);
        recyclerViewMyVotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyVotes.setAdapter(votesAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
        
        btnLogout.setOnClickListener(v -> {
            Repositories.auth.logout();
            startActivity(new Intent(ProfileActivity.this, AuthActivity.class));
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        imagePickerLauncher.launch(Intent.createChooser(intent, "Выберите аватар"));
    }

    private void loadProfile() {
        setLoading(true);
        executor.execute(() -> {
            Result<ProfileResponse> profileResult = Repositories.user.getProfile();
            Result<com.example.storytmakerui.api.models.PagedResponse<StoryResponse>> storiesResult = Repositories.user.getMyStories();
            Result<com.example.storytmakerui.api.models.PagedResponse<VoteHistoryResponse>> votesResult = Repositories.user.getMyVotes();

            mainHandler.post(() -> {
                setLoading(false);
                
                if (profileResult.isSuccess()) {
                    currentUser = profileResult.getData();
                    displayProfile(currentUser);
                } else {
                    showError("Ошибка загрузки профиля: " + profileResult.getError().getMessage());
                }

                if (storiesResult.isSuccess()) {
                    storiesList.clear();
                    storiesList.addAll(storiesResult.getData().getItems());
                    storiesAdapter.notifyDataSetChanged();
                    tvNoStories.setVisibility(storiesList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewMyStories.setVisibility(storiesList.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    storiesList.clear();
                    storiesAdapter.notifyDataSetChanged();
                    tvNoStories.setVisibility(View.VISIBLE);
                    recyclerViewMyStories.setVisibility(View.GONE);
                }

                if (votesResult.isSuccess()) {
                    com.example.storytmakerui.api.models.PagedResponse<VoteHistoryResponse> votesData = votesResult.getData();
                    if (votesData != null && votesData.getItems() != null) {
                        votesList.clear();
                        votesList.addAll(votesData.getItems());
                        if (votesAdapter == null) {
                            votesAdapter = new VoteHistoryAdapter(votesList);
                            recyclerViewMyVotes.setAdapter(votesAdapter);
                        } else {
                            votesAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    votesList.clear();
                    if (votesAdapter != null) {
                        votesAdapter.notifyDataSetChanged();
                    }
                }
            });
        });
    }

    private void displayProfile(ProfileResponse user) {
        tvUsername.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        tvMemberSince.setText("Член с " + formatDate(user.getCreatedAt()));

        if (user.getAvatarImageUrl() != null && !user.getAvatarImageUrl().isEmpty()) {
            String fullUrl = "http://192.168.1.70:5157" + user.getAvatarImageUrl();
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Обновляем статистику
        tvStoriesCount.setText(user.getStoriesCount() + " историй");
        tvVotesCount.setText(user.getVotesCount() + " голосов");
        tvCommentsCount.setText(user.getCommentsCount() + " коммент.");
    }

    private void uploadAvatar() {
        try {
            long fileSize = getContentResolver().openFileDescriptor(selectedImageUri, "r").getStatSize();
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                Toast.makeText(this, "Размер изображения превышает 10MB", Toast.LENGTH_SHORT).show();
                return;
            }

            File avatarFile = saveImageToFile();
            setLoading(true);

            executor.execute(() -> {
                Result<ProfileResponse> result = Repositories.user.uploadAvatar(avatarFile);

                mainHandler.post(() -> {
                    setLoading(false);
                    if (result.isSuccess()) {
                        currentUser = result.getData();
                        displayProfile(currentUser);
                        Toast.makeText(this, "Аватар обновлён", Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Ошибка загрузки аватара: " + result.getError().getMessage());
                    }
                });
            });
        } catch (IOException e) {
            showError("Ошибка обработки изображения: " + e.getMessage());
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

    private void setLoading(boolean loading) {
        runOnUiThread(() -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String formatDate(String rawDate) {
        if (rawDate == null) return "";
        SimpleDateFormat out = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

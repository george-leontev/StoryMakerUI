package com.example.storytmakerui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.StoryResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<StoryResponse> stories;

    public StoryAdapter(List<StoryResponse> stories) {
        this.stories = stories;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        StoryResponse story = stories.get(position);
        holder.bind(story);
        holder.itemView.setOnClickListener(v -> openDetails(v, story));
    }

    private void openDetails(View view, StoryResponse story) {
        Intent intent = new Intent(view.getContext(), StoryDetailsActivity.class);
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_ID, story.getId());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_TITLE, story.getTitle());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_DESCRIPTION, story.getDescription());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_AUTHOR, story.getAuthorUsername());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_COVER_URL, story.getCoverImageUrl());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_CHAPTER_COUNT, story.getChapterCount());
        intent.putExtra(StoryDetailsActivity.EXTRA_STORY_CREATED_AT, story.getCreatedAt());
        view.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvAuthor;
        private final TextView tvRating;
        private final TextView tvChapters;
        private final TextView tvCreatedAt;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvDescription = itemView.findViewById(R.id.tvStoryDescription);
            tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
            tvRating = itemView.findViewById(R.id.tvStoryRating);
            tvChapters = itemView.findViewById(R.id.tvStoryChapters);
            tvCreatedAt = itemView.findViewById(R.id.tvStoryCreatedAt);
        }

        public void bind(StoryResponse story) {
            // Загрузка обложки
            String coverUrl = story.getCoverImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                // Полная URL для загрузки изображения
                String fullUrl = "http://10.0.2.2:5157" + coverUrl;
                Glide.with(ivCover.getContext())
                        .load(fullUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivCover);
                ivCover.setVisibility(View.VISIBLE);
            } else {
                ivCover.setVisibility(View.GONE);
            }

            tvTitle.setText(story.getTitle());
            tvDescription.setText(story.getDescription() != null ? story.getDescription() : "No description");
            tvAuthor.setText("Author: " + (story.getAuthorUsername() != null ? story.getAuthorUsername() : "Unknown"));
            
            // Форматирование рейтинга
            double rating = story.getRating() != 0 ? story.getRating() : 0.0;
            tvRating.setText(String.format(Locale.getDefault(), "Rating: %.1f", rating));
            
            // Количество глав
            int chapterCount = story.getChapterCount() != 0 ? story.getChapterCount() : 0;
            tvChapters.setText("Chapters: " + chapterCount);
            
            tvCreatedAt.setText(formatDate(story.getCreatedAt()));
        }

        private String formatDate(String rawDate) {
            if (rawDate == null) return "";

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

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
                    if (date != null) return "Created: " + outputFormat.format(date);
                } catch (ParseException ignored) {
                }
            }

            return "Created: " + rawDate;
        }
    }
}

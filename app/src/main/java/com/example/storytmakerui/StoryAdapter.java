package com.example.storytmakerui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storytmakerui.api.models.StoryResponse;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvAuthor;
        private final TextView tvRating;
        private final TextView tvChapters;
        private final TextView tvCreatedAt;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvDescription = itemView.findViewById(R.id.tvStoryDescription);
            tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
            tvRating = itemView.findViewById(R.id.tvStoryRating);
            tvChapters = itemView.findViewById(R.id.tvStoryChapters);
            tvCreatedAt = itemView.findViewById(R.id.tvStoryCreatedAt);
        }

        public void bind(StoryResponse story) {
            tvTitle.setText(story.getTitle());
            tvDescription.setText(story.getDescription() != null ? story.getDescription() : "Нет описания");
            tvAuthor.setText("Автор: " + (story.getAuthorUsername() != null ? story.getAuthorUsername() : "Неизвестно"));
            
            // Форматирование рейтинга
            double rating = story.getRating() != 0 ? story.getRating() : 0.0;
            tvRating.setText(String.format(Locale.getDefault(), "Рейтинг: %.1f", rating));
            
            // Количество глав
            int chapterCount = story.getChapterCount() != 0 ? story.getChapterCount() : 0;
            tvChapters.setText("Глав: " + chapterCount);
            
            // Форматирование даты
            if (story.getCreatedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                    java.util.Date date = inputFormat.parse(story.getCreatedAt());
                    tvCreatedAt.setText(date != null ? "Создано: " + outputFormat.format(date) : "");
                } catch (Exception e) {
                    tvCreatedAt.setText("Создано: " + story.getCreatedAt());
                }
            }
        }
    }
}

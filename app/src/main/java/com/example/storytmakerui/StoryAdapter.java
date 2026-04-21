package com.example.storytmakerui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storytmakerui.api.models.StoryResponse;

import java.util.List;

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

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvDescription = itemView.findViewById(R.id.tvStoryDescription);
            tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
        }

        public void bind(StoryResponse story) {
            tvTitle.setText(story.getTitle());
            tvDescription.setText(story.getDescription() != null ? story.getDescription() : "Нет описания");
            tvAuthor.setText("ID автора: " + story.getAuthorId());
        }
    }
}

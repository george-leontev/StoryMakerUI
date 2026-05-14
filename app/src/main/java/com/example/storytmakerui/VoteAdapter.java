package com.example.storytmakerui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storytmakerui.api.models.UserVoteResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.VoteViewHolder> {

    private final List<UserVoteResponse> votes;

    public VoteAdapter(List<UserVoteResponse> votes) {
        this.votes = votes;
    }

    @NonNull
    @Override
    public VoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vote, parent, false);
        return new VoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoteViewHolder holder, int position) {
        UserVoteResponse vote = votes.get(position);
        holder.bind(vote);
    }

    @Override
    public int getItemCount() {
        return votes.size();
    }

    static class VoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStoryTitle;
        private final TextView tvChapterTitle;
        private final TextView tvYourChoice;
        private final LinearLayout llResults;
        private final TextView tvOption1Votes;
        private final TextView tvOption2Votes;
        private final TextView tvWinner;
        private final TextView tvVotingActive;
        private final TextView tvVotedAt;

        public VoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            tvYourChoice = itemView.findViewById(R.id.tvYourChoice);
            llResults = itemView.findViewById(R.id.llResults);
            tvOption1Votes = itemView.findViewById(R.id.tvOption1Votes);
            tvOption2Votes = itemView.findViewById(R.id.tvOption2Votes);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            tvVotingActive = itemView.findViewById(R.id.tvVotingActive);
            tvVotedAt = itemView.findViewById(R.id.tvVotedAt);
        }

        public void bind(UserVoteResponse vote) {
            tvStoryTitle.setText(vote.getStoryTitle() != null ? vote.getStoryTitle() : "Unknown story");
            tvChapterTitle.setText("Глава #" + vote.getChapterId());

            String votedOptionText = vote.getVotedOption() == 1
                    ? vote.getOption1Text()
                    : vote.getOption2Text();
            tvYourChoice.setText("Ваш выбор: " + (votedOptionText != null ? votedOptionText : "Неизвестно"));

            if (vote.isClosed()) {
                llResults.setVisibility(View.VISIBLE);
                tvVotingActive.setVisibility(View.GONE);

                Integer v1 = vote.getOption1Votes();
                Integer v2 = vote.getOption2Votes();

                tvOption1Votes.setText("Вариант 1: " + (v1 != null ? v1 : 0) + " голосов");
                tvOption2Votes.setText("Вариант 2: " + (v2 != null ? v2 : 0) + " голосов");

                if (vote.getWinningOption() != null) {
                    String winnerText = vote.getWinningOption() == 1
                            ? vote.getOption1Text()
                            : vote.getOption2Text();
                    tvWinner.setText("Победил: " + winnerText);
                } else {
                    tvWinner.setText("Ничья!");
                }
            } else {
                llResults.setVisibility(View.GONE);
                tvVotingActive.setVisibility(View.VISIBLE);
            }

            tvVotedAt.setText("Голосовано: " + formatDate(vote.getVotedAt()));
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
    }
}

package com.example.storytmakerui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storytmakerui.api.models.VoteHistoryResponse;

import java.util.ArrayList;
import java.util.List;

public class AuthorVoteAdapter extends RecyclerView.Adapter<AuthorVoteAdapter.AuthorVoteViewHolder> {

    private List<VoteHistoryResponse> votesList;

    public AuthorVoteAdapter() {
        this.votesList = new ArrayList<>();
    }

    public void setVotesList(List<VoteHistoryResponse> votesList) {
        this.votesList = votesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuthorVoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_author_vote, parent, false);
        return new AuthorVoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorVoteViewHolder holder, int position) {
        VoteHistoryResponse vote = votesList.get(position);
        holder.bind(vote);
    }

    @Override
    public int getItemCount() {
        return votesList.size();
    }

    static class AuthorVoteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStoryTitle;
        private TextView tvChapterTitle;
        private TextView tvOption1;
        private TextView tvOption2;
        private TextView tvOption1Votes;
        private TextView tvOption2Votes;
        private TextView tvWinner;
        private TextView tvVotingActive;
        private TextView tvVotingClosed;
        private LinearLayout llResults;

        public AuthorVoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            tvOption1 = itemView.findViewById(R.id.tvOption1);
            tvOption2 = itemView.findViewById(R.id.tvOption2);
            tvOption1Votes = itemView.findViewById(R.id.tvOption1Votes);
            tvOption2Votes = itemView.findViewById(R.id.tvOption2Votes);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            tvVotingActive = itemView.findViewById(R.id.tvVotingActive);
            tvVotingClosed = itemView.findViewById(R.id.tvVotingClosed);
            llResults = itemView.findViewById(R.id.llResults);
        }

        public void bind(VoteHistoryResponse vote) {
            tvStoryTitle.setText(vote.getStoryTitle() != null ? vote.getStoryTitle() : "Без названия");
            tvChapterTitle.setText(vote.getChapterTitle() != null ? vote.getChapterTitle() : "Без названия главы");

            // Получаем текст голосов из voteText (должен содержать опции через \n)
            if (vote.getVoteText() != null) {
                String[] options = vote.getVoteText().split("\n");
                if (options.length >= 2) {
                    tvOption1.setText("Вариант 1: " + options[0].trim());
                    tvOption2.setText("Вариант 2: " + options[1].trim());
                } else {
                    tvOption1.setText("Вариант 1: " + vote.getVoteText());
                    tvOption2.setText("Вариант 2");
                }
            } else {
                tvOption1.setText("Вариант 1");
                tvOption2.setText("Вариант 2");
            }

            // Показываем результаты
            if (vote.isClosed()) {
                // Голосование завершено - показываем результаты
                tvVotingClosed.setVisibility(View.VISIBLE);
                tvVotingActive.setVisibility(View.GONE);
                llResults.setVisibility(View.VISIBLE);

                int option1Votes = vote.getOption1Votes() != null ? vote.getOption1Votes() : 0;
                int option2Votes = vote.getOption2Votes() != null ? vote.getOption2Votes() : 0;

                tvOption1Votes.setText("Голосов: " + option1Votes);
                tvOption2Votes.setText("Голосов: " + option2Votes);

                // Показываем победителя
                if (vote.getWinningOption() != null) {
                    tvWinner.setVisibility(View.VISIBLE);
                    if (vote.getWinningOption() == 1) {
                        tvWinner.setText("Победил: Вариант 1");
                    } else {
                        tvWinner.setText("Победил: Вариант 2");
                    }
                }
            } else {
                // Голосование активно - скрываем результаты
                tvVotingActive.setVisibility(View.VISIBLE);
                tvVotingClosed.setVisibility(View.GONE);
                llResults.setVisibility(View.GONE);
            }
        }
    }
}

package com.example.storytmakerui.api.models;

public class RatingRequest {
    private int score;

    public RatingRequest() {}

    public RatingRequest(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

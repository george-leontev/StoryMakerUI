package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.RatingResponse;
import com.example.storytmakerui.api.models.RatingRequest;
import com.example.storytmakerui.api.services.RatingService;

public class RatingRepository {
    private final RatingService ratingService;

    public RatingRepository() {
        this.ratingService = ApiClient.create(RatingService.class);
    }

    public Result<RatingResponse> getRating(int storyId) {
        try {
            var response = ratingService.getRating(storyId).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения рейтинга: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<RatingResponse> setRating(int storyId, int score) {
        try {
            RatingRequest request = new RatingRequest(score);
            var response = ratingService.setRating(storyId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка установки рейтинга: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

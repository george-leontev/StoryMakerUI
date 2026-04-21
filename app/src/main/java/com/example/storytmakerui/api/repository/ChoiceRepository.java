package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.ChoiceResponse;
import com.example.storytmakerui.api.models.CreateChoiceRequest;
import com.example.storytmakerui.api.models.VoteRequest;
import com.example.storytmakerui.api.services.ChoiceService;

public class ChoiceRepository {
    private final ChoiceService choiceService;

    public ChoiceRepository() {
        this.choiceService = ApiClient.create(ChoiceService.class);
    }

    public Result<ChoiceResponse> getChoice(int chapterId) {
        try {
            var response = choiceService.getChoice(chapterId).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения выбора: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<ChoiceResponse> createChoice(int chapterId, String option1Text, String option2Text, int durationInMinutes) {
        try {
            CreateChoiceRequest request = new CreateChoiceRequest(option1Text, option2Text, durationInMinutes);
            var response = choiceService.createChoice(chapterId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка создания выбора: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<ChoiceResponse> vote(int chapterId, int choiceId, int option) {
        try {
            VoteRequest request = new VoteRequest(option);
            var response = choiceService.vote(chapterId, choiceId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка голосования: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

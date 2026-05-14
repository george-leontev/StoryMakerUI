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
            var response = choiceService.getChoiceByChapterId(chapterId).execute();
            
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

    // Метод для автора: получить результаты голосования (видит голоса всегда)
    public Result<ChoiceResponse> getChoiceForAuthor(int chapterId, int choiceId) {
        try {
            var response = choiceService.getChoiceForAuthor(chapterId, choiceId).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                // Преобразуем ChoiceAuthorResponse в ChoiceResponse (поля те же)
                var authorResponse = response.body();
                ChoiceResponse choiceResponse = new ChoiceResponse();
                choiceResponse.setId(authorResponse.getId());
                choiceResponse.setChapterId(authorResponse.getChapterId());
                choiceResponse.setOption1Text(authorResponse.getOption1Text());
                choiceResponse.setOption2Text(authorResponse.getOption2Text());
                choiceResponse.setOption1Votes(authorResponse.getOption1Votes());
                choiceResponse.setOption2Votes(authorResponse.getOption2Votes());
                choiceResponse.setWinningOption(authorResponse.getWinningOption());
                choiceResponse.setExpiresAt(authorResponse.getExpiresAt());
                choiceResponse.setClosed(authorResponse.isClosed());
                
                return Result.success(choiceResponse);
            } else {
                return Result.failure(new Exception("Ошибка получения результатов: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

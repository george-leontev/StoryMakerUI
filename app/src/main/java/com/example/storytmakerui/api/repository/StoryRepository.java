package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.*;
import com.example.storytmakerui.api.services.StoryService;

import java.util.List;

public class StoryRepository {
    private final StoryService storyService;

    public StoryRepository() {
        this.storyService = ApiClient.create(StoryService.class);
    }

    public Result<PagedResponse<StoryResponse>> getStories(int page, int pageSize) {
        try {
            var response = storyService.getStories(page, pageSize).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения историй: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<PagedResponse<StoryResponse>> getStories() {
        return getStories(1, 20);
    }

    public Result<List<StoryResponse>> getMyStories() {
        try {
            var response = storyService.getMyStories().execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения моих историй: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<StoryResponse> createStory(String title, String description) {
        try {
            CreateStoryRequest request = new CreateStoryRequest(title, description);
            var response = storyService.createStory(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка создания истории: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<StoryResponse> updateStory(int id, String title, String description) {
        try {
            CreateStoryRequest request = new CreateStoryRequest(title, description);
            var response = storyService.updateStory(id, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка обновления истории: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<Void> deleteStory(int id) {
        try {
            var response = storyService.deleteStory(id).execute();
            
            if (response.isSuccessful()) {
                return Result.success(null);
            } else {
                return Result.failure(new Exception("Ошибка удаления истории: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

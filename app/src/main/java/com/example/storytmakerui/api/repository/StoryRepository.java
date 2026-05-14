package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.CreateStoryRequest;
import com.example.storytmakerui.api.models.PagedResponse;
import com.example.storytmakerui.api.models.StoryResponse;
import com.example.storytmakerui.api.services.StoryService;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
                return Result.failure(new Exception("Error getting stories: " + response.code()));
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
                return Result.failure(new Exception("Error getting my stories: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    // Создание истории (обложка опциональна)
    public Result<StoryResponse> createStory(String title, String description, File coverFile) {
        try {
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);

            retrofit2.Response<StoryResponse> response;
            if (coverFile != null) {
                String mime = getMediaType(coverFile.getName());
                MultipartBody.Part coverPart = MultipartBody.Part.createFormData(
                        "coverImage",
                        coverFile.getName(),
                        RequestBody.create(MediaType.parse(mime), coverFile));
                response = storyService.createStoryWithCover(titleBody, descBody, coverPart).execute();
            } else {
                response = storyService.createStory(titleBody, descBody).execute();
            }

            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            }
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error details";
            return Result.failure(new Exception("HTTP " + response.code() + ": " + errorBody));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    // Метод без обложки для совместимости (тоже multipart)
    public Result<StoryResponse> createStory(String title, String description) {
        return createStory(title, description, null);
    }
    private String getMediaType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    public Result<StoryResponse> updateStory(int id, String title, String description) {
        return updateStory(id, title, description, null);
    }

    public Result<StoryResponse> updateStory(int id, String title, String description, File coverFile) {
        try {
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);

            MultipartBody.Part coverPart = null;
            if (coverFile != null) {
                String mime = getMediaType(coverFile.getName());
                coverPart = MultipartBody.Part.createFormData(
                        "coverImage",
                        coverFile.getName(),
                        RequestBody.create(MediaType.parse(mime), coverFile));
            }

            var response = storyService.updateStory(id, titleBody, descBody, coverPart).execute();

            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            }
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error details";
            return Result.failure(new Exception("HTTP " + response.code() + ": " + errorBody));
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
                return Result.failure(new Exception("Error deleting story: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}
package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.*;
import com.example.storytmakerui.api.services.ChapterService;

import java.util.List;

public class ChapterRepository {
    private final ChapterService chapterService;

    public ChapterRepository() {
        this.chapterService = ApiClient.create(ChapterService.class);
    }

    public Result<PagedResponse<ChapterResponse>> getChapters(int storyId, int page, int pageSize) {
        try {
            var response = chapterService.getChapters(storyId, page, pageSize).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения глав: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<PagedResponse<ChapterResponse>> getChapters(int storyId) {
        return getChapters(storyId, 1, 20);
    }

    public Result<ChapterResponse> createChapter(int storyId, String title, String content, Integer sequenceNumber) {
        try {
            CreateChapterRequest request = new CreateChapterRequest(title, content, sequenceNumber);
            var response = chapterService.createChapter(storyId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            }
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error details";
            return Result.failure(new Exception("HTTP " + response.code() + ": " + errorBody));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

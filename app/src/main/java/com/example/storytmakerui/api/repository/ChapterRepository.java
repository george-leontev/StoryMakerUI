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

    public Result<ChapterResponse> createChapter(int storyId, String content, Integer sequenceNumber) {
        try {
            CreateChapterRequest request = new CreateChapterRequest(content, sequenceNumber);
            var response = chapterService.createChapter(storyId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка создания главы: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<ChapterResponse> createChapter(int storyId, String content) {
        return createChapter(storyId, content, null);
    }
}

package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.CommentResponse;
import com.example.storytmakerui.api.models.CreateCommentRequest;
import com.example.storytmakerui.api.models.PagedResponse;
import com.example.storytmakerui.api.services.CommentService;

import java.util.List;

public class CommentRepository {
    private final CommentService commentService;

    public CommentRepository() {
        this.commentService = ApiClient.create(CommentService.class);
    }

    public Result<PagedResponse<CommentResponse>> getComments(int storyId, int page, int pageSize) {
        try {
            var response = commentService.getComments(storyId, page, pageSize).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка получения комментариев: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<PagedResponse<CommentResponse>> getComments(int storyId) {
        return getComments(storyId, 1, 20);
    }

    public Result<CommentResponse> createComment(int storyId, String content) {
        try {
            CreateCommentRequest request = new CreateCommentRequest(content);
            var response = commentService.createComment(storyId, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                return Result.failure(new Exception("Ошибка создания комментария: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<Void> deleteComment(int storyId, int commentId) {
        try {
            var response = commentService.deleteComment(storyId, commentId).execute();
            
            if (response.isSuccessful()) {
                return Result.success(null);
            } else {
                return Result.failure(new Exception("Ошибка удаления комментария: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}

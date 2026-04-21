package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.CommentResponse;
import com.example.storytmakerui.api.models.CreateCommentRequest;
import com.example.storytmakerui.api.models.PagedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CommentService {
    @GET("api/stories/{storyId}/comments")
    Call<PagedResponse<CommentResponse>> getComments(
            @Path("storyId") int storyId,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @POST("api/stories/{storyId}/comments")
    Call<CommentResponse> createComment(
            @Path("storyId") int storyId,
            @Body CreateCommentRequest request
    );

    @DELETE("api/stories/{storyId}/comments/{commentId}")
    Call<Void> deleteComment(
            @Path("storyId") int storyId,
            @Path("commentId") int commentId
    );
}

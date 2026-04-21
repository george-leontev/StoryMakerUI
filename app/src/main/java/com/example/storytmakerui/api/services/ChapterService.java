package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.ChapterResponse;
import com.example.storytmakerui.api.models.CreateChapterRequest;
import com.example.storytmakerui.api.models.PagedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChapterService {
    @GET("api/stories/{storyId}/chapters")
    Call<PagedResponse<ChapterResponse>> getChapters(
            @Path("storyId") int storyId,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @POST("api/stories/{storyId}/chapters")
    Call<ChapterResponse> createChapter(
            @Path("storyId") int storyId,
            @Body CreateChapterRequest request
    );
}

package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.CreateStoryRequest;
import com.example.storytmakerui.api.models.PagedResponse;
import com.example.storytmakerui.api.models.StoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StoryService {
    @GET("api/story")
    Call<PagedResponse<StoryResponse>> getStories(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/story/me")
    Call<List<StoryResponse>> getMyStories();

    @POST("api/story")
    Call<StoryResponse> createStory(@Body CreateStoryRequest request);

    @PUT("api/story/{id}")
    Call<StoryResponse> updateStory(@Path("id") int id, @Body CreateStoryRequest request);

    @DELETE("api/story/{id}")
    Call<Void> deleteStory(@Path("id") int id);
}

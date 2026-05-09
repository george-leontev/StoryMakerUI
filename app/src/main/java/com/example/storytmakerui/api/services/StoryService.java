package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.CreateStoryRequest;
import com.example.storytmakerui.api.models.PagedResponse;
import com.example.storytmakerui.api.models.StoryResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
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

    // Multipart запрос для создания истории без обложки
    @Multipart
    @POST("api/story")
    Call<StoryResponse> createStory(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description
    );

    // Multipart запрос для создания истории с обложкой
    @Multipart
    @POST("api/story")
    Call<StoryResponse> createStoryWithCover(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("coverImage") MultipartBody.Part coverImage
    );

    @Multipart
    @PUT("api/story/{id}")
    Call<StoryResponse> updateStory(
            @Path("id") int id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description
    );

    @Multipart
    @PUT("api/story/{id}")
    Call<StoryResponse> updateStoryWithCover(
            @Path("id") int id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part coverImage
    );

    @DELETE("api/story/{id}")
    Call<Void> deleteStory(@Path("id") int id);
}
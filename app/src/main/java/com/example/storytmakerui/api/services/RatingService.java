package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.RatingResponse;
import com.example.storytmakerui.api.models.RatingRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RatingService {
    @GET("api/stories/{storyId}/rating")
    Call<RatingResponse> getRating(@Path("storyId") int storyId);

    @POST("api/stories/{storyId}/rating")
    Call<RatingResponse> setRating(
            @Path("storyId") int storyId,
            @Body RatingRequest request
    );
}

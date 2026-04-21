package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.ChoiceResponse;
import com.example.storytmakerui.api.models.CreateChoiceRequest;
import com.example.storytmakerui.api.models.VoteRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChoiceService {
    @GET("api/chapters/{chapterId}/choices")
    Call<ChoiceResponse> getChoice(@Path("chapterId") int chapterId);

    @POST("api/chapters/{chapterId}/choices")
    Call<ChoiceResponse> createChoice(
            @Path("chapterId") int chapterId,
            @Body CreateChoiceRequest request
    );

    @POST("api/chapters/{chapterId}/choices/{choiceId}/vote")
    Call<ChoiceResponse> vote(
            @Path("chapterId") int chapterId,
            @Path("choiceId") int choiceId,
            @Body VoteRequest request
    );
}

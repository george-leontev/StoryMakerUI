package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.UpdatePasswordRequest;
import com.example.storytmakerui.api.models.UpdateUserRequest;
import com.example.storytmakerui.api.models.UserResponse;
import com.example.storytmakerui.api.models.VoteHistoryResponse;
import com.example.storytmakerui.api.models.PagedResponse;

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
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {
    @GET("api/user")
    Call<com.example.storytmakerui.api.models.ProfileResponse> getProfile();

    @PUT("api/user")
    Call<com.example.storytmakerui.api.models.ProfileResponse> updateProfile(@Body com.example.storytmakerui.api.models.UpdateProfileRequest request);

    @Multipart
    @POST("api/user/avatar")
    Call<com.example.storytmakerui.api.models.ProfileResponse> uploadAvatar(@Part MultipartBody.Part avatar);

    @DELETE("api/user/avatar")
    Call<com.example.storytmakerui.api.models.ProfileResponse> deleteAvatar();

    @PUT("api/user/password")
    Call<com.example.storytmakerui.api.models.ProfileResponse> changePassword(@Body UpdatePasswordRequest request);

    @GET("api/story/me")
    Call<PagedResponse<com.example.storytmakerui.api.models.StoryResponse>> getMyStories();

    @GET("api/user/votes")
    Call<PagedResponse<VoteHistoryResponse>> getMyVotes(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/user/author-votes")
    Call<PagedResponse<VoteHistoryResponse>> getAuthorVotes(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
}

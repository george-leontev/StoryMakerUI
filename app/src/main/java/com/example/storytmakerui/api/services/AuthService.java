package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.models.LoginRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AuthService {
    @Multipart
    @POST("api/auth/register")
    Call<AuthResponse> register(
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );

    @Multipart
    @POST("api/auth/register")
    Call<AuthResponse> registerWithAvatar(
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part avatar
    );

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}

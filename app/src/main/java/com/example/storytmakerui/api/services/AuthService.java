package com.example.storytmakerui.api.services;

import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.models.LoginRequest;
import com.example.storytmakerui.api.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}

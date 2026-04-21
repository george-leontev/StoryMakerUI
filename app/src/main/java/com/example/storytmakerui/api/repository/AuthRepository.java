package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.models.LoginRequest;
import com.example.storytmakerui.api.models.RegisterRequest;
import com.example.storytmakerui.api.services.AuthService;

import retrofit2.Response;

public class AuthRepository {
    private final AuthService authService;

    public AuthRepository() {
        this.authService = ApiClient.create(AuthService.class);
    }

    public Result<AuthResponse> register(String username, String email, String password) {
        try {
            RegisterRequest request = new RegisterRequest(username, email, password);
            Response<AuthResponse> response = authService.register(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                ApiClient.saveAuthToken(authResponse.getToken());
                return Result.success(authResponse);
            } else {
                return Result.failure(new Exception("Ошибка регистрации: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<AuthResponse> login(String email, String password) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            Response<AuthResponse> response = authService.login(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                ApiClient.saveAuthToken(authResponse.getToken());
                return Result.success(authResponse);
            } else {
                return Result.failure(new Exception("Ошибка входа: " + response.code()));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public boolean isAuthenticated() {
        return ApiClient.getAuthToken() != null;
    }

    public void logout() {
        ApiClient.clearAuthToken();
    }
}

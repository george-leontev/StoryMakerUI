package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.AuthResponse;
import com.example.storytmakerui.api.models.LoginRequest;
import com.example.storytmakerui.api.services.AuthService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class AuthRepository {
    private final AuthService authService;

    public AuthRepository() {
        this.authService = ApiClient.create(AuthService.class);
    }

    public Result<AuthResponse> register(String username, String email, String password) {
        return register(username, email, password, null);
    }

    public Result<AuthResponse> register(String username, String email, String password, File avatarFile) {
        try {
            RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
            RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
            RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

            Response<AuthResponse> response;
            if (avatarFile != null) {
                String mime = guessImageMime(avatarFile.getName());
                MultipartBody.Part avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        avatarFile.getName(),
                        RequestBody.create(MediaType.parse(mime), avatarFile));
                response = authService.registerWithAvatar(usernameBody, emailBody, passwordBody, avatarPart).execute();
            } else {
                response = authService.register(usernameBody, emailBody, passwordBody).execute();
            }

            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                ApiClient.saveAuthToken(authResponse.getToken());
                return Result.success(authResponse);
            }
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
            return Result.failure(new Exception("HTTP " + response.code() + ": " + errorBody));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private String guessImageMime(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
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

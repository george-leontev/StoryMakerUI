package com.example.storytmakerui.api.repository;

import com.example.storytmakerui.api.ApiClient;
import com.example.storytmakerui.api.models.*;
import com.example.storytmakerui.api.services.UserService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class UserRepository {
    private final UserService userService;

    public UserRepository() {
        this.userService = ApiClient.create(UserService.class);
    }

    public Result<com.example.storytmakerui.api.models.ProfileResponse> getProfile() {
        try {
            Response<com.example.storytmakerui.api.models.ProfileResponse> response = userService.getProfile().execute();
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка получения профиля: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.ProfileResponse> updateProfile(String username, String email) {
        try {
            com.example.storytmakerui.api.models.UpdateProfileRequest request = 
                new com.example.storytmakerui.api.models.UpdateProfileRequest(username, email);
            Response<com.example.storytmakerui.api.models.ProfileResponse> response = userService.updateProfile(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка обновления профиля: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.ProfileResponse> uploadAvatar(File avatarFile) {
        try {
            String mime = guessImageMime(avatarFile.getName());
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData(
                    "avatar",
                    avatarFile.getName(),
                    RequestBody.create(MediaType.parse(mime), avatarFile));
            
            Response<com.example.storytmakerui.api.models.ProfileResponse> response = userService.uploadAvatar(avatarPart).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка загрузки аватара: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.ProfileResponse> deleteAvatar() {
        try {
            Response<com.example.storytmakerui.api.models.ProfileResponse> response = userService.deleteAvatar().execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка удаления аватара: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.ProfileResponse> changePassword(String currentPassword, String newPassword) {
        try {
            com.example.storytmakerui.api.models.UpdatePasswordRequest request = 
                new com.example.storytmakerui.api.models.UpdatePasswordRequest(currentPassword, newPassword);
            Response<com.example.storytmakerui.api.models.ProfileResponse> response = userService.changePassword(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка смены пароля: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<PagedResponse<StoryResponse>> getMyStories() {
        try {
            Response<PagedResponse<StoryResponse>> response = userService.getMyStories().execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка получения историй: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.PagedResponse<VoteHistoryResponse>> getMyVotes(int page, int pageSize) {
        try {
            Response<com.example.storytmakerui.api.models.PagedResponse<VoteHistoryResponse>> response = userService.getMyVotes(page, pageSize).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                return Result.success(response.body());
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                return Result.failure(new Exception("Ошибка получения голосов: " + response.code() + " - " + errorBody));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<com.example.storytmakerui.api.models.PagedResponse<VoteHistoryResponse>> getMyVotes() {
        return getMyVotes(1, 20);
    }

    private String guessImageMime(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}

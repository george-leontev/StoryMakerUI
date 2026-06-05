package com.example.storytmakerui.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Тело POST /api/stories/{storyId}/comments.
 * Сервер ожидает поле "text" (камелкейс), Trim делает сам.
 */
public class CreateCommentRequest {
    @SerializedName("text")
    private String text;

    public CreateCommentRequest() {}

    public CreateCommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

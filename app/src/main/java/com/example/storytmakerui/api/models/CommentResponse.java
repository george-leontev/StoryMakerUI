package com.example.storytmakerui.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Ответ API на комментарий истории.
 * Контракт сервера (camelCase): id, storyId, userUsername, text, timestamp (UTC ISO-8601 с Z).
 * userId/аватар сервер НЕ возвращает — авторство определяем сравнением username.
 */
public class CommentResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("storyId")
    private int storyId;

    @SerializedName("userUsername")
    private String username;

    @SerializedName("text")
    private String text;

    @SerializedName("timestamp")
    private String timestamp;

    public CommentResponse() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStoryId() {
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

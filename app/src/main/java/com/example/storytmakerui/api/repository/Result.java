package com.example.storytmakerui.api.repository;

public class Result<T> {
    private final T data;
    private final Exception error;
    private final boolean success;

    private Result(T data, Exception error, boolean success) {
        this.data = data;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }

    public static <T> Result<T> failure(Exception error) {
        return new Result<>(null, error, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

    public T getOrNull() {
        return success ? data : null;
    }

    public void onSuccess(OnSuccessListener<T> listener) {
        if (success && listener != null) {
            listener.onSuccess(data);
        }
    }

    public void onFailure(OnFailureListener listener) {
        if (!success && listener != null) {
            listener.onFailure(error);
        }
    }

    @FunctionalInterface
    public interface OnSuccessListener<T> {
        void onSuccess(T data);
    }

    @FunctionalInterface
    public interface OnFailureListener {
        void onFailure(Exception error);
    }
}

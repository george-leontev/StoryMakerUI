package com.example.storytmakerui.api.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenProvider tokenProvider;

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = tokenProvider.getAuthToken();
        Request authenticatedRequest;
        
        if (token != null) {
            authenticatedRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        } else {
            authenticatedRequest = originalRequest;
        }

        return chain.proceed(authenticatedRequest);
    }

    public interface TokenProvider {
        String getAuthToken();
    }
}

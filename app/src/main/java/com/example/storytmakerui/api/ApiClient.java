package com.example.storytmakerui.api;

import android.content.Context;

import com.example.storytmakerui.api.interceptor.AuthInterceptor;
import com.example.storytmakerui.utils.PreferenceManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.70:5157/";

    private static Retrofit retrofit;
    private static PreferenceManager preferenceManager;

    public static void init(Context context) {
        preferenceManager = new PreferenceManager(context);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        AuthInterceptor authInterceptor = new AuthInterceptor(() -> preferenceManager.getAuthToken());

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T create(Class<T> serviceClass) {
        if (retrofit == null) {
            throw new IllegalStateException("ApiClient не инициализирован. Вызовите init() перед использованием.");
        }
        return retrofit.create(serviceClass);
    }

    public static String getAuthToken() {
        return preferenceManager != null ? preferenceManager.getAuthToken() : null;
    }

    public static void saveAuthToken(String token) {
        if (preferenceManager != null) {
            preferenceManager.saveAuthToken(token);
        }
    }

    public static void clearAuthToken() {
        if (preferenceManager != null) {
            preferenceManager.clearAuthToken();
        }
    }
}

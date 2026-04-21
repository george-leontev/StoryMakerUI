package com.example.storytmakerui;

import android.app.Application;
import com.example.storytmakerui.api.ApiClient;

public class StoryMakerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация API клиента при запуске приложения
        ApiClient.init(this);
    }
}

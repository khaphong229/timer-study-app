package com.example.timerstudy.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Use 10.0.2.2 for Android Emulator (maps to host machine's localhost)
    // Use your machine's IP address (e.g., 192.168.x.x:8669) for physical devices
    private static final String BASE_URL = "http://192.168.0.100:8669/";
    private static Retrofit retrofit = null;

    public static ApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

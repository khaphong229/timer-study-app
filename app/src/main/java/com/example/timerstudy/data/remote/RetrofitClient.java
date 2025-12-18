package com.example.timerstudy.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // --- CẤU HÌNH KẾT NỐI ---
    // Đặt là true nếu chạy trên Máy ảo (Emulator)
    // Đặt là false nếu chạy trên Máy thật (Real Device)
    private static final boolean IS_EMULATOR = true;

    // IP của máy tính khi chạy máy thật (Thay đổi theo mạng Wifi của bạn)
    // Cách xem IP: Mở CMD -> gõ ipconfig -> xem dòng IPv4 Address
    private static final String LOCAL_IP = "192.168.0.118";
    private static final String PORT = "8669";

    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:" + PORT + "/";
    private static final String BASE_URL_DEVICE = "http://" + LOCAL_IP + ":" + PORT + "/";

    private static final String BASE_URL = IS_EMULATOR ? BASE_URL_EMULATOR : BASE_URL_DEVICE;
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        android.util.Log.d("RetrofitClient", "Initialized with BASE_URL: " + BASE_URL);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}

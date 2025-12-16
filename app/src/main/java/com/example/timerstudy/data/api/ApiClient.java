package com.example.timerstudy.data.api;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;
    private static String accessToken = null;

    /**
     * Get Retrofit instance
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
                Log.d(TAG, "OkHttp: " + message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttp client with timeout and logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        // Add authorization header if access token exists
                        okhttp3.Request request = chain.request();
                        if (accessToken != null && !accessToken.isEmpty()) {
                            request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer " + accessToken)
                                    .build();
                        }
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    /**
     * Set access token for authenticated requests
     */
    public static void setAccessToken(String token) {
        accessToken = token;
        Log.d(TAG, "Access token updated");
        // Reset retrofit instance to apply new token
        retrofit = null;
    }

    /**
     * Clear access token (for logout)
     */
    public static void clearAccessToken() {
        accessToken = null;
        retrofit = null;
        Log.d(TAG, "Access token cleared");
    }

    /**
     * Get current access token
     */
    public static String getAccessToken() {
        return accessToken;
    }
}

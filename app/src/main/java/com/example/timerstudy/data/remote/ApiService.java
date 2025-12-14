package com.example.timerstudy.data.remote;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/user-entity/register")
    Call<ApiResponse<UserResponseData>> register(@Body RegisterRequest request);

    @POST("api/auth/user-entity/login")
    Call<ApiResponse<LoginResponseData>> login(@Body LoginRequest request);

    // --- DTO Classes ---

    class ApiResponse<T> {
        @SerializedName("http_code")
        public int httpCode;
        @SerializedName("success")
        public boolean success;
        @SerializedName("message")
        public String message;
        @SerializedName("data")
        public T data;
    }

    class RegisterRequest {
        public String email;
        public String password;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("profile_picture_url")
        public String profilePictureUrl;

        public RegisterRequest(String email, String password, String displayName, String profilePictureUrl) {
            this.email = email;
            this.password = password;
            this.displayName = displayName;
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class UserResponseData {
        @SerializedName("user_id")
        public int userId;
        public String email;
        @SerializedName("display_name")
        public String displayName;
    }

    class LoginResponseData {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("user")
        public Object user; // Hoặc map chi tiết nếu cần
    }
}

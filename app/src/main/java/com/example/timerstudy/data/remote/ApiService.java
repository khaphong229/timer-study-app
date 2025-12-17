package com.example.timerstudy.data.remote;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.example.timerstudy.model.Session;
import com.example.timerstudy.model.User;

import java.util.List;
import retrofit2.http.Header;

public interface ApiService {

    @POST("api/users/sync")
    Call<User> syncUser(@Header("Authorization") String token, @Body User user);

    @POST("api/auth/user-entity/register")
    Call<ApiResponse<UserResponseData>> register(@Body RegisterRequest request);

    @POST("api/auth/user-entity/login-firebase")
    Call<ApiResponse<LoginResponseData>> loginFirebase(@Body LoginFirebaseRequest request);

    // --- SESSIONS ---
    // Tạo phiên học mới trên server
    @POST("sessions")
    Call<Session> createSession(@Header("Authorization") String token, @Body Session session);

    // --- DTO Classes ---

    class ApiResponse<T> {
        @SerializedName("http_code")
        public int httpCode;
        @SerializedName("success")
        public boolean success;
        @SerializedName("message")
        public String message;
        @SerializedName("metadata")
        public Metadata metadata;
        @SerializedName("data")
        public T data;
    }

    class Metadata {
        public int page;
        @SerializedName("page_size")
        public int pageSize;
        public int total;
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

    class LoginFirebaseRequest {
        @SerializedName("firebase_id_token")
        public String firebaseIdToken;

        public LoginFirebaseRequest(String firebaseIdToken) {
            this.firebaseIdToken = firebaseIdToken;
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
        @SerializedName("refresh_token")
        public String refreshToken;
        @SerializedName("expires_in")
        public long expiresIn;
        @SerializedName("refresh_expires_in")
        public long refreshExpiresIn;
        @SerializedName("token_type")
        public String tokenType;
        @SerializedName("user")
        public UserInfo user;
    }

    class UserInfo {
        @SerializedName("user_id")
        public int userId;
        @SerializedName("email")
        public String email;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("profile_picture_url")
        public String profilePictureUrl;
        @SerializedName("created_at")
        public double createdAt;
        @SerializedName("last_login")
        public double lastLogin;
        @SerializedName("is_anonymous")
        public int isAnonymous;
        @SerializedName("updated_at")
        public double updatedAt;
    }
}

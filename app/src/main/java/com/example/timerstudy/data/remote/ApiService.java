package com.example.timerstudy.data.remote;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.example.timerstudy.model.Session;
import com.example.timerstudy.model.User;

import java.util.List;

public interface ApiService {

    @POST("api/users/sync")
    Call<User> syncUser(@Header("Authorization") String token, @Body User user);



    @POST("api/auth/user-entity/register")
    Call<ApiResponse<UserResponseData>> register(@Body RegisterRequest request);

    @POST("api/auth/user-entity/login-firebase")
    Call<ApiResponse<LoginResponseData>> loginFirebase(@Body LoginFirebaseRequest request);

    // --- SESSIONS ---

    // Lấy tất cả phiên học (không phân trang)
    @GET("api/v1/sessions/all")
    Call<ApiResponse<List<Session>>> getAllSessions(@Header("Authorization") String token);

    // Lấy danh sách phiên học (có phân trang)
    @GET("api/v1/sessions")
    Call<ApiResponse<List<Session>>> getSessions(
            @Header("Authorization") String token,
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize,
            @Query("sort_by") String sortBy,
            @Query("order") String order
    );

    // Tạo phiên học mới trên server
    @POST("api/v1/sessions")
    Call<ApiResponse<Session>> createSession(@Header("Authorization") String token, @Body Session session);

    // Lấy chi tiết phiên học
    @GET("api/v1/sessions/{session_id}")
    Call<ApiResponse<Session>> getSessionDetail(@Header("Authorization") String token, @Path("session_id") int sessionId);

    // Cập nhật toàn bộ phiên học
    @PUT("api/v1/sessions/{session_id}")
    Call<ApiResponse<Session>> updateSession(@Header("Authorization") String token, @Path("session_id") int sessionId, @Body Session session);

    // Cập nhật một phần phiên học
    @PATCH("api/v1/sessions/{session_id}")
    Call<ApiResponse<Session>> patchSession(@Header("Authorization") String token, @Path("session_id") int sessionId, @Body Session session);

    // Xóa phiên học
    @DELETE("api/v1/sessions/{session_id}")
    Call<ApiResponse<Void>> deleteSession(@Header("Authorization") String token, @Path("session_id") int sessionId);

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
        public long userId;
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
        public double expiresIn;
        @SerializedName("refresh_expires_in")
        public double refreshExpiresIn;
        @SerializedName("token_type")
        public String tokenType;
        @SerializedName("user")
        public Object user; 
    }
}

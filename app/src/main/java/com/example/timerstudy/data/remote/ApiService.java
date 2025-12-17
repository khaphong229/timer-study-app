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
import retrofit2.http.Header;
import retrofit2.http.GET;
import retrofit2.http.Query;

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
            @Query("order") String order);

    // Tạo phiên học mới trên server
    @POST("sessions")
    Call<Session> createSession(@Header("Authorization") String token, @Body Session session);

    // --- LEADERBOARD ---
    @GET("api/v1/leaderboard/facebook-friends")
    Call<ApiResponse<LeaderboardData>> getFacebookFriendsLeaderboard(
            @Header("Authorization") String token,
            @Query("period") String period,
            @Query("metric") String metric,
            @Query("limit") int limit,
            @Query("include_self") boolean includeSelf);

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

    class LeaderboardData {
        @SerializedName("period")
        public String period;
        @SerializedName("metric")
        public String metric;
        @SerializedName("current_user_rank")
        public int currentUserRank;
        @SerializedName("total_participants")
        public int totalParticipants;
        @SerializedName("entries")
        public List<LeaderboardEntry> entries;
        @SerializedName("note")
        public String note;
    }

    class LeaderboardEntry {
        @SerializedName("rank")
        public int rank;
        @SerializedName("user_id")
        public int userId;
        @SerializedName("display_name")
        public String displayName;
        @SerializedName("profile_picture_url")
        public String profilePictureUrl;
        @SerializedName("facebook_user_id")
        public String facebookUserId;
        @SerializedName("is_current_user")
        public boolean isCurrentUser;
        @SerializedName("focus_time")
        public int focusTime;
        @SerializedName("sessions")
        public int sessions;
        @SerializedName("tasks")
        public int tasks;
        @SerializedName("current_streak")
        public int currentStreak;
        @SerializedName("best_streak")
        public int bestStreak;
        @SerializedName("goals")
        public int goals;
        @SerializedName("score")
        public int score;
    }
}

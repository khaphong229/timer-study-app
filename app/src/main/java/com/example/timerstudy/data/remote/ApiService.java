package com.example.timerstudy.data.remote;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Path;

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

    // --- STREAK API ---
    
    // 1. Lấy thông tin Streak Summary (khuyến nghị dùng)
    @GET("api/v1/statistics/streak/summary")
    Call<ApiResponse<StreakSummaryResponse>> getStreakSummary(@Header("Authorization") String token);
    
    // 2. Lấy thông tin Streak hiện tại (từ cache)
    @GET("api/v1/statistics/streak/current")
    Call<ApiResponse<StreakCurrentResponse>> getStreakCurrent(@Header("Authorization") String token);
    
    // 3. Lấy tất cả Streak Records
    @GET("api/v1/statistics/streak/all")
    Call<ApiResponse<List<StreakRecordResponse>>> getAllStreakRecords(@Header("Authorization") String token);
    
    // 4. Lấy Streak Records với filter/pagination
    @GET("api/v1/statistics/streak")
    Call<ApiResponse<List<StreakRecordResponse>>> getStreakRecords(
        @Header("Authorization") String token,
        @Query("page") Integer page,
        @Query("page_size") Integer pageSize,
        @Query("sort_by") String sortBy,
        @Query("sort_order") String sortOrder
    );
    
    // 5. Lấy Streak Record theo ngày cụ thể
    @GET("api/v1/statistics/streak/by-date")
    Call<ApiResponse<StreakRecordResponse>> getStreakByDate(
        @Header("Authorization") String token,
        @Query("date") Double date
    );
    
    // 6. Tạo/Cập nhật Streak Record (upsert)
    @POST("api/v1/statistics/streak")
    Call<ApiResponse<StreakRecordResponse>> upsertStreakRecord(
        @Header("Authorization") String token,
        @Body StreakRecordRequest request
    );
    
    // 7. Lấy Streak Record theo ID
    @GET("api/v1/statistics/streak/{streak_id}")
    Call<ApiResponse<StreakRecordResponse>> getStreakById(
        @Header("Authorization") String token,
        @Path("streak_id") int streakId
    );
    
    // 8. Cập nhật Streak Record
    @PUT("api/v1/statistics/streak/{streak_id}")
    Call<ApiResponse<StreakRecordResponse>> updateStreakRecord(
        @Header("Authorization") String token,
        @Path("streak_id") int streakId,
        @Body StreakRecordRequest request
    );
    
    @PATCH("api/v1/statistics/streak/{streak_id}")
    Call<ApiResponse<StreakRecordResponse>> patchStreakRecord(
        @Header("Authorization") String token,
        @Path("streak_id") int streakId,
        @Body StreakRecordRequest request
    );
    
    // 9. Xóa Streak Record
    @DELETE("api/v1/statistics/streak/{streak_id}")
    Call<ApiResponse<Void>> deleteStreakRecord(
        @Header("Authorization") String token,
        @Path("streak_id") int streakId
    );

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
        public double expiresIn; // Backend trả về double (có thể có phần thập phân)
        @SerializedName("refresh_expires_in")
        public double refreshExpiresIn; // Backend trả về double (có thể có phần thập phân)
        @SerializedName("token_type")
        public String tokenType;
        @SerializedName("user")
        public Object user; // Hoặc map chi tiết nếu cần
    }

    // --- STREAK MODEL CLASSES ---
    
    class StreakSummaryResponse {
        @SerializedName("current_streak")
        public int currentStreak;
        @SerializedName("best_streak")
        public int bestStreak;
        @SerializedName("total_active_days")
        public int totalActiveDays;
    }
    
    class StreakCurrentResponse {
        @SerializedName("current_streak")
        public int currentStreak;
        @SerializedName("best_streak")
        public int bestStreak;
    }
    
    class StreakRecordResponse {
        @SerializedName("streak_id")
        public int streakId;
        @SerializedName("user_id")
        public long userId;
        @SerializedName("streak_date")
        public double streakDate; // Timestamp as double
        @SerializedName("has_activity")
        public int hasActivity; // 0 or 1
        @SerializedName("session_count")
        public int sessionCount;
        @SerializedName("focus_time")
        public int focusTime;
    }
    
    class StreakRecordRequest {
        @SerializedName("streak_date")
        public double streakDate; // Timestamp as double
        @SerializedName("has_activity")
        public int hasActivity; // 0 or 1
        @SerializedName("session_count")
        public int sessionCount;
        @SerializedName("focus_time")
        public int focusTime;
        
        public StreakRecordRequest(double streakDate, int hasActivity, int sessionCount, int focusTime) {
            this.streakDate = streakDate;
            this.hasActivity = hasActivity;
            this.sessionCount = sessionCount;
            this.focusTime = focusTime;
        }
    }
}

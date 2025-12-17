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
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.PUT;

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

    // --- TASKS ---
    @GET("api/v1/tasks/all")
    Call<ApiResponse<List<TaskItem>>> getAllTasks(@Header("Authorization") String authorization);

    @POST("api/v1/tasks")
    Call<ApiResponse<TaskItem>> createTask(@Header("Authorization") String authorization,
                                           @Body TaskCreateRequest request);

    @PUT("api/v1/tasks/{task_id}")
    Call<ApiResponse<TaskItem>> updateTask(@Header("Authorization") String authorization,
                                           @Path("task_id") int taskId,
                                           @Body TaskUpdateRequest request);

    @DELETE("api/v1/tasks/{task_id}")
    Call<ApiResponse<Object>> deleteTask(@Header("Authorization") String authorization,
                                         @Path("task_id") int taskId);

    // --- SESSIONS ---
    @GET("api/v1/sessions/all")
    Call<ApiResponse<List<Session>>> getAllSessions(@Header("Authorization") String token);

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

    // --- LEADERBOARD ---
    @GET("api/v1/leaderboard/facebook-friends")
    Call<ApiResponse<LeaderboardData>> getFacebookFriendsLeaderboard(
            @Header("Authorization") String token,
            @Query("period") String period,
            @Query("metric") String metric,
            @Query("limit") int limit,
            @Query("include_self") boolean includeSelf);

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
        public long userId;
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
        public long userId;
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

    // --- Task DTO for API list ---
    class TaskItem {
        @SerializedName("task_id")
        public int taskId;
        @SerializedName("user_id")
        public long userId;
        @SerializedName("title")
        public String title;
        @SerializedName("description")
        public String description;
        @SerializedName("priority")
        public String priority;
        // Timestamps are epoch seconds (fractional)
        @SerializedName("task_date")
        public double taskDate;
        @SerializedName("is_completed")
        public int isCompleted; // 0/1
        @SerializedName("completed_at")
        public double completedAt;
        @SerializedName("total_time_spent")
        public int totalTimeSpent;
        @SerializedName("estimated_sessions")
        public int estimatedSessions;
        @SerializedName("actual_sessions")
        public int actualSessions;
        @SerializedName("order_index")
        public int orderIndex;
        @SerializedName("created_at")
        public double createdAt;
        @SerializedName("updated_at")
        public double updatedAt;
    }

    // --- Task DTO for Create ---
    class TaskCreateRequest {
        @SerializedName("title")
        public String title;
        @SerializedName("description")
        public String description;
        @SerializedName("priority")
        public String priority;
        // For create we send epoch milliseconds
        @SerializedName("task_date")
        public long taskDate;
        @SerializedName("is_completed")
        public int isCompleted;
        @SerializedName("completed_at")
        public long completedAt; // 0 if not completed
        @SerializedName("total_time_spent")
        public int totalTimeSpent;
        @SerializedName("estimated_sessions")
        public int estimatedSessions;
        @SerializedName("actual_sessions")
        public int actualSessions;
        @SerializedName("order_index")
        public int orderIndex;

        public TaskCreateRequest(String title,
                                 String description,
                                 String priority,
                                 long taskDate,
                                 int isCompleted,
                                 long completedAt,
                                 int totalTimeSpent,
                                 int estimatedSessions,
                                 int actualSessions,
                                 int orderIndex) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.taskDate = taskDate;
            this.isCompleted = isCompleted;
            this.completedAt = completedAt;
            this.totalTimeSpent = totalTimeSpent;
            this.estimatedSessions = estimatedSessions;
            this.actualSessions = actualSessions;
            this.orderIndex = orderIndex;
        }
    }

    // --- Task DTO for Update ---
    class TaskUpdateRequest {
        @SerializedName("title")
        public String title;
        @SerializedName("description")
        public String description;
        @SerializedName("priority")
        public String priority;
        @SerializedName("task_date")
        public long taskDate; // epoch ms
        @SerializedName("is_completed")
        public int isCompleted;
        @SerializedName("completed_at")
        public long completedAt; // 0 if not completed
        @SerializedName("total_time_spent")
        public int totalTimeSpent;
        @SerializedName("estimated_sessions")
        public int estimatedSessions;
        @SerializedName("actual_sessions")
        public int actualSessions;
        @SerializedName("order_index")
        public int orderIndex;

        public TaskUpdateRequest(String title,
                                 String description,
                                 String priority,
                                 long taskDate,
                                 int isCompleted,
                                 long completedAt,
                                 int totalTimeSpent,
                                 int estimatedSessions,
                                 int actualSessions,
                                 int orderIndex) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.taskDate = taskDate;
            this.isCompleted = isCompleted;
            this.completedAt = completedAt;
            this.totalTimeSpent = totalTimeSpent;
            this.estimatedSessions = estimatedSessions;
            this.actualSessions = actualSessions;
            this.orderIndex = orderIndex;
        }
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
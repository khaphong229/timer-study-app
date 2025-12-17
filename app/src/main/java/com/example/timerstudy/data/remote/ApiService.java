package com.example.timerstudy.data.remote;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
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
        public double expiresIn;
        @SerializedName("refresh_expires_in")
        public double refreshExpiresIn;
        @SerializedName("token_type")
        public String tokenType;
        @SerializedName("user")
        public Object user; // Hoặc map chi tiết nếu cần
    }

    // --- Task DTO for API list ---
    class TaskItem {
        @SerializedName("task_id")
        public int taskId;
        @SerializedName("user_id")
        public int userId;
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
}

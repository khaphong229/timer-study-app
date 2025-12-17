package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.callback.DataCallback;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

public class TaskRepository {
    // Khớp với Presenter: lấy task theo userId và ngày
    public void getTasksByUserIdAndDate(long userId, long taskDate, DataCallback<List<TaskEntity>> callback) {
        getTasksByUserAndDate(userId, taskDate, callback);
    }
    private final TaskDao taskDao;
    private final ExecutorService executor;
    private final UserRepository userRepository;

    public TaskRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
        executor = Executors.newSingleThreadExecutor();
        userRepository = UserRepository.getInstance(context);
    }

    public void getAllTasks(DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getAllTasks());
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUserId(long userId, DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getTasksByUserId(userId));
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUserAndDate(long userId, long taskDate, DataCallback<List<TaskEntity>> callback) {
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getTasksByUserAndDate(userId, taskDate));
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // =============== Remote API ===============
    public void fetchAllTasksFromApi(String accessToken, DataCallback<List<TaskEntity>> callback) {
        executor.execute(() -> {
            try {
                Log.d("TaskRepository", "=== FETCH ALL TASKS API CALL ===");

                ApiService api = RetrofitClient.getInstance().getApiService();
                String authHeader = accessToken != null && accessToken.startsWith("Bearer ")
                        ? accessToken
                        : (accessToken != null ? "Bearer " + accessToken : null);

                // LOG TOKEN
                Log.d("TaskRepository", "Access Token: " + (accessToken != null ? "EXISTS" : "NULL"));
                if (accessToken != null) {
                    Log.d("TaskRepository", "Token preview: " + accessToken.substring(0, Math.min(50, accessToken.length())) + "...");
                }
                Log.d("TaskRepository", "Auth Header: " + authHeader);

                Call<ApiService.ApiResponse<List<ApiService.TaskItem>>> call = api.getAllTasks(authHeader);

                // LOG URL
                Log.d("TaskRepository", "API URL: " + call.request().url());
                Log.d("TaskRepository", "HTTP Method: " + call.request().method());

                Response<ApiService.ApiResponse<List<ApiService.TaskItem>>> res = call.execute();

                // LOG RESPONSE STATUS
                Log.d("TaskRepository", "=== RESPONSE STATUS ===");
                Log.d("TaskRepository", "Response Code: " + res.code());
                Log.d("TaskRepository", "Is Successful: " + res.isSuccessful());
                Log.d("TaskRepository", "Response Message: " + res.message());

                if (res.isSuccessful() && res.body() != null) {
                    ApiService.ApiResponse<List<ApiService.TaskItem>> responseBody = res.body();

                    // LOG RESPONSE BODY
                    Log.d("TaskRepository", "=== RESPONSE BODY ===");
                    Log.d("TaskRepository", "HTTP Code: " + responseBody.httpCode);
                    Log.d("TaskRepository", "Success: " + responseBody.success);
                    Log.d("TaskRepository", "Message: " + responseBody.message);

                    if (responseBody.metadata != null) {
                        Log.d("TaskRepository", "=== METADATA ===");
                        Log.d("TaskRepository", "Page: " + responseBody.metadata.page);
                        Log.d("TaskRepository", "Page Size: " + responseBody.metadata.pageSize);
                        Log.d("TaskRepository", "Total: " + responseBody.metadata.total);
                    }

                    if (responseBody.data != null) {
                        List<ApiService.TaskItem> tasks = responseBody.data;

                        // LOG TASKS COUNT
                        Log.d("TaskRepository", "=== TASKS DATA ===");
                        Log.d("TaskRepository", "Total tasks received: " + tasks.size());

                        // LOG EACH TASK
                        for (int i = 0; i < tasks.size(); i++) {
                            ApiService.TaskItem item = tasks.get(i);
                            Log.d("TaskRepository", "--- Task #" + (i + 1) + " ---");
                            Log.d("TaskRepository", "Task ID: " + item.taskId);
                            Log.d("TaskRepository", "User ID: " + item.userId);
                            Log.d("TaskRepository", "Title: " + item.title);
                            Log.d("TaskRepository", "Description: " + item.description);
                            Log.d("TaskRepository", "Priority: " + item.priority);
                            Log.d("TaskRepository", "Task Date: " + item.taskDate);
                            Log.d("TaskRepository", "Is Completed: " + item.isCompleted);
                            Log.d("TaskRepository", "Completed At: " + item.completedAt);
                            Log.d("TaskRepository", "Total Time Spent: " + item.totalTimeSpent);
                            Log.d("TaskRepository", "Estimated Sessions: " + item.estimatedSessions);
                            Log.d("TaskRepository", "Actual Sessions: " + item.actualSessions);
                            Log.d("TaskRepository", "Order Index: " + item.orderIndex);
                            Log.d("TaskRepository", "Created At: " + item.createdAt);
                            Log.d("TaskRepository", "Updated At: " + item.updatedAt);
                        }

                        // MAP TO ENTITIES
                        List<TaskEntity> mapped = new ArrayList<>();
                        for (ApiService.TaskItem item : tasks) {
                            mapped.add(mapToEntity(item));
                        }

                        Log.d("TaskRepository", "=== FETCH ALL TASKS SUCCESS ===");
                        Log.d("TaskRepository", "Mapped " + mapped.size() + " tasks to entities");

                        if (callback != null) callback.onSuccess(mapped);
                    } else {
                        Log.e("TaskRepository", "Response data is NULL");
                        if (callback != null) callback.onError("Response data is null");
                    }
                } else {
                    // LOG ERROR RESPONSE
                    Log.e("TaskRepository", "=== FETCH TASKS FAILED ===");

                    String msg = "Unknown error";
                    if (res.body() != null) {
                        msg = res.body().message != null ? res.body().message : "Unknown error";
                        Log.e("TaskRepository", "Error from body: " + msg);
                        Log.e("TaskRepository", "HTTP Code from body: " + res.body().httpCode);
                    } else {
                        msg = res.message();
                        Log.e("TaskRepository", "Error from response: " + msg);
                    }

                    if (res.errorBody() != null) {
                        try {
                            String errorBodyString = res.errorBody().string();
                            Log.e("TaskRepository", "=== ERROR BODY ===");
                            Log.e("TaskRepository", errorBodyString);

                            // Try parse as JSON
                            try {
                                org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                                Log.e("TaskRepository", "Parsed error JSON: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
                                Log.e("TaskRepository", "Error body is not JSON");
                            }
                        } catch (Exception e) {
                            Log.e("TaskRepository", "Cannot read error body", e);
                        }
                    }

                    if (callback != null) callback.onError("API error: " + msg);
                }
            } catch (IOException e) {
                Log.e("TaskRepository", "=== NETWORK ERROR ===");
                Log.e("TaskRepository", "Network error fetching tasks", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());

                if (callback != null) callback.onError("Network error: " + e.getMessage());
            } catch (Exception e) {
                Log.e("TaskRepository", "=== UNEXPECTED ERROR ===");
                Log.e("TaskRepository", "Unexpected error fetching tasks", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());
                e.printStackTrace();

                if (callback != null) callback.onError("Unexpected error: " + e.getMessage());
            }
        });
    }

    public void createTaskViaApi(TaskEntity task, String accessToken, DataCallback<TaskEntity> callback) {
        executor.execute(() -> {
            try {
                Log.d("TaskRepository", "=== CREATE TASK API CALL ===");

                ApiService api = RetrofitClient.getInstance().getApiService();
                String authHeader = accessToken != null && accessToken.startsWith("Bearer ")
                        ? accessToken
                        : (accessToken != null ? "Bearer " + accessToken : null);

                // LOG TOKEN
                Log.d("TaskRepository", "Access Token: " + accessToken );
                if (accessToken != null) {
                    Log.d("TaskRepository", "Token preview: " + accessToken);
                }
                Log.d("TaskRepository", "Auth Header: " + authHeader);

                long nowMillis = System.currentTimeMillis();
                long taskDateMillis = task.getTaskDate() != null ? task.getTaskDate().getTime() : nowMillis;
                String priority = task.getPriority() != null ? task.getPriority().toUpperCase() : TaskEntity.PRIORITY_MEDIUM;

                ApiService.TaskCreateRequest body = new ApiService.TaskCreateRequest(
                        task.getTitle(),
                        task.getDescription(),
                        priority,
                        taskDateMillis,
                        task.isCompleted() ? 1 : 0,
                        task.isCompleted() ? nowMillis : 0L,
                        task.getTotalTimeSpent(),
                        task.getEstimatedSessions(),
                        task.getActualSessions(),
                        task.getOrderIndex()
                );

                // LOG REQUEST BODY
                Log.d("TaskRepository", "=== REQUEST BODY ===");
                Log.d("TaskRepository", "Title: " + body.title);
                Log.d("TaskRepository", "Description: " + body.description);
                Log.d("TaskRepository", "Priority: " + body.priority);
                Log.d("TaskRepository", "Task Date (millis): " + body.taskDate);
                Log.d("TaskRepository", "Is Completed: " + body.isCompleted);
                Log.d("TaskRepository", "Total Time Spent: " + body.totalTimeSpent);
                Log.d("TaskRepository", "Estimated Sessions: " + body.estimatedSessions);
                Log.d("TaskRepository", "Actual Sessions: " + body.actualSessions);
                Log.d("TaskRepository", "Order Index: " + body.orderIndex);

                Call<ApiService.ApiResponse<ApiService.TaskItem>> call = api.createTask(authHeader, body);

                // LOG URL
                Log.d("TaskRepository", "API URL: " + call.request().url());
                Log.d("TaskRepository", "HTTP Method: " + call.request().method());

                Response<ApiService.ApiResponse<ApiService.TaskItem>> res = call.execute();

                // LOG RESPONSE STATUS
                Log.d("TaskRepository", "=== RESPONSE STATUS ===");
                Log.d("TaskRepository", "Response Code: " + res.code());
                Log.d("TaskRepository", "Is Successful: " + res.isSuccessful());
                Log.d("TaskRepository", "Response Message: " + res.message());

                if (res.isSuccessful() && res.body() != null) {
                    ApiService.ApiResponse<ApiService.TaskItem> responseBody = res.body();

                    // LOG RESPONSE BODY
                    Log.d("TaskRepository", "=== RESPONSE BODY ===");
                    Log.d("TaskRepository", "HTTP Code: " + responseBody.httpCode);
                    Log.d("TaskRepository", "Success: " + responseBody.success);
                    Log.d("TaskRepository", "Message: " + responseBody.message);

                    if (responseBody.data != null) {
                        ApiService.TaskItem item = responseBody.data;

                        // LOG RESPONSE DATA
                        Log.d("TaskRepository", "=== RESPONSE DATA ===");
                        Log.d("TaskRepository", "Task ID: " + item.taskId);
                        Log.d("TaskRepository", "User ID: " + item.userId);
                        Log.d("TaskRepository", "Title: " + item.title);
                        Log.d("TaskRepository", "Description: " + item.description);
                        Log.d("TaskRepository", "Priority: " + item.priority);
                        Log.d("TaskRepository", "Task Date: " + item.taskDate);
                        Log.d("TaskRepository", "Is Completed: " + item.isCompleted);
                        Log.d("TaskRepository", "Completed At: " + item.completedAt);
                        Log.d("TaskRepository", "Total Time Spent: " + item.totalTimeSpent);
                        Log.d("TaskRepository", "Created At: " + item.createdAt);
                        Log.d("TaskRepository", "Updated At: " + item.updatedAt);

                        TaskEntity created = mapToEntity(item);
                        Log.d("TaskRepository", "=== CREATE TASK SUCCESS ===");

                        if (callback != null) callback.onSuccess(created);
                    } else {
                        Log.e("TaskRepository", "Response data is NULL");
                        if (callback != null) callback.onError("Response data is null");
                    }
                } else {
                    // LOG ERROR RESPONSE
                    Log.e("TaskRepository", "=== CREATE TASK FAILED ===");

                    String msg = "Unknown error";
                    if (res.body() != null) {
                        msg = res.body().message != null ? res.body().message : "Unknown error";
                        Log.e("TaskRepository", "Error from body: " + msg);
                        Log.e("TaskRepository", "HTTP Code from body: " + res.body().httpCode);
                    } else {
                        msg = res.message();
                        Log.e("TaskRepository", "Error from response: " + msg);
                    }

                    if (res.errorBody() != null) {
                        try {
                            String errorBodyString = res.errorBody().string();
                            Log.e("TaskRepository", "=== ERROR BODY ===");
                            Log.e("TaskRepository", errorBodyString);

                            // Try parse as JSON
                            try {
                                org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                                Log.e("TaskRepository", "Parsed error JSON: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
                                Log.e("TaskRepository", "Error body is not JSON");
                            }
                        } catch (Exception e) {
                            Log.e("TaskRepository", "Cannot read error body", e);
                        }
                    }

                    if (callback != null) callback.onError("API error: " + msg);
                }
            } catch (IOException e) {
                Log.e("TaskRepository", "=== NETWORK ERROR ===");
                Log.e("TaskRepository", "Network error creating task", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());

                if (callback != null) callback.onError("Network error: " + e.getMessage());
            } catch (Exception e) {
                Log.e("TaskRepository", "=== UNEXPECTED ERROR ===");
                Log.e("TaskRepository", "Unexpected error creating task", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());
                e.printStackTrace();

                if (callback != null) callback.onError("Unexpected error: " + e.getMessage());
            }
        });
    }

    public void updateTaskViaApi(TaskEntity task, String accessToken, DataCallback<TaskEntity> callback) {
        executor.execute(() -> {
            try {
                Log.d("TaskRepository", "=== UPDATE TASK API CALL ===");
                Log.d("TaskRepository", "Task ID: " + task.getTaskId());
                Log.d("TaskRepository", "Is Completed: " + task.isCompleted());
                
                ApiService api = RetrofitClient.getInstance().getApiService();
                String authHeader = accessToken != null && accessToken.startsWith("Bearer ")
                        ? accessToken
                        : (accessToken != null ? "Bearer " + accessToken : null);

                long nowMillis = System.currentTimeMillis();
                long taskDateMillis = task.getTaskDate() != null ? task.getTaskDate().getTime() : nowMillis;
                String priority = task.getPriority() != null ? task.getPriority().toUpperCase() : TaskEntity.PRIORITY_MEDIUM;
                
                // Lấy completed_at từ task, nếu null thì dùng 0
                long completedAtMillis = 0L;
                if (task.isCompleted()) {
                    completedAtMillis = task.getCompletedAt() != null ? task.getCompletedAt().getTime() : nowMillis;
                }

                ApiService.TaskUpdateRequest body = new ApiService.TaskUpdateRequest(
                        task.getTitle(),
                        task.getDescription(),
                        priority,
                        taskDateMillis,
                        task.isCompleted() ? 1 : 0,  // is_completed: 1 = true, 0 = false
                        completedAtMillis,
                        task.getTotalTimeSpent(),
                        task.getEstimatedSessions(),
                        task.getActualSessions(),
                        task.getOrderIndex()
                );
                
                // LOG REQUEST
                Log.d("TaskRepository", "=== UPDATE REQUEST BODY ===");
                Log.d("TaskRepository", "Is Completed: " + body.isCompleted);
                Log.d("TaskRepository", "Completed At: " + body.completedAt);

                Call<ApiService.ApiResponse<ApiService.TaskItem>> call = api.updateTask(authHeader, task.getTaskId(), body);
                Log.d("TaskRepository", "API URL: " + call.request().url());
                
                Response<ApiService.ApiResponse<ApiService.TaskItem>> res = call.execute();
                
                // LOG RESPONSE
                Log.d("TaskRepository", "=== UPDATE RESPONSE ===");
                Log.d("TaskRepository", "Response Code: " + res.code());
                Log.d("TaskRepository", "Is Successful: " + res.isSuccessful());

                if (res.isSuccessful() && res.body() != null && res.body().success && res.body().data != null) {
                    Log.d("TaskRepository", "Task updated successfully");
                    TaskEntity updated = mapToEntity(res.body().data);
                    if (callback != null) callback.onSuccess(updated);
                } else {
                    String msg = res.body() != null ? (res.body().message != null ? res.body().message : "Unknown error") : (res.message());
                    Log.e("TaskRepository", "Update failed: " + msg);
                    if (callback != null) callback.onError("API error: " + msg);
                }
            } catch (IOException e) {
                Log.e("TaskRepository", "Network error updating task", e);
                if (callback != null) callback.onError("Network error: " + e.getMessage());
            } catch (Exception e) {
                Log.e("TaskRepository", "Unexpected error updating task", e);
                if (callback != null) callback.onError("Unexpected error: " + e.getMessage());
            }
        });
    }

    public void deleteTaskViaApi(int taskId, String accessToken, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Log.d("TaskRepository", "=== DELETE TASK API CALL ===");
                Log.d("TaskRepository", "Task ID to delete: " + taskId);

                ApiService api = RetrofitClient.getInstance().getApiService();
                String authHeader = accessToken != null && accessToken.startsWith("Bearer ")
                        ? accessToken
                        : (accessToken != null ? "Bearer " + accessToken : null);

                // LOG TOKEN
                Log.d("TaskRepository", "Access Token: " + (accessToken != null ? "EXISTS" : "NULL"));
                if (accessToken != null) {
                    Log.d("TaskRepository", "Token preview: " + accessToken.substring(0, Math.min(50, accessToken.length())) + "...");
                }
                Log.d("TaskRepository", "Auth Header: " + authHeader);

                Call<ApiService.ApiResponse<Object>> call = api.deleteTask(authHeader, taskId);

                // LOG URL
                Log.d("TaskRepository", "API URL: " + call.request().url());
                Log.d("TaskRepository", "HTTP Method: " + call.request().method());

                Response<ApiService.ApiResponse<Object>> res = call.execute();

                // LOG RESPONSE STATUS
                Log.d("TaskRepository", "=== RESPONSE STATUS ===");
                Log.d("TaskRepository", "Response Code: " + res.code());
                Log.d("TaskRepository", "Is Successful: " + res.isSuccessful());
                Log.d("TaskRepository", "Response Message: " + res.message());

                // XỬ LÝ 204 NO CONTENT HOẶC 200 OK
                if (res.isSuccessful()) {
                    // HTTP 204 No Content hoặc 200 OK đều là thành công
                    if (res.code() == 204) {
                        Log.d("TaskRepository", "=== DELETE TASK SUCCESS (204 No Content) ===");
                        Log.d("TaskRepository", "Task ID " + taskId + " deleted successfully");
                        if (callback != null) callback.onSuccess(null);
                    } else if (res.body() != null) {
                        // Có body (200 OK)
                        ApiService.ApiResponse<Object> responseBody = res.body();

                        Log.d("TaskRepository", "=== RESPONSE BODY ===");
                        Log.d("TaskRepository", "HTTP Code: " + responseBody.httpCode);
                        Log.d("TaskRepository", "Success: " + responseBody.success);
                        Log.d("TaskRepository", "Message: " + responseBody.message);

                        if (responseBody.data != null) {
                            Log.d("TaskRepository", "Response Data: " + responseBody.data.toString());
                        }

                        Log.d("TaskRepository", "=== DELETE TASK SUCCESS ===");
                        Log.d("TaskRepository", "Task ID " + taskId + " deleted successfully");

                        if (callback != null) callback.onSuccess(null);
                    } else {
                        // Successful nhưng không có body và không phải 204
                        Log.d("TaskRepository", "=== DELETE TASK SUCCESS (No Body) ===");
                        Log.d("TaskRepository", "Task ID " + taskId + " deleted successfully");
                        if (callback != null) callback.onSuccess(null);
                    }
                } else {
                    // LOG ERROR RESPONSE
                    Log.e("TaskRepository", "=== DELETE TASK FAILED ===");

                    String msg = "Unknown error";
                    if (res.body() != null) {
                        msg = res.body().message != null ? res.body().message : "Unknown error";
                        Log.e("TaskRepository", "Error from body: " + msg);
                        Log.e("TaskRepository", "HTTP Code from body: " + res.body().httpCode);
                    } else {
                        msg = res.message();
                        Log.e("TaskRepository", "Error from response: " + msg);
                    }

                    if (res.errorBody() != null) {
                        try {
                            String errorBodyString = res.errorBody().string();
                            Log.e("TaskRepository", "=== ERROR BODY ===");
                            Log.e("TaskRepository", errorBodyString);

                            // Try parse as JSON
                            try {
                                org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                                Log.e("TaskRepository", "Parsed error JSON: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
                                Log.e("TaskRepository", "Error body is not JSON");
                            }
                        } catch (Exception e) {
                            Log.e("TaskRepository", "Cannot read error body", e);
                        }
                    }

                    if (callback != null) callback.onError("API error: " + msg);
                }
            } catch (IOException e) {
                Log.e("TaskRepository", "=== NETWORK ERROR ===");
                Log.e("TaskRepository", "Network error deleting task", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());

                if (callback != null) callback.onError("Network error: " + e.getMessage());
            } catch (Exception e) {
                Log.e("TaskRepository", "=== UNEXPECTED ERROR ===");
                Log.e("TaskRepository", "Unexpected error deleting task", e);
                Log.e("TaskRepository", "Error message: " + e.getMessage());
                Log.e("TaskRepository", "Error class: " + e.getClass().getName());
                e.printStackTrace();

                if (callback != null) callback.onError("Unexpected error: " + e.getMessage());
            }
        });
    }

    private TaskEntity mapToEntity(ApiService.TaskItem item) {
        TaskEntity t = new TaskEntity();
        t.setTaskId(item.taskId);
        t.setUserId(item.userId);
        t.setTitle(item.title);
        t.setDescription(item.description);
        t.setPriority(item.priority != null ? item.priority : TaskEntity.PRIORITY_MEDIUM);
        // Convert epoch timestamp (seconds or milliseconds) to Date; treat <=0 as null
        t.setTaskDate(epochToDate(item.taskDate));
        t.setCompleted(item.isCompleted != 0);
        t.setCompletedAt(epochToDate(item.completedAt));
        t.setTotalTimeSpent(item.totalTimeSpent);
        t.setEstimatedSessions(item.estimatedSessions);
        t.setActualSessions(item.actualSessions);
        t.setOrderIndex(item.orderIndex);
        t.setCreatedAt(epochToDate(item.createdAt));
        t.setUpdatedAt(epochToDate(item.updatedAt));
        return t;
    }

    private Date epochToDate(double value) {
        if (value <= 0) return null;
        // Detect seconds vs milliseconds
        long millis;
        if (value >= 1e11) { // likely milliseconds
            millis = (long) Math.floor(value);
        } else { // seconds
            millis = (long) Math.floor(value * 1000d);
        }
        return new Date(millis);
    }
}

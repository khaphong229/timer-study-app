package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.utils.UserManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Statistics Cache API operations
 * Handles all statistics cache-related API calls
 */
public class StatisticsCacheApiRepository {
    
    private static final String TAG = "StatisticsCacheApiRepo";
    
    private final ApiService apiService;
    private final ExecutorService executorService;
    private final Gson gson;
    private Context context;
    
    // LiveData for reactive updates
    private final MutableLiveData<List<ApiService.StatisticsCacheResponse>> allCacheLiveData;
    private final MutableLiveData<ApiService.StatisticsCacheResponse> cacheByIdLiveData;
    private final MutableLiveData<ApiService.DailyStatisticsResponse> dailyStatisticsLiveData;
    private final MutableLiveData<ApiService.MonthlyStatisticsResponse> monthlyStatisticsLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;
    
    // Singleton instance
    private static volatile StatisticsCacheApiRepository INSTANCE;
    
    private StatisticsCacheApiRepository() {
        apiService = RetrofitClient.getInstance().getApiService();
        executorService = Executors.newFixedThreadPool(2);
        gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Initialize LiveData
        allCacheLiveData = new MutableLiveData<>();
        cacheByIdLiveData = new MutableLiveData<>();
        dailyStatisticsLiveData = new MutableLiveData<>();
        monthlyStatisticsLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }
    
    // ==================== LOGGING HELPER METHODS ====================
    
    /**
     * Log request details before sending
     */
    private <T> void logRequest(String methodName, Call<T> call, Object requestBody) {
        Log.d(TAG, "=== " + methodName.toUpperCase() + " REQUEST ===");
        Log.d(TAG, "URL: " + call.request().url());
        Log.d(TAG, "HTTP Method: " + call.request().method());
        
        // Log headers
        Log.d(TAG, "=== REQUEST HEADERS ===");
        okhttp3.Headers headers = call.request().headers();
        for (int i = 0; i < headers.size(); i++) {
            String headerName = headers.name(i);
            String headerValue = headers.value(i);
            // Mask authorization token for security
            if (headerName.equalsIgnoreCase("Authorization")) {
                if (headerValue != null && headerValue.length() > 20) {
                    headerValue = headerValue.substring(0, 20) + "...";
                }
            }
            Log.d(TAG, headerName + ": " + headerValue);
        }
        
        // Log request body if available
        if (requestBody != null) {
            try {
                String jsonBody = gson.toJson(requestBody);
                Log.d(TAG, "=== REQUEST BODY (JSON) ===");
                Log.d(TAG, jsonBody);
            } catch (Exception e) {
                Log.d(TAG, "=== REQUEST BODY ===");
                Log.d(TAG, requestBody.toString());
            }
        }
        
        Log.d(TAG, "========================================");
    }
    
    /**
     * Log response details
     */
    private <T> void logResponse(String methodName, Call<?> call, Response<ApiService.ApiResponse<T>> response) {
        Log.d(TAG, "=== " + methodName.toUpperCase() + " RESPONSE ===");
        Log.d(TAG, "URL: " + call.request().url());
        Log.d(TAG, "HTTP Method: " + call.request().method());
        Log.d(TAG, "HTTP Code: " + response.code());
        Log.d(TAG, "Is Successful: " + response.isSuccessful());
        Log.d(TAG, "Response Message: " + response.message());
        
        if (response.body() != null) {
            ApiService.ApiResponse<T> responseBody = response.body();
            Log.d(TAG, "=== RESPONSE BODY ===");
            Log.d(TAG, "HTTP Code (from body): " + responseBody.httpCode);
            Log.d(TAG, "Success: " + responseBody.success);
            Log.d(TAG, "Message: " + responseBody.message);
            
            if (responseBody.metadata != null) {
                Log.d(TAG, "=== METADATA ===");
                Log.d(TAG, "Page: " + responseBody.metadata.page);
                Log.d(TAG, "Page Size: " + responseBody.metadata.pageSize);
                Log.d(TAG, "Total: " + responseBody.metadata.total);
            }
            
            if (responseBody.data != null) {
                try {
                    String jsonData = gson.toJson(responseBody.data);
                    Log.d(TAG, "=== RESPONSE DATA (JSON) ===");
                    Log.d(TAG, jsonData);
                } catch (Exception e) {
                    Log.d(TAG, "=== RESPONSE DATA ===");
                    Log.d(TAG, "Data: " + responseBody.data.toString());
                }
            } else {
                Log.d(TAG, "Response data is NULL");
            }
        } else {
            Log.e(TAG, "Response body is NULL");
            
            // Log error body if available
            if (response.errorBody() != null) {
                try {
                    String errorBodyString = response.errorBody().string();
                    Log.e(TAG, "=== ERROR BODY ===");
                    Log.e(TAG, errorBodyString);
                    
                    // Try parse as JSON
                    try {
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                        Log.e(TAG, "=== PARSED ERROR JSON ===");
                        Log.e(TAG, errorJson.toString(2));
                    } catch (Exception jsonEx) {
                        Log.e(TAG, "Error body is not JSON");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read error body", e);
                }
            }
        }
        Log.d(TAG, "========================================");
    }
    
    /**
     * Set context for UserManager access
     */
    public void setContext(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }
    
    /**
     * Get singleton instance
     */
    public static StatisticsCacheApiRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (StatisticsCacheApiRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StatisticsCacheApiRepository();
                }
            }
        }
        return INSTANCE;
    }
    
    // ==================== LIVE DATA GETTERS ====================
    
    public LiveData<List<ApiService.StatisticsCacheResponse>> getAllCacheLiveData() {
        return allCacheLiveData;
    }
    
    public LiveData<ApiService.StatisticsCacheResponse> getCacheByIdLiveData() {
        return cacheByIdLiveData;
    }
    
    public LiveData<ApiService.DailyStatisticsResponse> getDailyStatisticsLiveData() {
        return dailyStatisticsLiveData;
    }
    
    public LiveData<ApiService.MonthlyStatisticsResponse> getMonthlyStatisticsLiveData() {
        return monthlyStatisticsLiveData;
    }
    
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    // ==================== AUTHENTICATION HELPER ====================
    
    /**
     * Get access token from UserManager
     */
    private void getAuthToken(AuthTokenCallback callback) {
        Log.d(TAG, "=== GETTING AUTH TOKEN ===");
        
        if (context == null) {
            Log.e(TAG, "Context is null - cannot get access token");
            callback.onError("Context not initialized");
            return;
        }
        
        try {
            UserManager userManager = UserManager.getInstance(context);
            com.example.timerstudy.model.User user = userManager.getCurrentUser();
            
            Log.d(TAG, "User from UserManager: " + (user != null ? "EXISTS" : "NULL"));
            
            if (user != null) {
                Log.d(TAG, "User ID: " + user.getUserId());
                Log.d(TAG, "User Name: " + user.getName());
                Log.d(TAG, "Access Token: " + (user.getAccessToken() != null ? "EXISTS (length: " + user.getAccessToken().length() + ")" : "NULL"));
                
                if (user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                    String accessToken = user.getAccessToken();
                    // Check if token already has "Bearer " prefix
                    String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
                    Log.d(TAG, "Token formatted: " + (token.startsWith("Bearer ") ? "YES" : "NO"));
                    Log.d(TAG, "Token length: " + token.length());
                    Log.d(TAG, "Token preview: " + token.substring(0, Math.min(30, token.length())) + "...");
                    callback.onTokenReceived(token);
                } else {
                    Log.e(TAG, "No access token found in UserManager - user not authenticated or token missing");
                    callback.onError("User not authenticated or access token missing");
                }
            } else {
                Log.e(TAG, "User is null - user not logged in");
                callback.onError("User not logged in");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting access token from UserManager", e);
            e.printStackTrace();
            callback.onError("Failed to get access token: " + e.getMessage());
        }
        
        Log.d(TAG, "========================================");
    }
    
    interface AuthTokenCallback {
        void onTokenReceived(String token);
        void onError(String error);
    }
    
    // ==================== API METHODS ====================
    
    /**
     * 1. Get All Statistics Cache
     */
    public void getAllStatisticsCache(StatisticsCacheCallback<List<ApiService.StatisticsCacheResponse>> callback) {
        Log.d(TAG, "=== getAllStatisticsCache CALLED ===");
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                Log.d(TAG, "Token received, creating API call...");
                Call<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> call = apiService.getAllStatisticsCache(token);
                
                // Log request before sending
                logRequest("getAllStatisticsCache", call, null);
                
                call.enqueue(new Callback<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> call,
                                             Response<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("getAllStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                List<ApiService.StatisticsCacheResponse> data = response.body().data;
                                Log.d(TAG, "getAllStatisticsCache - Success! Count: " + (data != null ? data.size() : 0));
                                allCacheLiveData.postValue(data);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get statistics cache";
                                errorLiveData.postValue(errorMsg);
                                Log.e(TAG, "getAllStatisticsCache - Error: " + errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "=== getAllStatisticsCache FAILED ===");
                            Log.e(TAG, "Error: " + t.getMessage());
                            Log.e(TAG, "Error class: " + t.getClass().getName());
                            Log.e(TAG, "Request URL: " + call.request().url());
                            t.printStackTrace();
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 2. Get Statistics Cache with pagination/filter
     */
    public void getStatisticsCache(Integer page, Integer pageSize, String sortBy, String order,
                                    StatisticsCacheCallback<List<ApiService.StatisticsCacheResponse>> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStatisticsCache(token, page, pageSize, sortBy, order)
                    .enqueue(new Callback<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> call,
                                             Response<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("getStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                List<ApiService.StatisticsCacheResponse> data = response.body().data;
                                allCacheLiveData.postValue(data);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get statistics cache";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<List<ApiService.StatisticsCacheResponse>>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "getStatisticsCache failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 3. Create Statistics Cache
     */
    public void createStatisticsCache(double cacheDate, String cacheType, int totalSessions,
                                       int totalFocusTime, int totalBreakTime, int completedTasks,
                                       int goalAchieved, int currentStreak, int bestStreak,
                                       StatisticsCacheCallback<ApiService.StatisticsCacheResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                ApiService.StatisticsCacheRequest request = new ApiService.StatisticsCacheRequest(
                    cacheDate, cacheType, totalSessions, totalFocusTime, totalBreakTime,
                    completedTasks, goalAchieved, currentStreak, bestStreak
                );
                apiService.createStatisticsCache(token, request)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("createStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StatisticsCacheResponse data = response.body().data;
                                cacheByIdLiveData.postValue(data);
                                // Refresh all cache list
                                getAllStatisticsCache(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to create statistics cache";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "createStatisticsCache failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 4. Get Statistics Cache by ID
     */
    public void getStatisticsCacheById(int cacheId, StatisticsCacheCallback<ApiService.StatisticsCacheResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStatisticsCacheById(token, cacheId)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("getStatisticsCacheById", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StatisticsCacheResponse data = response.body().data;
                                cacheByIdLiveData.postValue(data);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get statistics cache by ID";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "getStatisticsCacheById failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 5. Update Statistics Cache (PUT)
     */
    public void updateStatisticsCache(int cacheId, double cacheDate, String cacheType, int totalSessions,
                                      int totalFocusTime, int totalBreakTime, int completedTasks,
                                      int goalAchieved, int currentStreak, int bestStreak,
                                      StatisticsCacheCallback<ApiService.StatisticsCacheResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                ApiService.StatisticsCacheRequest request = new ApiService.StatisticsCacheRequest(
                    cacheDate, cacheType, totalSessions, totalFocusTime, totalBreakTime,
                    completedTasks, goalAchieved, currentStreak, bestStreak
                );
                apiService.updateStatisticsCache(token, cacheId, request)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("updateStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StatisticsCacheResponse data = response.body().data;
                                cacheByIdLiveData.postValue(data);
                                // Refresh all cache list
                                getAllStatisticsCache(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to update statistics cache";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "updateStatisticsCache failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 6. Patch Statistics Cache (PATCH - partial update)
     */
    public void patchStatisticsCache(int cacheId, ApiService.StatisticsCacheRequest request,
                                     StatisticsCacheCallback<ApiService.StatisticsCacheResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.patchStatisticsCache(token, cacheId, request)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("patchStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StatisticsCacheResponse data = response.body().data;
                                cacheByIdLiveData.postValue(data);
                                // Refresh all cache list
                                getAllStatisticsCache(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to patch statistics cache";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StatisticsCacheResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "patchStatisticsCache failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * 7. Delete Statistics Cache
     */
    public void deleteStatisticsCache(int cacheId, StatisticsCacheCallback<Void> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.deleteStatisticsCache(token, cacheId)
                    .enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                             Response<ApiService.ApiResponse<Void>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("deleteStatisticsCache", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                // Refresh all cache list
                                getAllStatisticsCache(null);
                                if (callback != null) callback.onSuccess(null);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to delete statistics cache";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "deleteStatisticsCache failed", t);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    // ==================== CALCULATED STATISTICS METHODS ====================
    
    /**
     * Get Daily Statistics (calculated from sessions, tasks, goals)
     * 
     * @param date Unix timestamp (double) - date to get statistics for
     * @param callback Callback for success/error
     */
    public void getDailyStatistics(double date, StatisticsCacheCallback<ApiService.DailyStatisticsResponse> callback) {
        Log.d(TAG, "=== getDailyStatistics CALLED ===");
        Log.d(TAG, "Date parameter: " + date);
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                Log.d(TAG, "Token received, creating API call...");
                Call<ApiService.ApiResponse<ApiService.DailyStatisticsResponse>> call = apiService.getDailyStatistics(token, date);
                
                // Log request before sending
                logRequest("getDailyStatistics", call, null);
                
                call.enqueue(new Callback<ApiService.ApiResponse<ApiService.DailyStatisticsResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.DailyStatisticsResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.DailyStatisticsResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("getDailyStatistics", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.DailyStatisticsResponse data = response.body().data;
                                if (data != null) {
                                    Log.d(TAG, "getDailyStatistics - Success! Sessions: " + data.totalSessions + 
                                        ", Focus Time: " + data.totalFocusTime + " min");
                                    dailyStatisticsLiveData.postValue(data);
                                    if (callback != null) callback.onSuccess(data);
                                } else {
                                    String errorMsg = "No data returned for daily statistics";
                                    errorLiveData.postValue(errorMsg);
                                    if (callback != null) callback.onError(errorMsg);
                                }
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get daily statistics";
                                errorLiveData.postValue(errorMsg);
                                Log.e(TAG, "getDailyStatistics - Error: " + errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.DailyStatisticsResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "=== getDailyStatistics FAILED ===");
                            Log.e(TAG, "Error: " + t.getMessage());
                            Log.e(TAG, "Error class: " + t.getClass().getName());
                            Log.e(TAG, "Request URL: " + call.request().url());
                            t.printStackTrace();
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Get Monthly Statistics (calculated from sessions, tasks, goals)
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @param callback Callback for success/error
     */
    public void getMonthlyStatistics(int year, int month, StatisticsCacheCallback<ApiService.MonthlyStatisticsResponse> callback) {
        Log.d(TAG, "=== getMonthlyStatistics CALLED ===");
        Log.d(TAG, "Year: " + year + ", Month: " + month);
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                Log.d(TAG, "Token received, creating API call...");
                Call<ApiService.ApiResponse<ApiService.MonthlyStatisticsResponse>> call = apiService.getMonthlyStatistics(token, year, month);
                
                // Log request before sending
                logRequest("getMonthlyStatistics", call, null);
                
                call.enqueue(new Callback<ApiService.ApiResponse<ApiService.MonthlyStatisticsResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.MonthlyStatisticsResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.MonthlyStatisticsResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            
                            // Log full response
                            logResponse("getMonthlyStatistics", call, response);
                            
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.MonthlyStatisticsResponse data = response.body().data;
                                if (data != null) {
                                    Log.d(TAG, "getMonthlyStatistics - Success! Year: " + data.year + 
                                        ", Month: " + data.month + ", Sessions: " + data.totalSessions + 
                                        ", Focus Time: " + data.totalFocusTime + " min");
                                    monthlyStatisticsLiveData.postValue(data);
                                    if (callback != null) callback.onSuccess(data);
                                } else {
                                    String errorMsg = "No data returned for monthly statistics";
                                    errorLiveData.postValue(errorMsg);
                                    if (callback != null) callback.onError(errorMsg);
                                }
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get monthly statistics";
                                errorLiveData.postValue(errorMsg);
                                Log.e(TAG, "getMonthlyStatistics - Error: " + errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.MonthlyStatisticsResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "=== getMonthlyStatistics FAILED ===");
                            Log.e(TAG, "Error: " + t.getMessage());
                            Log.e(TAG, "Error class: " + t.getClass().getName());
                            Log.e(TAG, "Request URL: " + call.request().url());
                            t.printStackTrace();
                            if (callback != null) callback.onError(errorMsg);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    // ==================== CALLBACK INTERFACE ====================
    
    public interface StatisticsCacheCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}


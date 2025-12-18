package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.utils.UserManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Streak API operations
 * Handles all streak-related API calls and caching
 */
public class StreakRepository {
    
    private static final String TAG = "StreakRepository";
    
    private final ApiService apiService;
    private final ExecutorService executorService;
    private Context context;
    
    // LiveData for reactive updates
    private final MutableLiveData<ApiService.StreakSummaryResponse> streakSummaryLiveData;
    private final MutableLiveData<ApiService.StreakCurrentResponse> streakCurrentLiveData;
    private final MutableLiveData<List<ApiService.StreakRecordResponse>> streakRecordsLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;
    
    // Singleton instance
    private static volatile StreakRepository INSTANCE;
    
    private StreakRepository() {
        apiService = RetrofitClient.getInstance().getApiService();
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialize LiveData
        streakSummaryLiveData = new MutableLiveData<>();
        streakCurrentLiveData = new MutableLiveData<>();
        streakRecordsLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
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
    public static StreakRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (StreakRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StreakRepository();
                }
            }
        }
        return INSTANCE;
    }
    
    // ==================== LIVE DATA GETTERS ====================
    
    public LiveData<ApiService.StreakSummaryResponse> getStreakSummaryLiveData() {
        return streakSummaryLiveData;
    }
    
    public LiveData<ApiService.StreakCurrentResponse> getStreakCurrentLiveData() {
        return streakCurrentLiveData;
    }
    
    public LiveData<List<ApiService.StreakRecordResponse>> getStreakRecordsLiveData() {
        return streakRecordsLiveData;
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
        if (context == null) {
            Log.e(TAG, "Context is null - cannot get access token");
            callback.onError("Context not initialized");
            return;
        }
        
        try {
            UserManager userManager = UserManager.getInstance(context);
            com.example.timerstudy.model.User user = userManager.getCurrentUser();
            
            if (user != null && user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                String accessToken = user.getAccessToken();
                // Check if token already has "Bearer " prefix
                String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
                Log.d(TAG, "Access token retrieved from UserManager (length: " + token.length() + ")");
                callback.onTokenReceived(token);
            } else {
                Log.e(TAG, "No access token found in UserManager - user not authenticated or token missing");
                callback.onError("User not authenticated or access token missing");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting access token from UserManager", e);
            callback.onError("Failed to get access token: " + e.getMessage());
        }
    }
    
    interface AuthTokenCallback {
        void onTokenReceived(String token);
        void onError(String error);
    }
    
    // ==================== API METHODS ====================
    
    /**
     * 1. Get Streak Summary (Recommended)
     */
    public void getStreakSummary(StreakCallback<ApiService.StreakSummaryResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStreakSummary(token).enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakSummaryResponse>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakSummaryResponse>> call,
                                         Response<ApiService.ApiResponse<ApiService.StreakSummaryResponse>> response) {
                        isLoadingLiveData.postValue(false);
                        
                        // Log response details for debugging
                        Log.d(TAG, "getStreakSummary - HTTP Code: " + response.code());
                        Log.d(TAG, "getStreakSummary - URL: " + call.request().url());
                        Log.d(TAG, "getStreakSummary - Is Successful: " + response.isSuccessful());
                        
                        if (response.body() != null) {
                            Log.d(TAG, "getStreakSummary - Response success: " + response.body().success);
                            Log.d(TAG, "getStreakSummary - Response message: " + response.body().message);
                            Log.d(TAG, "getStreakSummary - Response http_code: " + response.body().httpCode);
                        } else {
                            Log.e(TAG, "getStreakSummary - Response body is NULL");
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "getStreakSummary - Error body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "getStreakSummary - Cannot read error body", e);
                                }
                            }
                        }
                        
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ApiService.StreakSummaryResponse data = response.body().data;
                            Log.d(TAG, "getStreakSummary - Success! Current streak: " + data.currentStreak);
                            streakSummaryLiveData.postValue(data);
                            if (callback != null) callback.onSuccess(data);
                        } else {
                            String errorMsg;
                            if (response.body() != null) {
                                errorMsg = response.body().message != null ? response.body().message : "Failed to get streak summary (HTTP " + response.code() + ")";
                            } else {
                                errorMsg = "Failed to get streak summary (HTTP " + response.code() + ", no response body)";
                            }
                            Log.e(TAG, "getStreakSummary - Error: " + errorMsg);
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakSummaryResponse>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "getStreakSummary failed", t);
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
     * 2. Get Current Streak (from cache)
     */
    public void getStreakCurrent(StreakCallback<ApiService.StreakCurrentResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStreakCurrent(token).enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakCurrentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakCurrentResponse>> call,
                                         Response<ApiService.ApiResponse<ApiService.StreakCurrentResponse>> response) {
                        isLoadingLiveData.postValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ApiService.StreakCurrentResponse data = response.body().data;
                            streakCurrentLiveData.postValue(data);
                            if (callback != null) callback.onSuccess(data);
                        } else {
                            String errorMsg = response.body() != null ? response.body().message : "Failed to get current streak";
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakCurrentResponse>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "getStreakCurrent failed", t);
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
     * 3. Get All Streak Records
     */
    public void getAllStreakRecords(StreakCallback<List<ApiService.StreakRecordResponse>> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getAllStreakRecords(token).enqueue(new Callback<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> call,
                                         Response<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> response) {
                        isLoadingLiveData.postValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            List<ApiService.StreakRecordResponse> data = response.body().data;
                            streakRecordsLiveData.postValue(data);
                            if (callback != null) callback.onSuccess(data);
                        } else {
                            String errorMsg = response.body() != null ? response.body().message : "Failed to get streak records";
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "getAllStreakRecords failed", t);
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
     * 4. Get Streak Records with pagination/filter
     */
    public void getStreakRecords(Integer page, Integer pageSize, String sortBy, String sortOrder,
                                 StreakCallback<List<ApiService.StreakRecordResponse>> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStreakRecords(token, page, pageSize, sortBy, sortOrder)
                    .enqueue(new Callback<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> call,
                                             Response<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> response) {
                            isLoadingLiveData.postValue(false);
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                List<ApiService.StreakRecordResponse> data = response.body().data;
                                streakRecordsLiveData.postValue(data);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to get streak records";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<List<ApiService.StreakRecordResponse>>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "getStreakRecords failed", t);
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
     * 5. Get Streak by Date
     */
    public void getStreakByDate(double date, StreakCallback<ApiService.StreakRecordResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStreakByDate(token, date).enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakRecordResponse>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call,
                                         Response<ApiService.ApiResponse<ApiService.StreakRecordResponse>> response) {
                        isLoadingLiveData.postValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ApiService.StreakRecordResponse data = response.body().data;
                            // Check if data is null (no record found for this date)
                            if (data != null) {
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                // No record found for this date
                                String errorMsg = "No streak record found for this date";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        } else {
                            // API returned error or no record
                            String errorMsg = response.body() != null ? response.body().message : "No streak record found";
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "getStreakByDate failed", t);
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
     * 6. Upsert Streak Record (Create or Update)
     */
    public void upsertStreakRecord(double streakDate, int hasActivity, int sessionCount, int focusTime,
                                   StreakCallback<ApiService.StreakRecordResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                ApiService.StreakRecordRequest request = new ApiService.StreakRecordRequest(
                    streakDate, hasActivity, sessionCount, focusTime
                );
                apiService.upsertStreakRecord(token, request)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakRecordResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StreakRecordResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StreakRecordResponse data = response.body().data;
                                // Refresh summary after update
                                getStreakSummary(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to upsert streak record";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "upsertStreakRecord failed", t);
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
     * 7. Get Streak by ID
     */
    public void getStreakById(int streakId, StreakCallback<ApiService.StreakRecordResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.getStreakById(token, streakId).enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakRecordResponse>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call,
                                         Response<ApiService.ApiResponse<ApiService.StreakRecordResponse>> response) {
                        isLoadingLiveData.postValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ApiService.StreakRecordResponse data = response.body().data;
                            if (callback != null) callback.onSuccess(data);
                        } else {
                            String errorMsg = response.body() != null ? response.body().message : "Failed to get streak by ID";
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "getStreakById failed", t);
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
     * 8. Update Streak Record
     */
    public void updateStreakRecord(int streakId, double streakDate, int hasActivity, int sessionCount, int focusTime,
                                   StreakCallback<ApiService.StreakRecordResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                ApiService.StreakRecordRequest request = new ApiService.StreakRecordRequest(
                    streakDate, hasActivity, sessionCount, focusTime
                );
                apiService.updateStreakRecord(token, streakId, request)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakRecordResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StreakRecordResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StreakRecordResponse data = response.body().data;
                                // Refresh summary after update
                                getStreakSummary(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to update streak record";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakRecordResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "updateStreakRecord failed", t);
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
     * 9. Delete Streak Record
     */
    public void deleteStreakRecord(int streakId, StreakCallback<Void> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.deleteStreakRecord(token, streakId).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                         Response<ApiService.ApiResponse<Void>> response) {
                        isLoadingLiveData.postValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            // Refresh summary after delete
                            getStreakSummary(null);
                            if (callback != null) callback.onSuccess(null);
                        } else {
                            String errorMsg = response.body() != null ? response.body().message : "Failed to delete streak record";
                            errorLiveData.postValue(errorMsg);
                            if (callback != null) callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                        isLoadingLiveData.postValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        errorLiveData.postValue(errorMsg);
                        Log.e(TAG, "deleteStreakRecord failed", t);
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
     * 10. Cleanup Duplicate Streak Records
     */
    public void cleanupDuplicateStreakRecords(StreakCallback<ApiService.StreakCleanupResponse> callback) {
        isLoadingLiveData.postValue(true);
        getAuthToken(new AuthTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                apiService.cleanupDuplicateStreakRecords(token)
                    .enqueue(new Callback<ApiService.ApiResponse<ApiService.StreakCleanupResponse>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<ApiService.StreakCleanupResponse>> call,
                                             Response<ApiService.ApiResponse<ApiService.StreakCleanupResponse>> response) {
                            isLoadingLiveData.postValue(false);
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                ApiService.StreakCleanupResponse data = response.body().data;
                                Log.d(TAG, "cleanupDuplicateStreakRecords - Success! Merged: " + 
                                    data.mergedGroups + ", Deleted: " + data.deletedRecords);
                                // Refresh summary after cleanup
                                getStreakSummary(null);
                                if (callback != null) callback.onSuccess(data);
                            } else {
                                String errorMsg = response.body() != null ? response.body().message : "Failed to cleanup duplicate streak records";
                                errorLiveData.postValue(errorMsg);
                                if (callback != null) callback.onError(errorMsg);
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<ApiService.StreakCleanupResponse>> call, Throwable t) {
                            isLoadingLiveData.postValue(false);
                            String errorMsg = "Network error: " + t.getMessage();
                            errorLiveData.postValue(errorMsg);
                            Log.e(TAG, "cleanupDuplicateStreakRecords failed", t);
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
    
    public interface StreakCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}


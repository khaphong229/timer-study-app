package com.example.timerstudy.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.UserDao;
import com.example.timerstudy.data.local.database.entities.UserEntity;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.model.User;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for User data operations
 *
 * This repository handles all user-related database operations and provides
 * a clean interface for the ViewModel layer. It manages background threads
 * and error handling for database operations.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String PREF_NAME = "user_profile_prefs";
    private static final String KEY_CURRENT_USER = "current_user";

    // Database and DAO
    private final AppDatabase database;
    private final UserDao userDao;
    private final Context context;
    private final ApiService apiService;

    // SharedPreferences for User model
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    // Thread executor for background operations
    private final ExecutorService executorService;

    // LiveData for reactive updates
    private final MutableLiveData<List<UserEntity>> allUsersLiveData;
    private final MutableLiveData<UserEntity> currentUserLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    // Singleton instance
    private static volatile UserRepository INSTANCE;

    /**
     * Constructor
     *
     * @param context Application context
     */
    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getDatabase(context);
        userDao = database.userDao();
        apiService = RetrofitClient.getInstance().getApiService();
        executorService = Executors.newFixedThreadPool(4);

        // Initialize SharedPreferences and Gson
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        // Initialize LiveData
        allUsersLiveData = new MutableLiveData<>();
        currentUserLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        // Load initial data
        loadAllUsers();
    }

    /**
     * Get singleton instance
     *
     * @param context Application context
     * @return UserRepository instance
     */
    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ==================== LIVE DATA GETTERS ====================

    /**
     * Get all users LiveData
     *
     * @return LiveData containing list of all users
     */
    public LiveData<List<UserEntity>> getAllUsersLiveData() {
        return allUsersLiveData;
    }

    /**
     * Get current user LiveData
     *
     * @return LiveData containing current user
     */
    public LiveData<UserEntity> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    /**
     * Get loading state LiveData
     *
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    /**
     * Get error LiveData
     *
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Load all users from database
     */
    public void loadAllUsers() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<UserEntity> users = userDao.getAllUsers();
                allUsersLiveData.postValue(users);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all users", e);
                errorLiveData.postValue("Failed to load users: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Get user by ID
     *
     * @param userId User ID to search for
     * @return UserEntity or null if not found
     */
    public void getUserById(long userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                UserEntity user = userDao.getUserById(userId);
                currentUserLiveData.postValue(user);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by ID: " + userId, e);
                errorLiveData.postValue("Failed to get user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Get user by email
     *
     * @param email Email to search for
     * @return UserEntity or null if not found
     */
    public void getUserByEmail(String email) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                UserEntity user = userDao.getUserByEmail(email);
                currentUserLiveData.postValue(user);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by email: " + email, e);
                errorLiveData.postValue("Failed to get user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Fetch user coin balance from API
     * @param token Authorization token
     */
    public void fetchUserCoin(String token) {
        apiService.getUserCoin("Bearer " + token).enqueue(new retrofit2.Callback<ApiService.ApiResponse<ApiService.CoinData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.CoinData>> call, Response<ApiService.ApiResponse<ApiService.CoinData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    int serverCoin = response.body().data.coin;
                    
                    // Update User model in SharedPreferences
                    User currentUser = getCurrentUser();
                    if (currentUser != null) {
                        currentUser.setTotalCoins(serverCoin);
                        saveUser(currentUser);
                    }
                    
                    // Update LiveData/DB
                    executorService.execute(() -> {
                        User currentUserModel = getCurrentUser();
                        if (currentUserModel != null) {
                            UserEntity userEntity = userDao.getUserById(currentUserModel.getUserId());
                            if (userEntity != null) {
                                userEntity.setTotalCoins(serverCoin);
                                userDao.updateUser(userEntity);
                                currentUserLiveData.postValue(userEntity);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to fetch user coin: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.CoinData>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch user coin", t);
            }
        });
    }

    /**
     * Sync user coin balance to API
     * @param token Authorization token
     * @param coinAmount New coin balance
     */
    public void syncUserCoin(String token, int coinAmount) {
        apiService.setUserCoin("Bearer " + token, new ApiService.CoinRequest(coinAmount)).enqueue(new retrofit2.Callback<ApiService.ApiResponse<ApiService.CoinData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.CoinData>> call, Response<ApiService.ApiResponse<ApiService.CoinData>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Coin synced successfully");
                } else {
                    Log.e(TAG, "Failed to sync coin: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.CoinData>> call, Throwable t) {
                Log.e(TAG, "Failed to sync coin", t);
            }
        });
    }

    /**
     * Create a new user
     *
     * @param user UserEntity to create
     * @return User ID of created user
     */
    public void createUser(UserEntity user) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long userId = userDao.insertUser(user);
                user.setUserId((int) userId);
                currentUserLiveData.postValue(user);
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating user", e);
                errorLiveData.postValue("Failed to create user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Create a new anonymous user
     *
     * @return UserEntity of created anonymous user
     */
    public void createAnonymousUser() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                UserEntity user = new UserEntity();
                user.setAnonymous(true);
                long userId = userDao.insertUser(user);
                user.setUserId((int) userId);
                currentUserLiveData.postValue(user);
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating anonymous user", e);
                errorLiveData.postValue("Failed to create anonymous user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Update user information
     *
     * @param user UserEntity with updated information
     */
    public void updateUser(UserEntity user) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                userDao.updateUser(user);
                currentUserLiveData.postValue(user);
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                errorLiveData.postValue("Failed to update user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Update user's last login time
     *
     * @param userId User ID to update
     */
    public void updateLastLogin(long userId) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                userDao.updateLastLogin(userId, currentTime);
                // Refresh current user if it's the same user
                UserEntity currentUser = currentUserLiveData.getValue();
                if (currentUser != null && currentUser.getUserId() == userId) {
                    currentUser.setLastLogin(new java.util.Date(currentTime));
                    currentUserLiveData.postValue(currentUser);
                }
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating last login for user: " + userId, e);
                errorLiveData.postValue("Failed to update last login: " + e.getMessage());
            }
        });
    }

    /**
     * Update user's display name
     *
     * @param userId      User ID to update
     * @param displayName New display name
     */
    public void updateDisplayName(long userId, String displayName) {
        executorService.execute(() -> {
            try {
                userDao.updateDisplayName(userId, displayName);
                // Refresh current user if it's the same user
                UserEntity currentUser = currentUserLiveData.getValue();
                if (currentUser != null && currentUser.getUserId() == userId) {
                    currentUser.setDisplayName(displayName);
                    currentUserLiveData.postValue(currentUser);
                }
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating display name for user: " + userId, e);
                errorLiveData.postValue("Failed to update display name: " + e.getMessage());
            }
        });
    }

    /**
     * Update user's profile picture URL
     *
     * @param userId            User ID to update
     * @param profilePictureUrl New profile picture URL
     */
    public void updateProfilePicture(long userId, String profilePictureUrl) {
        executorService.execute(() -> {
            try {
                userDao.updateProfilePicture(userId, profilePictureUrl);
                // Refresh current user if it's the same user
                UserEntity currentUser = currentUserLiveData.getValue();
                if (currentUser != null && currentUser.getUserId() == userId) {
                    currentUser.setProfilePictureUrl(profilePictureUrl);
                    currentUserLiveData.postValue(currentUser);
                }
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile picture for user: " + userId, e);
                errorLiveData.postValue("Failed to update profile picture: " + e.getMessage());
            }
        });
    }

    /**
     * Convert anonymous user to registered user
     *
     * @param userId      User ID to convert
     * @param email       User's email
     * @param displayName User's display name
     */
    public void convertToRegisteredUser(long userId, String email, String displayName) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                userDao.convertToRegisteredUser(userId, email, displayName);
                // Refresh current user if it's the same user
                UserEntity currentUser = currentUserLiveData.getValue();
                if (currentUser != null && currentUser.getUserId() == userId) {
                    currentUser.setAnonymous(false);
                    currentUser.setEmail(email);
                    currentUser.setDisplayName(displayName);
                    currentUserLiveData.postValue(currentUser);
                }
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error converting user to registered: " + userId, e);
                errorLiveData.postValue("Failed to convert user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Delete user by ID
     *
     * @param userId User ID to delete
     */
    public void deleteUser(long userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                userDao.deleteUserById(userId);
                // Clear current user if it's the same user
                UserEntity currentUser = currentUserLiveData.getValue();
                if (currentUser != null && currentUser.getUserId() == userId) {
                    currentUserLiveData.postValue(null);
                }
                loadAllUsers(); // Refresh the list
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user: " + userId, e);
                errorLiveData.postValue("Failed to delete user: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Check if email exists
     *
     * @param email Email to check
     * @return True if email exists, false otherwise
     */
    public void checkEmailExists(String email) {
        executorService.execute(() -> {
            try {
                boolean exists = userDao.isEmailExists(email);
                // You can post this result to a LiveData if needed
                Log.d(TAG, "Email exists: " + exists);
            } catch (Exception e) {
                Log.e(TAG, "Error checking email existence: " + email, e);
                errorLiveData.postValue("Failed to check email: " + e.getMessage());
            }
        });
    }

    /**
     * Get user count
     *
     * @return Total number of users
     */
    public void getUserCount() {
        executorService.execute(() -> {
            try {
                int count = userDao.getUserCount();
                Log.d(TAG, "Total users: " + count);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user count", e);
                errorLiveData.postValue("Failed to get user count: " + e.getMessage());
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Clear all data
     */
    public void clearAllData() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                userDao.deleteAllUsers();
                allUsersLiveData.postValue(null);
                currentUserLiveData.postValue(null);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing all data", e);
                errorLiveData.postValue("Failed to clear data: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Close repository and cleanup resources
     */
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Check if repository is closed
     *
     * @return True if closed, false otherwise
     */
    public boolean isClosed() {
        return executorService == null || executorService.isShutdown();
    }

    /**
     * Initialize user for the device
     * Creates a default user if none exists
     */
    public void initializeUser() {
        executorService.execute(() -> {
            try {
                // Sử dụng user ID cố định
                long userId = 1;

                // Kiểm tra user đã tồn tại chưa
                UserEntity existingUser = userDao.getUserById(userId);
                if (existingUser == null) {
                    // Tạo user mới
                    UserEntity newUser = new UserEntity();
                    newUser.setUserId(userId);
                    newUser.setDisplayName("User");
                    newUser.setAnonymous(true);
                    userDao.insertUser(newUser);
                    Log.d(TAG, "Created new user with ID: " + userId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing user", e);
            }
        });
    }

    /**
     * Get current user ID
     *
     * @return int user ID
     */
    public int getCurrentUserId() {
        return 1;
    }

    // ==================== USER MODEL METHODS (for Profile) ====================

    /**
     * Get current user (User model for profile)
     *
     * @return User object
     */
    public User getCurrentUser() {
        String userJson = sharedPreferences.getString(KEY_CURRENT_USER, null);
        if (userJson != null) {
            try {
                return gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing user JSON", e);
            }
        }
        return new User();
    }

    /**
     * Save user (User model for profile)
     *
     * @param user User object to save
     */
    public void saveUser(User user) {
        try {
            String userJson = gson.toJson(user);
            sharedPreferences.edit()
                    .putString(KEY_CURRENT_USER, userJson)
                    .apply();
            Log.d(TAG, "User saved successfully to Prefs");

            // Also save to Database to ensure Foreign Key constraints are met
            executorService.execute(() -> {
                try {
                    if (user.getUserId() > 0) {
                        UserEntity existing = userDao.getUserById(user.getUserId());
                        UserEntity entity = new UserEntity();
                        entity.setUserId(user.getUserId());
                        entity.setEmail(user.getEmail());
                        entity.setDisplayName(user.getName());
                        entity.setProfilePictureUrl(user.getProfileImageUrl());
                        entity.setTotalCoins(user.getTotalCoins());
                        entity.setLastLogin(new java.util.Date());
                        entity.setAnonymous(false);

                        if (existing == null) {
                            entity.setCreatedAt(new java.util.Date());
                            userDao.insertUser(entity);
                            Log.d(TAG, "User inserted into DB: " + user.getUserId());
                        } else {
                            entity.setCreatedAt(existing.getCreatedAt());
                            userDao.updateUser(entity);
                            Log.d(TAG, "User updated in DB: " + user.getUserId());
                        }
                        // Post value to LiveData to update UI immediately
                        currentUserLiveData.postValue(entity);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving user to DB", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error saving user", e);
        }
    }

    /**
     * Clear current user
     */
    public void clearCurrentUser() {
        sharedPreferences.edit()
                .remove(KEY_CURRENT_USER)
                .apply();
        Log.d(TAG, "Current user cleared");
    }

    /**
     * Check if user exists
     *
     * @return true if user data exists
     */
    public boolean hasCurrentUser() {
        return sharedPreferences.contains(KEY_CURRENT_USER);
    }

    /**
     * Callback interface for sync operations
     */
    public interface SyncCallback {
        void onSuccess(User user);

        void onError(String message);
    }

    /**
     * Sync Facebook user with Backend
     * Logic: Login with Firebase Token -> Save Token
     */
    public void syncFacebookUser(User fbUser, String firebaseToken, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);

                Log.d(TAG, "=== STARTING FACEBOOK SYNC ===");
                Log.d(TAG, "User: " + fbUser.getName());
                Log.d(TAG, "Firebase Token length: " + (firebaseToken != null ? firebaseToken.length() : 0));

                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Log.d(TAG, "ApiService created successfully");

                // Login with Firebase Token
                ApiService.LoginFirebaseRequest loginReq = new ApiService.LoginFirebaseRequest(firebaseToken);
                Call<ApiService.ApiResponse<ApiService.LoginResponseData>> loginCall = apiService
                        .loginFirebase(loginReq);
                Response<ApiService.ApiResponse<ApiService.LoginResponseData>> loginRes = loginCall.execute();

                // Log response details for debugging
                Log.d(TAG, "=== BACKEND LOGIN RESPONSE ===");
                Log.d(TAG, "HTTP Code: " + loginRes.code());
                Log.d(TAG, "Is Successful: " + loginRes.isSuccessful());
                Log.d(TAG, "Response Body is null: " + (loginRes.body() == null));

                // Try to log raw response if available
                if (loginRes.errorBody() != null) {
                    try {
                        String errorBody = loginRes.errorBody().string();
                        Log.d(TAG, "Error Body (if any): " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot read error body", e);
                    }
                }

                if (loginRes.body() != null) {
                    Log.d(TAG, "Response success: " + loginRes.body().success);
                    Log.d(TAG, "Response http_code: " + loginRes.body().httpCode);
                    Log.d(TAG, "Response message: " + loginRes.body().message);
                    Log.d(TAG, "Response data is null: " + (loginRes.body().data == null));

                    if (loginRes.body().data != null) {
                        Log.d(TAG, "Data accessToken is null: " + (loginRes.body().data.accessToken == null));
                        Log.d(TAG, "Data accessToken empty: " + (loginRes.body().data.accessToken != null
                                && loginRes.body().data.accessToken.isEmpty()));
                        if (loginRes.body().data.accessToken != null) {
                            Log.d(TAG, "Data accessToken length: " + loginRes.body().data.accessToken.length());
                        }
                    }
                } else {
                    Log.e(TAG, "Response body is NULL");
                    if (loginRes.errorBody() != null) {
                        try {
                            String errorBody = loginRes.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read error body", e);
                        }
                    }
                }
                Log.d(TAG, "==============================");

                if (loginRes.isSuccessful() && loginRes.body() != null && loginRes.body().success) {
                    if (loginRes.body().data != null && loginRes.body().data.accessToken != null) {
                        String token = loginRes.body().data.accessToken;
                        fbUser.setAccessToken(token);
                        fbUser.setRefreshToken(loginRes.body().data.refreshToken);
                        double expiresIn = loginRes.body().data.expiresIn;

                        // Calculate expiration time
                        long expiresAtMs;
                        if (expiresIn > 1_500_000_000) {
                            expiresAtMs = (long) (expiresIn * 1000);
                        } else {
                            expiresAtMs = System.currentTimeMillis() + (long) (expiresIn * 1000);
                        }
                        fbUser.setTokenExpiresAt(expiresAtMs);

                        Log.d(TAG, "Token Expiration: " + expiresIn);
                        Log.d(TAG, "Calculated ExpiresAt: " + expiresAtMs);

                        // Lưu user đã có token vào local
                        saveUser(fbUser);

                        // Sync sessions from server
                        SessionRepository.getInstance(context).fetchAndSaveSessionsFromApi(token, fbUser.getUserId());

                        Log.d(TAG, "=== BACKEND SYNC SUCCESS ===");
                        Log.d(TAG, "Access Token received successfully from backend");
                        Log.d(TAG, "Access Token: " + token);
                        Log.d(TAG, "Token length: " + token.length());
                        if (loginRes.body().data != null) {
                            Log.d(TAG, "Refresh Token: " + loginRes.body().data.refreshToken);
                            Log.d(TAG, "Expires In: " + loginRes.body().data.expiresIn + " seconds");
                            Log.d(TAG, "Token Type: " + loginRes.body().data.tokenType);
                        }
                        Log.d(TAG, "User saved with token: "
                                + (fbUser.getAccessToken() != null && !fbUser.getAccessToken().isEmpty()));
                        Log.d(TAG, "============================");
                        Log.d(TAG, "ACCESS TOKEN: " + token);
                        Log.d(TAG, "TOKEN LENGTH: " + (token != null ? token.length() : "null"));
                        Log.d(TAG, "USER ACCESS TOKEN: " + fbUser.getAccessToken());

                        // Kiểm tra token có được lưu đúng không
                        User savedUser = getCurrentUser();
                        Log.d(TAG, "SAVED USER TOKEN: " + savedUser);

                        // Post lên UI
                        currentUserLiveData.postValue(null); // Trigger update if needed

                        if (callback != null)
                            callback.onSuccess(fbUser);
                    } else {
                        Log.e(TAG, "=== BACKEND SYNC FAILED ===");
                        Log.e(TAG, "Response data or accessToken is null!");
                        Log.e(TAG, "Data is null: " + (loginRes.body().data == null));
                        if (loginRes.body().data != null) {
                            Log.e(TAG, "AccessToken is null: " + (loginRes.body().data.accessToken == null));
                        }
                        Log.e(TAG, "============================");

                        // Lưu user local ngay cả khi không có token
                        saveUser(fbUser);

                        if (callback != null)
                            callback.onError("Backend returned success but no access token");
                    }
                } else {
                    // Backend returned error or success=false
                    String errorMsg = "Login failed";
                    if (loginRes.body() != null) {
                        errorMsg = loginRes.body().message != null ? loginRes.body().message : "Login failed";
                        Log.e(TAG, "=== BACKEND LOGIN FAILED ===");
                        Log.e(TAG, "Response success: " + loginRes.body().success);
                        Log.e(TAG, "Response http_code: " + loginRes.body().httpCode);
                        Log.e(TAG, "Response message: " + loginRes.body().message);
                        Log.e(TAG, "Response data is null: " + (loginRes.body().data == null));

                        // Even if success=false, check if data exists
                        if (loginRes.body().data != null) {
                            Log.e(TAG, "Data exists but success=false");
                            Log.e(TAG,
                                    "AccessToken: " + (loginRes.body().data.accessToken != null ? "EXISTS" : "NULL"));
                            // Try to use token even if success=false (some backends do this)
                            if (loginRes.body().data.accessToken != null
                                    && !loginRes.body().data.accessToken.isEmpty()) {
                                String token = loginRes.body().data.accessToken;
                                fbUser.setAccessToken(token);
                                saveUser(fbUser);
                                Log.d(TAG, "=== TOKEN EXTRACTED DESPITE success=false ===");
                                Log.d(TAG, "Access Token: " + token);
                                Log.d(TAG, "Token length: " + token.length());
                                Log.d(TAG, "===========================================");

                                if (callback != null)
                                    callback.onSuccess(fbUser);
                                return; // Exit early
                            }
                        }
                        Log.e(TAG, "============================");
                    } else {
                        Log.e(TAG, "Response body is NULL");
                    }

                    if (loginRes.errorBody() != null) {
                        try {
                            String errorBody = loginRes.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read error body", e);
                        }
                    }

                    // Lưu user local ngay cả khi backend fail
                    saveUser(fbUser);

                    if (callback != null)
                        callback.onError("Backend Sync Failed: " + errorMsg);
                }
            } catch (IOException e) {
                Log.e(TAG, "Network Error during sync", e);

                // Lưu user local khi có network error
                saveUser(fbUser);

                String errorMessage;
                if (e.getMessage() != null && e.getMessage().contains("CLEARTEXT")) {
                    errorMessage = "CLEARTEXT communication to 10.0.2.2 not permitted by network security policy";
                } else {
                    errorMessage = "Network Error: " + e.getMessage();
                }

                if (callback != null)
                    callback.onError(errorMessage);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing with backend", e);

                // Lưu user local khi có lỗi khác
                saveUser(fbUser);

                if (callback != null)
                    callback.onError("Sync Error: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Refresh Access Token
     */
    public void refreshToken(User user, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty()) {
                    if (callback != null)
                        callback.onError("No refresh token available");
                    return;
                }

                Log.d(TAG, "refreshToken: " + user.getName());
                ApiService apiService = RetrofitClient.getInstance().getApiService();
                ApiService.RefreshTokenRequest request = new ApiService.RefreshTokenRequest(user.getRefreshToken());

                Call<ApiService.ApiResponse<ApiService.LoginResponseData>> call = apiService.refreshToken(request);
                Response<ApiService.ApiResponse<ApiService.LoginResponseData>> response = call.execute();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ApiService.LoginResponseData data = response.body().data;
                    if (data != null && data.accessToken != null) {
                        user.setAccessToken(data.accessToken);
                        if (data.refreshToken != null) {
                            user.setRefreshToken(data.refreshToken);
                        }

                        double expiresIn = data.expiresIn;
                        long expiresAtMs;
                        if (expiresIn > 1_500_000_000) {
                            expiresAtMs = (long) (expiresIn * 1000);
                        } else {
                            expiresAtMs = System.currentTimeMillis() + (long) (expiresIn * 1000);
                        }
                        user.setTokenExpiresAt(expiresAtMs);

                        saveUser(user);

                        if (callback != null)
                            callback.onSuccess(user);
                    } else {
                        if (callback != null)
                            callback.onError("Invalid response data");
                    }
                } else {
                    String errorMsg = "Refresh failed";
                    if (response.body() != null)
                        errorMsg = response.body().message;
                    if (callback != null)
                        callback.onError(errorMsg);
                }
            } catch (Exception e) {
                if (callback != null)
                    callback.onError("Network error: " + e.getMessage());
            }
        });
    }
}

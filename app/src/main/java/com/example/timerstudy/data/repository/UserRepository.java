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
        database = AppDatabase.getDatabase(context);
        userDao = database.userDao();
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
    public void getUserById(int userId) {
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
    public void updateLastLogin(int userId) {
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
    public void updateDisplayName(int userId, String displayName) {
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
    public void updateProfilePicture(int userId, String profilePictureUrl) {
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
    public void convertToRegisteredUser(int userId, String email, String displayName) {
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
    public void deleteUser(int userId) {
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
                int userId = 1;

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
                Log.d(TAG, "Reading User JSON: " + userJson);
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
            Log.d(TAG, "Saving User JSON: " + userJson);
            sharedPreferences.edit()
                    .putString(KEY_CURRENT_USER, userJson)
                    .apply();
            Log.d(TAG, "User saved successfully");
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
     * Logic: Login with Firebase Token -> Save Token and User Info
     */
    public void syncFacebookUser(User fbUser, String firebaseToken, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);

                Log.d(TAG, "=== STARTING FACEBOOK SYNC ===");
                Log.d(TAG, "User: " + fbUser.getName());
                Log.d(TAG, "Firebase Token: " + firebaseToken);

                ApiService apiService = RetrofitClient.getInstance().getApiService();

                // Login with Firebase Token
                ApiService.LoginFirebaseRequest loginReq = new ApiService.LoginFirebaseRequest(firebaseToken);
                Call<ApiService.ApiResponse<ApiService.LoginResponseData>> loginCall = apiService
                        .loginFirebase(loginReq);
                Response<ApiService.ApiResponse<ApiService.LoginResponseData>> loginRes = loginCall.execute();

                if (loginRes.isSuccessful() && loginRes.body() != null && loginRes.body().success) {
                    ApiService.LoginResponseData data = loginRes.body().data;
                    Log.d(TAG, "API Response Data: " + new Gson().toJson(data));

                    if (data.accessToken == null || data.accessToken.isEmpty()) {
                        Log.e(TAG, "ERROR: Access Token from API is NULL or EMPTY!");
                    } else {
                        Log.d(TAG, "Access Token from API: "
                                + data.accessToken.substring(0, Math.min(20, data.accessToken.length())) + "...");
                    }

                    // Calculate duration
                    long expiresInDuration = (long) data.expiresIn;
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;

                    // If expiresIn is a timestamp (e.g. > 1 year from epoch), convert to duration
                    if (expiresInDuration > currentTimeSeconds) {
                        expiresInDuration = expiresInDuration - currentTimeSeconds;
                    }

                    // Lưu tokens
                    fbUser.setTokens(
                            data.accessToken,
                            data.refreshToken,
                            expiresInDuration);

                    Log.d(TAG, "=== BACKEND SYNC SUCCESS ===");
                    Log.d(TAG, "Access Token: " + data.accessToken.substring(0, 20) + "...");
                    Log.d(TAG, "Refresh Token: " + data.refreshToken.substring(0, 20) + "...");
                    Log.d(TAG,
                            "Token expires in: " + expiresInDuration + " seconds");

                    // Merge user info từ backend (nếu có)
                    if (data.user != null) {
                        fbUser.setName(data.user.displayName);
                        fbUser.setEmail(data.user.email);
                        if (data.user.profilePictureUrl != null && !data.user.profilePictureUrl.isEmpty()) {
                            fbUser.setProfileImageUrl(data.user.profilePictureUrl);
                        }

                        Log.d(TAG, "Backend User ID: " + data.user.userId);
                        Log.d(TAG, "Backend Display Name: " + data.user.displayName);
                        Log.d(TAG, "Backend Email: " + data.user.email);
                        Log.d(TAG, "Backend Profile Picture: " + data.user.profilePictureUrl);
                    }

                    // Lưu user đã có token vào local
                    Log.d(TAG, "Saving user with token: "
                            + fbUser.getAccessToken().substring(0, Math.min(10, fbUser.getAccessToken().length()))
                            + "...");
                    saveUser(fbUser);

                    // Post lên UI
                    currentUserLiveData.postValue(null);

                    if (callback != null)
                        callback.onSuccess(fbUser);
                } else {
                    String errorMsg = (loginRes.body() != null) ? loginRes.body().message : "Login failed";
                    Log.e(TAG, "Login failed: " + errorMsg);
                    if (loginRes.errorBody() != null) {
                        Log.e(TAG, "Login error body: " + loginRes.errorBody().string());
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
}

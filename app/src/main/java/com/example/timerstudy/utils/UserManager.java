package com.example.timerstudy.utils;

import android.content.Context;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.model.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {
    private static final String TAG = "UserManager";
    private static UserManager instance;
    private UserRepository userRepository;

    private User currentUser;

    private UserManager(Context context) {
        userRepository = UserRepository.getInstance(context);
        loadCurrentUser();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    public void loadCurrentUser() {
        currentUser = userRepository.getCurrentUser();
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            loadCurrentUser();
        }

        // Debug log để kiểm tra
        if (currentUser != null) {
            android.util.Log.d(TAG, "=== GET CURRENT USER ===");
            android.util.Log.d(TAG, "User Name: " + currentUser.getName());
            android.util.Log.d(TAG, "Is Logged In: " + currentUser.isLoggedIn());
            android.util.Log.d(TAG, "Login Provider: " + currentUser.getLoginProvider());
            android.util.Log.d(TAG,
                    "Access Token: " + (currentUser.getAccessToken() != null && !currentUser.getAccessToken().isEmpty()
                            ? currentUser.getAccessToken().substring(0,
                                    Math.min(30, currentUser.getAccessToken().length())) + "..."
                            : "NULL/EMPTY"));
            android.util.Log.d(TAG, "Token Expired: " + currentUser.isTokenExpired());
            android.util.Log.d(TAG, "Token Expires At: " + currentUser.getTokenExpiresAt());
            android.util.Log.d(TAG, "Current Time: " + System.currentTimeMillis());
            android.util.Log.d(TAG, "========================");
        } else {
            android.util.Log.d(TAG, "getCurrentUser() returned NULL");
        }

        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void saveUser() {
        if (currentUser != null) {
            userRepository.saveUser(currentUser);
        }
    }

    // Coin Management
    public int getTotalCoins() {
        return getCurrentUser().getTotalCoins();
    }

    public void addCoins(int amount) {
        User user = getCurrentUser();
        user.setTotalCoins(user.getTotalCoins() + amount);
        saveUser();
    }

    public boolean subtractCoins(int amount) {
        User user = getCurrentUser();
        if (user.getTotalCoins() >= amount) {
            user.setTotalCoins(user.getTotalCoins() - amount);
            saveUser();
            return true;
        }
        return false;
    }

    public int getTimerDuration() {
        return getCurrentUser().getStudyDuration();
    }

    public void setTimerDuration(int duration) {
        User user = getCurrentUser();
        user.setStudyDuration(duration);
        saveUser();
    }

    public int getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }

    public void syncFacebookUser(User fbUser, String firebaseToken, UserRepository.SyncCallback callback) {
        userRepository.syncFacebookUser(fbUser, firebaseToken, callback);
    }

    // Hàm mới để sync session
    public void syncSession(Session session) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    // sessionRepository.syncSession(session, token, new
                    // SessionRepository.SyncCallback() {
                    // @Override
                    // public void onSuccess() {
                    // android.util.Log.d("UserManager", "Session synced successfully");
                    // }
                    //
                    // @Override
                    // public void onError(String message) {
                    // android.util.Log.e("UserManager", "Session sync failed: " + message);
                    // // Có thể lưu vào hàng đợi để sync sau bằng WorkManager nếu muốn
                    // }
                    // });
                }
            });
        }
    }

    /**
     * Debug method để log toàn bộ trạng thái user
     */
    public void debugUserState() {
        User user = getCurrentUser();
        android.util.Log.d(TAG, "=== DEBUG USER STATE ===");
        if (user != null) {
            android.util.Log.d(TAG, "User ID: " + user.getUserId());
            android.util.Log.d(TAG, "Name: " + user.getName());
            android.util.Log.d(TAG, "Email: " + user.getEmail());
            android.util.Log.d(TAG, "Is Logged In: " + user.isLoggedIn());
            android.util.Log.d(TAG, "Login Provider: " + user.getLoginProvider());
            android.util.Log.d(TAG, "Profile Image URL: " + user.getProfileImageUrl());
            android.util.Log.d(TAG,
                    "Access Token Present: " + (user.getAccessToken() != null && !user.getAccessToken().isEmpty()));
            android.util.Log.d(TAG,
                    "Access Token Length: " + (user.getAccessToken() != null ? user.getAccessToken().length() : 0));
            android.util.Log.d(TAG,
                    "Refresh Token Present: " + (user.getRefreshToken() != null && !user.getRefreshToken().isEmpty()));
            android.util.Log.d(TAG, "Token Expires At: " + user.getTokenExpiresAt());
            android.util.Log.d(TAG, "Current Time: " + System.currentTimeMillis());
            android.util.Log.d(TAG, "Is Token Expired: " + user.isTokenExpired());
        } else {
            android.util.Log.d(TAG, "User is NULL");
        }
        android.util.Log.d(TAG, "========================");
    }
}

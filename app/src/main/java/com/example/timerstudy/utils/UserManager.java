package com.example.timerstudy.utils;

import android.content.Context;
import android.util.Log;

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
    private SessionRepository sessionRepository;

    private User currentUser;

    private UserManager(Context context) {
        userRepository = UserRepository.getInstance(context);
        sessionRepository = SessionRepository.getInstance(context);
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

        if (currentUser != null) {
            android.util.Log.d(TAG, "User Name: " + currentUser.getName());
            android.util.Log.d(TAG, "Is Logged In: " + currentUser.isLoggedIn());
            android.util.Log.d(TAG,
                    "Access Token: " + (currentUser.getAccessToken() != null && !currentUser.getAccessToken().isEmpty()
                            ? currentUser.getAccessToken().substring(0,
                                    Math.min(30, currentUser.getAccessToken().length())) + "..."
                            : "NULL/EMPTY"));
            android.util.Log.d(TAG, "Token Expired: " + currentUser.isTokenExpired());
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

    public long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public void syncFacebookUser(User fbUser, String firebaseToken, UserRepository.SyncCallback callback) {
        userRepository.syncFacebookUser(fbUser, firebaseToken, new UserRepository.SyncCallback() {
            @Override
            public void onSuccess(User user) {
                setCurrentUser(user);
                android.util.Log.d("UserManager", "User synced and updated in UserManager");

                if (user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                    sessionRepository.fetchAndSaveSessionsFromApi(user.getAccessToken(), user.getUserId());
                }

                if (callback != null) {
                    callback.onSuccess(user);
                }
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onError(message);
                }
            }
        });
    }

    // Hàm mới để sync session
    public void syncSession(Session session) {
        User currentUser = getCurrentUser();
        Log.d("DebugSyncSession", "syncSession: " + currentUser.getAccessToken());

        if (currentUser.getAccessToken() != null && !currentUser.getAccessToken().isEmpty()) {
            String token = currentUser.getAccessToken();
            sessionRepository.syncSession(session, token, new SessionRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d("UserManager", "Session synced successfully");
                }

                @Override
                public void onError(String message) {
                    Log.e("UserManager", "Session sync failed: " + message);
                    // Có thể lưu vào hàng đợi để sync sau bằng WorkManager nếu muốn
                }
            });
        } else {
            android.util.Log.e("UserManager", "Cannot sync session: User not logged in or no access token");
        }
    }

    public void debugUserState() {
        User user = getCurrentUser();
        if (user != null) {
            android.util.Log.d(TAG, "User ID: " + user.getUserId());
            android.util.Log.d(TAG, "Name: " + user.getName());
            android.util.Log.d(TAG, "Email: " + user.getEmail());
            android.util.Log.d(TAG, "Is Logged In: " + user.isLoggedIn());
            android.util.Log.d(TAG, "Login Provider: " + user.getLoginProvider());
            android.util.Log.d(TAG, "Profile Image URL: " + user.getProfileImageUrl());
            android.util.Log.d(TAG, "Token Expires At: " + user.getTokenExpiresAt());
            android.util.Log.d(TAG, "Is Token Expired: " + user.isTokenExpired());
        } else {
            android.util.Log.d(TAG, "User is NULL");
        }
        android.util.Log.d(TAG, "========================");
    }

    public void checkAndRefreshToken(UserRepository.SyncCallback callback) {
        User user = getCurrentUser();
        if (user != null && user.isLoggedIn()) {
            if (user.isTokenExpired()) {
                userRepository.refreshToken(user, new UserRepository.SyncCallback() {
                    @Override
                    public void onSuccess(User refreshedUser) {
                        setCurrentUser(refreshedUser);
                        android.util.Log.d(TAG, "Token refresh successful");
                        if (callback != null) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                callback.onSuccess(refreshedUser);
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        android.util.Log.e(TAG, "Token refresh failed: " + message);
                        if (callback != null) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                callback.onError(message);
                            });
                        }
                    }
                });
            } else {
                android.util.Log.d(TAG, "Token is valid");
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(user);
                    });
                }
            }
        } else {
            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onError("User not logged in");
                });
            }
        }
    }
}

package com.example.timerstudy.utils;

import android.content.Context;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.model.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {
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
//                    sessionRepository.syncSession(session, token, new SessionRepository.SyncCallback() {
//                        @Override
//                        public void onSuccess() {
//                            android.util.Log.d("UserManager", "Session synced successfully");
//                        }
//
//                        @Override
//                        public void onError(String message) {
//                            android.util.Log.e("UserManager", "Session sync failed: " + message);
//                            // Có thể lưu vào hàng đợi để sync sau bằng WorkManager nếu muốn
//                        }
//                    });
                }
            });
        }
    }
}

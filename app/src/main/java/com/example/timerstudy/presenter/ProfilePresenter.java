package com.example.timerstudy.presenter;

import android.os.Bundle;
import android.util.Log;

import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.ProfileContract;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

public class ProfilePresenter implements ProfileContract.Presenter {

    private static final String TAG = "ProfilePresenter";
    private final ProfileContract.View view;
    private final UserManager userManager;
    private final FirebaseAuth mAuth;
    private User currentUser;

    public ProfilePresenter(ProfileContract.View view, UserManager userManager) {
        this.view = view;
        this.userManager = userManager;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadUserData() {
        currentUser = userManager.getCurrentUser();

        // Log để debug
        Log.d(TAG, "=== LOADING USER DATA ===");
        if (currentUser != null) {
            Log.d(TAG, "User found:");
            Log.d(TAG, "Name: " + currentUser.getName());
            Log.d(TAG, "IsLoggedIn: " + currentUser.isLoggedIn());
            Log.d(TAG, "Profile Image URL: " + currentUser.getProfileImageUrl());
            Log.d(TAG, "Login Provider: " + currentUser.getLoginProvider());
        } else {
            Log.d(TAG, "No user found");
        }
        Log.d(TAG, "==========================");

        updateView();
    }

    private void updateView() {
        if (currentUser != null) {
            if (currentUser.isLoggedIn()) {
                // Log để debug updateView
                Log.d(TAG, "=== UPDATE VIEW - LOGGED IN USER ===");
                Log.d(TAG, "Showing profile for: " + currentUser.getName());
                Log.d(TAG, "Profile Image URL: " + currentUser.getProfileImageUrl());

                view.showUserProfile(currentUser);
                view.showLogoutButton();
            } else {
                Log.d(TAG, "=== UPDATE VIEW - GUEST USER ===");
                view.showGuestMode();
                view.hideLogoutButton();
            }
            view.updateVibratorSwitch(currentUser.isVibratorEnabled());
        } else {
            Log.d(TAG, "=== UPDATE VIEW - NO USER ===");
            view.showGuestMode();
            view.hideLogoutButton();
        }
    }

    @Override
    public void setVibratorEnabled(boolean enabled) {
        if (currentUser != null) {
            currentUser.setVibratorEnabled(enabled);
            userManager.saveUser();
            view.showMessage("Vibrator " + (enabled ? "enabled" : "disabled"));
        }
    }

    @Override
    public void logout() {
        mAuth.signOut();
        if (currentUser != null) {
            currentUser.logout();
            userManager.saveUser();
        }
        updateView();
        view.showMessage("Logged out successfully");
    }

    @Override
    public void handleFacebookToken(AccessToken token) {
        view.showLoading();
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        // --- LẤY FIREBASE ID TOKEN ---
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();
                                            Log.d(TAG, "Firebase ID Token: " + firebaseIdToken);

                                            // Tiếp tục xử lý với token này
                                            requestFacebookUserData(token, firebaseIdToken);
                                        } else {
                                            Log.e(TAG, "Error getting Firebase ID Token", tokenTask.getException());
                                            view.hideLoading();
                                            view.showMessage("Error getting authentication token");
                                        }
                                    });
                        }
                    } else {
                        view.hideLoading();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        view.showMessage("Authentication failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void requestFacebookUserData(AccessToken accessToken, String firebaseIdToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    // Không hide loading ở đây nữa vì còn phải sync backend
                    try {
                        processFacebookUserData(object, firebaseIdToken);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Facebook user data", e);
                        view.hideLoading();
                        view.showMessage("Error getting user data");
                    }
                });

        Bundle parameters = new Bundle();
        // Only request public_profile fields (id, name, picture)
        parameters.putString("fields", "id,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void processFacebookUserData(JSONObject object, String firebaseIdToken) {
        try {
            // Log Firebase ID Token ở đây
            Log.d(TAG, "=== FIREBASE ID TOKEN ===");
            Log.d(TAG, firebaseIdToken);
            Log.d(TAG, "========================");

            String facebookId = object.optString("id", "");
            String name = object.optString("name", "Facebook User");
            // Email is not available with public_profile permission only
            String email = facebookId + "@facebook.local"; // Generate fallback email

            String profileImageUrl = "";
            if (object.has("picture")) {
                profileImageUrl = object.getJSONObject("picture")
                        .getJSONObject("data")
                        .optString("url", "");

                // Log avatar URL để kiểm tra
                Log.d(TAG, "=== FACEBOOK AVATAR URL ===");
                Log.d(TAG, "Profile Image URL: " + profileImageUrl);
                Log.d(TAG, "============================");
            }

            // Tạo User mới
            User facebookUser = new User();

            facebookUser.setUserId(facebookId);
            facebookUser.setName(name);
            facebookUser.setEmail(email);
            facebookUser.setProfileImageUrl(profileImageUrl);

            // Log toàn bộ thông tin user để debug
            Log.d(TAG, "=== USER INFO ===");
            Log.d(TAG, "User ID: " + facebookUser.getUserId());
            Log.d(TAG, "Name: " + facebookUser.getName());
            Log.d(TAG, "Email: " + facebookUser.getEmail());
            Log.d(TAG, "Profile Image URL: " + facebookUser.getProfileImageUrl());
            Log.d(TAG, "==================");

            facebookUser.setLoggedIn(true);
            facebookUser.setLoginProvider("facebook");
            facebookUser.setLastLoginTime(System.currentTimeMillis());

            // --- LOGIC QUAN TRỌNG: BẢO TOÀN DỮ LIỆU CŨ (Merge Data) ---
            if (currentUser != null) {
                facebookUser.setTotalCoins(currentUser.getTotalCoins());
                facebookUser.setTotalStudyMinutes(currentUser.getTotalStudyMinutes());
                facebookUser.setTotalSessions(currentUser.getTotalSessions());
                facebookUser.setCurrentStreak(currentUser.getCurrentStreak());
                facebookUser.setLongestStreak(currentUser.getLongestStreak());
                facebookUser.setLastStudyDate(currentUser.getLastStudyDate());
                facebookUser.setSelectedBackgroundId(currentUser.getSelectedBackgroundId());
                facebookUser.setStudyDuration(currentUser.getStudyDuration());
                facebookUser.setBreakDuration(currentUser.getBreakDuration());
                facebookUser.setSoundEnabled(currentUser.isSoundEnabled());
                facebookUser.setVibratorEnabled(currentUser.isVibratorEnabled());
            }

            currentUser = facebookUser;

            // QUAN TRỌNG: Lưu user ngay lập tức để đảm bảo dữ liệu được persist
            userManager.setCurrentUser(currentUser);
            userManager.saveUser();

            Log.d(TAG, "=== USER SAVED LOCALLY ===");
            Log.d(TAG, "Saved user: " + currentUser.getName());
            Log.d(TAG, "Profile Image URL: " + currentUser.getProfileImageUrl());
            Log.d(TAG, "============================");

            // Giữ loading hiển thị và chỉ update message
            view.showMessage("Syncing with server...");

            // Tạm thời giữ nguyên logic cũ
            userManager.syncFacebookUser(currentUser, new UserRepository.SyncCallback() {
                @Override
                public void onSuccess(User syncedUser) {
                    currentUser = syncedUser;
                    userManager.setCurrentUser(currentUser);
                    userManager.saveUser();

                    Log.d(TAG, "=== SYNC SUCCESS ===");
                    Log.d(TAG, "Synced user: " + syncedUser.getName());
                    Log.d(TAG, "Profile Image URL: " + syncedUser.getProfileImageUrl());
                    Log.d(TAG, "===================");

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();
                        view.showMessage("Welcome " + syncedUser.getName() + "!");
                    });
                }

                @Override
                public void onError(String message) {
                    // Đảm bảo user được lưu ngay cả khi sync thất bại
                    userManager.setCurrentUser(currentUser);
                    userManager.saveUser();

                    Log.d(TAG, "=== SYNC ERROR - SAVED LOCALLY ===");
                    Log.d(TAG, "Local user: " + currentUser.getName());
                    Log.d(TAG, "Profile Image URL: " + currentUser.getProfileImageUrl());
                    Log.d(TAG, "Error message: " + message);
                    Log.d(TAG, "==================================");

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();

                        // Hiển thị message phù hợp với lỗi
                        if (message.contains("CLEARTEXT")) {
                            view.showMessage("Login successful (offline mode)");
                        } else {
                            view.showMessage("Login local only. " + message);
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error processing data", e);
            view.hideLoading();
        }
    }
}

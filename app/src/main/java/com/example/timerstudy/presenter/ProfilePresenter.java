package com.example.timerstudy.presenter;

import android.os.Bundle;
import android.util.Log;

import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.view.contracts.ProfileContract;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ProfilePresenter implements ProfileContract.Presenter {

    private static final String TAG = "ProfilePresenter";
    private final ProfileContract.View view;
    private final UserRepository userRepository;
    private final FirebaseAuth mAuth;
    private User currentUser;

    public ProfilePresenter(ProfileContract.View view, UserRepository userRepository) {
        this.view = view;
        this.userRepository = userRepository;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadUserData() {
        currentUser = userRepository.getCurrentUser();
        updateView();
    }

    private void updateView() {
        if (currentUser != null) {
            if (currentUser.isLoggedIn()) {
                view.showUserProfile(currentUser);
            } else {
                view.showGuestMode();
            }
            view.updateVibratorSwitch(currentUser.isVibratorEnabled());
        }
    }

    @Override
    public void setVibratorEnabled(boolean enabled) {
        if (currentUser != null) {
            currentUser.setVibratorEnabled(enabled);
            userRepository.saveUser(currentUser);
            view.showMessage("Vibrator " + (enabled ? "enabled" : "disabled"));
        }
    }

    @Override
    public void logout() {
        mAuth.signOut();
        if (currentUser != null) {
            currentUser.logout();
            userRepository.saveUser(currentUser);
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
                    view.hideLoading();
                    try {
                        processFacebookUserData(object, firebaseIdToken);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Facebook user data", e);
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
            }

            // Tạo User mới
            User facebookUser = new User();

            facebookUser.setUserId(facebookId);
            facebookUser.setName(name);
            facebookUser.setEmail(email);
            facebookUser.setProfileImageUrl(profileImageUrl);

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

            // THAY ĐỔI: Gọi sync với backend, truyền thêm firebaseIdToken
            view.showLoading();
            view.showMessage("Syncing with server...");

            // TODO: Truyền firebaseIdToken vào syncFacebookUser
            // userRepository.syncFacebookUser(currentUser, firebaseIdToken, callback);

            // Tạm thời giữ nguyên logic cũ
            userRepository.syncFacebookUser(currentUser, new UserRepository.SyncCallback() {
                @Override
                public void onSuccess(User syncedUser) {
                    currentUser = syncedUser;

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();
                        view.showMessage("Welcome " + syncedUser.getName() + "!");
                    });
                }

                @Override
                public void onError(String message) {
                    userRepository.saveUser(currentUser);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();
                        view.showMessage("Login local only. " + message);
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error processing data", e);
            view.hideLoading();
        }
    }

    // Remove friends functionality since user_friends permission is deprecated
    // public void getFacebookFriendsList(AccessToken accessToken) { ... }
    // private void processFriendsList(JSONArray friendsArray) { ... }
}

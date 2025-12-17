package com.example.timerstudy.presenter;

import android.os.Bundle;
import android.util.Log;

import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.ProfileContract;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            Log.d(TAG, "ACCESS TOKEN: " + currentUser.getAccessToken());

            Log.d(TAG, "TOKEN LENGTH: " + (currentUser.getAccessToken() != null ? currentUser.getAccessToken().length() : "null"));
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
        Log.d(TAG, "=== LOGOUT PROCESS STARTED ===");

        // Logout from Firebase
        mAuth.signOut();
        Log.d(TAG, "Firebase signed out");

        // Logout from Facebook SDK (đảm bảo logout hoàn toàn)
        LoginManager.getInstance().logOut();
        Log.d(TAG, "Facebook SDK logged out");

        // Clear local user data
        if (currentUser != null) {
            Log.d(TAG, "Clearing user: " + currentUser.getName());
            currentUser.logout();
            userManager.setCurrentUser(currentUser);
            userManager.saveUser();
        }

        Log.d(TAG, "=== LOGOUT COMPLETED ===");

        updateView();
        view.showMessage("Logged out successfully");
    }

    @Override
    public void handleFacebookToken(AccessToken token) {
        view.showLoading();
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // Log Facebook Access Token
        Log.d(TAG, "=== FACEBOOK ACCESS TOKEN ===");
        Log.d(TAG, "Token: " + token.getToken());
        Log.d(TAG, "User ID: " + token.getUserId());
        Log.d(TAG, "Application ID: " + token.getApplicationId());
        Log.d(TAG, "Expires: " + token.getExpires());
        Log.d(TAG, "Permissions: " + token.getPermissions());
        Log.d(TAG, "Declined Permissions: " + token.getDeclinedPermissions());
        Log.d(TAG, "============================");

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        // --- LẤY FIREBASE ID TOKEN ---
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Force refresh token và đợi một chút để tránh "token used too early"
                            firebaseUser.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();
                                            Log.d(TAG, "Firebase ID Token received (length: " + firebaseIdToken.length() + ")");
                                            
                                            // Đợi 2 giây để đảm bảo token đã "chín" và tránh lỗi "token used too early"
                                            // Do lệch thời gian giữa client và server
                                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                                Log.d(TAG, "Delaying 2 seconds before sending token to backend to avoid 'token used too early' error");
                                                // Tiếp tục xử lý với token này
                                                requestFacebookUserData(token, firebaseIdToken);
                                            }, 2000); // 2 seconds delay
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
        // Log Facebook Access Token again before GraphRequest
        Log.d(TAG, "=== FACEBOOK ACCESS TOKEN (GraphRequest) ===");
        Log.d(TAG, "Token: " + accessToken.getToken());
        Log.d(TAG, "Is Expired: " + accessToken.isExpired());
        Log.d(TAG, "Permissions granted: " + accessToken.getPermissions());
        Log.d(TAG, "==========================================");

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    // Không hide loading ở đây nữa vì còn phải sync backend
                    try {
                        processFacebookUserData(object, firebaseIdToken);

                        // Nếu có quyền user_friends, lấy danh sách bạn bè
                        if (accessToken.getPermissions().contains("user_friends")) {
                            Log.d(TAG, "user_friends permission granted, fetching friends list...");
                            getFacebookFriendsList(accessToken);
                        } else {
                            Log.d(TAG, "user_friends permission NOT granted");
                        }
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

    /**
     * Lấy danh sách bạn bè Facebook
     * Lưu ý: user_friends chỉ trả về bạn bè cũng sử dụng app này
     */
    private void getFacebookFriendsList(AccessToken accessToken) {
        GraphRequest friendsRequest = new GraphRequest(
                accessToken,
                "/me/friends",
                null,
                null,
                response -> {
                    try {
                        Log.d(TAG, "=== FACEBOOK FRIENDS RESPONSE ===");
                        Log.d(TAG, "Response: " + response);

                        JSONObject jsonResponse = response.getJSONObject();
                        if (jsonResponse != null && jsonResponse.has("data")) {
                            JSONArray friendsArray = jsonResponse.getJSONArray("data");

                            Log.d(TAG, "Total friends using this app: " + friendsArray.length());

                            if (friendsArray.length() > 0) {
                                processFriendsList(friendsArray);
                            } else {
                                Log.d(TAG, "No friends using this app found");
                            }
                        }

                        Log.d(TAG, "==================================");
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing friends list", e);
                    }
                });

        friendsRequest.executeAsync();
    }

    /**
     * Xử lý danh sách bạn bè
     */
    private void processFriendsList(JSONArray friendsArray) {
        try {
            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friend = friendsArray.getJSONObject(i);
                String friendId = friend.optString("id");
                String friendName = friend.optString("name");

                Log.d(TAG, "Friend " + (i + 1) + ": " + friendName + " (ID: " + friendId + ")");

                // TODO: Lưu danh sách bạn bè vào database hoặc hiển thị lên UI
                // Có thể thêm vào User model hoặc tạo entity riêng để lưu friends
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing friends list", e);
        }
    }

    private void processFacebookUserData(JSONObject object, String firebaseIdToken) {
        try {
            // Log Firebase ID Token ở đây
            Log.d(TAG, "=== FIREBASE ID TOKEN ===");
            Log.d(TAG, firebaseIdToken);
            Log.d(TAG, "========================");

            String facebookIdString = object.optString("id", "");
            long facebookIdLong = 0;
            try {
                facebookIdLong = Long.parseLong(facebookIdString);
            } catch (NumberFormatException e) {
                facebookIdLong = 0;
            }
            String name = object.optString("name", "Facebook User");
            // Email is not available with public_profile permission only
            String email = facebookIdLong + "@facebook.local"; // Generate fallback email

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

            facebookUser.setUserId(facebookIdLong);
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
            userManager.syncFacebookUser(currentUser, firebaseIdToken, new UserRepository.SyncCallback() {
                @Override
                public void onSuccess(User syncedUser) {
                    currentUser = syncedUser;
                    userManager.setCurrentUser(currentUser);
                    userManager.saveUser();

                    Log.d(TAG, "=== SYNC SUCCESS ===");
                    Log.d(TAG, "Synced user: " + syncedUser.getName());
                    Log.d(TAG, "Profile Image URL: " + syncedUser.getProfileImageUrl());
                    Log.d(TAG, "ACCESS TOKEN FROM SYNC: " + syncedUser.getAccessToken());
                    Log.d(TAG, "TOKEN LENGTH: " + (syncedUser.getAccessToken() != null ? syncedUser.getAccessToken().length() : "null"));
                    Log.d(TAG,
                            "Access Token: " + (syncedUser.getAccessToken() != null
                                    ? syncedUser.getAccessToken().substring(0, 20) + "..."
                                    : "null"));
                    Log.d(TAG,
                            "Refresh Token: " + (syncedUser.getRefreshToken() != null
                                    ? syncedUser.getRefreshToken().substring(0, 20) + "..."
                                    : "null"));
                    Log.d(TAG, "Token Expires At: " + syncedUser.getTokenExpiresAt());
                    Log.d(TAG, "Is Token Expired: " + syncedUser.isTokenExpired());
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

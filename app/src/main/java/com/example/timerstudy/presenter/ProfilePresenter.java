package com.example.timerstudy.presenter;

import android.os.Bundle;
import android.util.Log;

import com.example.timerstudy.data.api.ApiClient;
import com.example.timerstudy.data.api.request.LoginFirebaseRequest;
import com.example.timerstudy.data.api.response.LoginResponse;
import com.example.timerstudy.data.api.service.AuthApiService;
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

        // Clear backend access token
        ApiClient.clearAccessToken();
        Log.d(TAG, "Backend access token cleared");

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
            currentUser.setAccessToken("");
            currentUser.setRefreshToken("");
            currentUser.setTokenExpiresAt(0);
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
                            firebaseUser.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();
                                            Log.d(TAG, "Firebase ID Token: " + firebaseIdToken);

                                            // Gọi API login-firebase để lấy access_token
                                            loginWithFirebaseToken(firebaseIdToken, token);
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

    /**
     * Gọi API backend để login với Firebase ID Token
     * Lấy access_token và refresh_token từ backend
     */
    private void loginWithFirebaseToken(String firebaseIdToken, AccessToken facebookAccessToken) {
        Log.d(TAG, "=== CALLING LOGIN-FIREBASE API ===");
        Log.d(TAG, "Firebase ID Token: " + firebaseIdToken.substring(0, Math.min(50, firebaseIdToken.length())) + "...");

        // Tạo request body
        LoginFirebaseRequest request = new LoginFirebaseRequest(firebaseIdToken);

        // Tạo API service
        AuthApiService authService = ApiClient.getClient().create(AuthApiService.class);

        // Gọi API
        Call<LoginResponse> call = authService.loginWithFirebase(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "=== LOGIN-FIREBASE API RESPONSE ===");
                Log.d(TAG, "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "Success: " + loginResponse.isSuccess());
                    Log.d(TAG, "Message: " + loginResponse.getMessage());

                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        LoginResponse.LoginData data = loginResponse.getData();
                        
                        // Lưu tokens
                        String accessToken = data.getAccessToken();
                        String refreshToken = data.getRefreshToken();
                        long expiresIn = data.getExpiresIn();

                        Log.d(TAG, "Access Token: " + (accessToken != null ? accessToken.substring(0, Math.min(50, accessToken.length())) + "..." : "null"));
                        Log.d(TAG, "Refresh Token: " + (refreshToken != null ? refreshToken.substring(0, Math.min(50, refreshToken.length())) + "..." : "null"));
                        Log.d(TAG, "Expires In: " + expiresIn + " seconds");

                        // Set access token vào ApiClient
                        ApiClient.setAccessToken(accessToken);

                        // Tiếp tục lấy thông tin user từ Facebook
                        requestFacebookUserData(facebookAccessToken, firebaseIdToken, accessToken, refreshToken, expiresIn);
                    } else {
                        Log.e(TAG, "Login failed: " + loginResponse.getMessage());
                        view.hideLoading();
                        view.showMessage("Login failed: " + loginResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                        view.hideLoading();
                        view.showMessage("Login failed: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        view.hideLoading();
                        view.showMessage("Login failed with code: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "=== LOGIN-FIREBASE API FAILURE ===");
                Log.e(TAG, "Error: " + t.getMessage(), t);
                view.hideLoading();
                view.showMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void requestFacebookUserData(AccessToken accessToken, String firebaseIdToken, 
                                         String backendAccessToken, String backendRefreshToken, long expiresIn) {
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
                        processFacebookUserData(object, firebaseIdToken, backendAccessToken, backendRefreshToken, expiresIn);

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
                        Log.d(TAG, "Response: " + response.toString());

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

    private void processFacebookUserData(JSONObject object, String firebaseIdToken,
                                         String backendAccessToken, String backendRefreshToken, long expiresIn) {
        try {
            // Log Firebase ID Token ở đây
            Log.d(TAG, "=== FIREBASE ID TOKEN ===");
            Log.d(TAG, firebaseIdToken);
            Log.d(TAG, "========================");

            // Log Backend Tokens
            Log.d(TAG, "=== BACKEND TOKENS ===");
            Log.d(TAG, "Access Token: " + (backendAccessToken != null ? backendAccessToken.substring(0, Math.min(50, backendAccessToken.length())) + "..." : "null"));
            Log.d(TAG, "Refresh Token: " + (backendRefreshToken != null ? backendRefreshToken.substring(0, Math.min(50, backendRefreshToken.length())) + "..." : "null"));
            Log.d(TAG, "Expires In: " + expiresIn + " seconds");
            Log.d(TAG, "=====================");

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

            // Lưu tokens vào User
            facebookUser.setTokens(backendAccessToken, backendRefreshToken, expiresIn);

            // Log toàn bộ thông tin user để debug
            Log.d(TAG, "=== USER INFO ===");
            Log.d(TAG, "User ID: " + facebookUser.getUserId());
            Log.d(TAG, "Name: " + facebookUser.getName());
            Log.d(TAG, "Email: " + facebookUser.getEmail());
            Log.d(TAG, "Profile Image URL: " + facebookUser.getProfileImageUrl());
            Log.d(TAG, "Access Token: " + (facebookUser.getAccessToken() != null ? "Saved" : "null"));
            Log.d(TAG, "Refresh Token: " + (facebookUser.getRefreshToken() != null ? "Saved" : "null"));
            Log.d(TAG, "Token Expires At: " + facebookUser.getTokenExpiresAt());
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

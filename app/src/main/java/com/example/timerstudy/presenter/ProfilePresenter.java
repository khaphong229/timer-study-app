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

        if (currentUser != null) {
            Log.d(TAG, "Name: " + currentUser.getName());
            Log.d(TAG, "IsLoggedIn: " + currentUser.isLoggedIn());
            Log.d(TAG, "ACCESS TOKEN: " + currentUser.getAccessToken());
        } else {
            Log.d(TAG, "No user found");
        }
        updateView();
    }

    private void updateView() {
        if (currentUser != null) {
            if (currentUser.isLoggedIn()) {
                view.showUserProfile(currentUser);
                view.showLogoutButton();
            } else {
                view.showGuestMode();
                view.hideLogoutButton();
            }
            view.updateVibratorSwitch(currentUser.isVibratorEnabled());
        } else {
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
        Log.d(TAG, "Firebase signed out");

        LoginManager.getInstance().logOut();
        Log.d(TAG, "Facebook SDK logged out");

        if (currentUser != null) {
            currentUser.logout();
            userManager.setCurrentUser(currentUser);
            userManager.saveUser();
        }

        updateView();
        view.showMessage("Logged out successfully");
    }

    @Override
    public void handleFacebookToken(AccessToken token) {
        view.showLoading();
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        Log.d(TAG, "Token: " + token.getToken());
        Log.d(TAG, "User ID: " + token.getUserId());

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // firebase id token
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();
                                            Log.d(TAG, "Firebase ID Token: " + firebaseIdToken);

                                            new android.os.Handler(android.os.Looper.getMainLooper())
                                                    .postDelayed(() -> {
                                                        requestFacebookUserData(token, firebaseIdToken);
                                                    }, 2000);
                                        } else {
                                            Log.e(TAG, "error get firebase id token", tokenTask.getException());
                                            view.hideLoading();
                                            view.showMessage("Error getting authentication token");
                                        }
                                    });
                        }
                    } else {
                        view.hideLoading();
                        Log.w(TAG, "signInWithCredential", task.getException());
                        view.showMessage("Authentication failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void requestFacebookUserData(AccessToken accessToken, String firebaseIdToken) {

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    try {
                        processFacebookUserData(object, firebaseIdToken);

                        if (accessToken.getPermissions().contains("user_friends")) {
                            getFacebookFriendsList(accessToken);
                        } else {
                            Log.d(TAG, "user_friends permission NOT granted");
                        }
                    } catch (Exception e) {
                        view.hideLoading();
                        view.showMessage("Error getting user data");
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getFacebookFriendsList(AccessToken accessToken) {
        GraphRequest friendsRequest = new GraphRequest(
                accessToken,
                "/me/friends",
                null,
                null,
                response -> {
                    try {
                        Log.d(TAG, "response " + response);

                        JSONObject jsonResponse = response.getJSONObject();
                        if (jsonResponse != null && jsonResponse.has("data")) {
                            JSONArray friendsArray = jsonResponse.getJSONArray("data");

                            if (friendsArray.length() > 0) {
                                processFriendsList(friendsArray);
                            } else {
                                Log.d(TAG, "not found friends");
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                });

        friendsRequest.executeAsync();
    }

    private void processFriendsList(JSONArray friendsArray) {
        try {
            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friend = friendsArray.getJSONObject(i);
                String friendId = friend.optString("id");
                String friendName = friend.optString("name");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing friends list", e);
        }
    }

    private void processFacebookUserData(JSONObject object, String firebaseIdToken) {
        try {
            Log.d(TAG, firebaseIdToken);

            String facebookIdString = object.optString("id", "");
            long facebookIdLong = 0;
            try {
                facebookIdLong = Long.parseLong(facebookIdString);
            } catch (NumberFormatException e) {
                facebookIdLong = 0;
            }
            String name = object.optString("name", "Facebook User");
            String email = facebookIdLong + "@facebook.local";

            String profileImageUrl = "";
            if (object.has("picture")) {
                profileImageUrl = object.getJSONObject("picture")
                        .getJSONObject("data")
                        .optString("url", "");
            }

            User facebookUser = new User();

            facebookUser.setUserId(facebookIdLong);
            facebookUser.setName(name);
            facebookUser.setEmail(email);
            facebookUser.setProfileImageUrl(profileImageUrl);

            facebookUser.setLoggedIn(true);
            facebookUser.setLoginProvider("facebook");
            facebookUser.setLastLoginTime(System.currentTimeMillis());

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

            userManager.setCurrentUser(currentUser);
            userManager.saveUser();

            view.showMessage("Syncing with server...");

            userManager.syncFacebookUser(currentUser, firebaseIdToken, new UserRepository.SyncCallback() {
                @Override
                public void onSuccess(User syncedUser) {
                    currentUser = syncedUser;
                    userManager.setCurrentUser(currentUser);
                    userManager.saveUser();

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();
                        view.showMessage("Welcome " + syncedUser.getName() + "!");
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Backend sync failed: " + message);

                    mAuth.signOut();
                    LoginManager.getInstance().logOut();

                    if (currentUser != null) {
                        currentUser.logout();
                        userManager.setCurrentUser(currentUser);
                        userManager.saveUser();
                    }

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        view.hideLoading();
                        updateView();
                        view.showMessage("Login failed: " + message + ". Please try again.");
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error processing data", e);
            view.hideLoading();
        }
    }
}

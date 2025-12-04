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

import org.json.JSONObject;
import org.json.JSONArray;

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
                        requestFacebookUserData(token);
                    } else {
                        view.hideLoading();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        view.showMessage("Authentication failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void requestFacebookUserData(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    view.hideLoading();
                    try {
                        processFacebookUserData(object);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Facebook user data", e);
                        view.showMessage("Error getting user data");
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void processFacebookUserData(JSONObject object) {
        try {
            String facebookId = object.optString("id", "");
            String name = object.optString("name", "Facebook User");
            String email = object.optString("email", "");

            String profileImageUrl = "";
            if (object.has("picture")) {
                profileImageUrl = object.getJSONObject("picture")
                        .getJSONObject("data")
                        .optString("url", "");
            }

            // --- TẠO USER MỚI (Sử dụng Constructor hoặc Setters để an toàn) ---
            // Giả sử bạn có constructor User(id, name, email) hoặc dùng setters
            User facebookUser = new User(facebookId, name, email);
            facebookUser.setProfileImageUrl(profileImageUrl);

            // Nếu User không có constructor trên, hãy dùng:
            // User facebookUser = new User();
            // facebookUser.setId(facebookId); ...

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
            // -----------------------------------------------------------

            currentUser = facebookUser;
            userRepository.saveUser(currentUser);

            updateView();
            view.showMessage("Welcome " + name + "!");

        } catch (Exception e) {
            Log.e(TAG, "Error processing data", e);
        }
    }

    public void getFacebookFriendsList(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMyFriendsRequest(
                accessToken,
                (objects, response) -> {
                    try {
                        // objects là JSONArray chứa danh sách bạn bè
                        Log.d(TAG, "Friends List Response: " + response.toString());

                        if (objects != null) {
                            processFriendsList(objects);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting friends", e);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void processFriendsList(JSONArray friendsArray) {
        try {
            // Danh sách ID của bạn bè dùng app
            List<String> friendIds = new ArrayList<>();

            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friendObj = friendsArray.getJSONObject(i);
                String friendId = friendObj.getString("id");
                String friendName = friendObj.getString("name");

                friendIds.add(friendId);
                Log.d(TAG, "Found friend using app: " + friendName + " (ID: " + friendId + ")");
            }

            // BƯỚC TIẾP THEO (QUAN TRỌNG):
            // Sau khi có list friendIds này, bạn phải gửi list này lên Firebase
            // để lấy thông tin "TotalStudyMinutes" của từng ID.
            // fetchFriendsDataFromFirebase(friendIds);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing friends list", e);
        }
    }
}

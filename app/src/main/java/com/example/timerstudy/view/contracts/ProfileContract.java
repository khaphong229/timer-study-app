package com.example.timerstudy.view.contracts;

import com.example.timerstudy.model.User;
import com.facebook.AccessToken;

public interface ProfileContract {
    interface View {
        void showUserProfile(User user);

        void showGuestMode();

        void showMessage(String message);

        void updateVibratorSwitch(boolean isEnabled);

        void showLoading();

        void hideLoading();

        void showLogoutButton();

        void hideLogoutButton();

        void loadUserAvatar(String imageUrl);

        void showDefaultAvatar();
    }

    interface Presenter {
        void loadUserData();

        void handleFacebookToken(AccessToken token);

        void logout();

        void setVibratorEnabled(boolean enabled);
    }
}

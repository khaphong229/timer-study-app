package com.example.timerstudy.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.timerstudy.R;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.timerstudy.model.User;
import com.example.timerstudy.presenter.ProfilePresenter;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.ProfileContract;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;

public class ProfileFragment extends Fragment implements ProfileContract.View {

    private static final String TAG = "ProfileFragment";

    private ImageView ivAvatar;
    private TextView tvUserName;
    private LoginButton btnLoginFacebook;
    private SwitchCompat switchVibrator;
    private LinearLayout btnNotifications;
    private LinearLayout btnPrivacy;
    private LinearLayout btnLogout;

    // Loading UI
    private FrameLayout loadingOverlay;
    private TextView tvLoadingMessage;

    private ProfilePresenter presenter;
    private CallbackManager mCallbackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        initFacebookLogin();
        setupListeners();

        presenter.loadUserData();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        btnLoginFacebook = view.findViewById(R.id.btnLoginFacebook);
        switchVibrator = view.findViewById(R.id.switchVibrator);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnPrivacy = view.findViewById(R.id.btnPrivacy);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Loading UI
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);

        btnLogout.setVisibility(View.GONE);
    }

    private void initPresenter() {
        UserManager userManager = UserManager.getInstance(requireContext());
        presenter = new ProfilePresenter(this, userManager);
    }

    private void initFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        btnLoginFacebook.setFragment(this);
        btnLoginFacebook.setReadPermissions("public_profile");

        btnLoginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                presenter.handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                showMessage("Facebook login cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                String errorMessage = error.getMessage();
                if (errorMessage != null && errorMessage.contains("key hash")) {
                    showMessage("Facebook configuration error. Please check app settings.");
                    Log.e(TAG,
                            "Key hash error - add the following to Facebook app settings: dtXwpvkdPGYCQI9CIWE3eQPPrFI=");
                } else {
                    showMessage("Facebook login error: " + errorMessage);
                }
            }
        });
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        btnNotifications.setOnClickListener(v -> showMessage("Notifications Settings (Coming soon)"));

        btnPrivacy.setOnClickListener(v -> showMessage("Privacy Settings (Coming soon)"));

        switchVibrator.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setVibratorEnabled(isChecked));
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Logout from Facebook SDK first
                    LoginManager.getInstance().logOut();
                    Log.d(TAG, "Facebook SDK logged out");

                    // Then logout from our app
                    presenter.logout();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void showUserProfile(User user) {
        Log.d(TAG, "=== SHOW USER PROFILE ===");
        Log.d(TAG, "User name: " + user.getName());
        Log.d(TAG, "Profile Image URL: " + user.getProfileImageUrl());
        Log.d(TAG, "=========================");

        tvUserName.setText(user.getName());
        btnLoginFacebook.setVisibility(View.GONE);

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            loadUserAvatar(user.getProfileImageUrl());
            Log.d(TAG, "Loading Facebook avatar: " + user.getProfileImageUrl());
        } else {
            Log.d(TAG, "No profile image URL, showing default avatar");
            showDefaultAvatar();
        }
    }

    @Override
    public void showGuestMode() {
        Log.d(TAG, "=== SHOW GUEST MODE ===");
        tvUserName.setText("Guest User");
        btnLoginFacebook.setVisibility(View.VISIBLE);
        showDefaultAvatar();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateVibratorSwitch(boolean isEnabled) {
        if (switchVibrator.isChecked() != isEnabled) {
            switchVibrator.setChecked(isEnabled);
        }
    }

    @Override
    public void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading overlay shown");
        }
    }

    @Override
    public void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            Log.d(TAG, "Loading overlay hidden");
        }
    }

    @Override
    public void showLogoutButton() {
        btnLogout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLogoutButton() {
        btnLogout.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void loadUserAvatar(String imageUrl) {
        Log.d(TAG, "=== LOAD USER AVATAR ===");
        Log.d(TAG, "Image URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.sbg_rain_girl_frog)
                    .error(R.drawable.sbg_rain_girl_frog);

            Glide.with(this)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(ivAvatar);

            Log.d(TAG, "Avatar loaded successfully from: " + imageUrl);
        } else {
            Log.d(TAG, "Empty image URL, showing default avatar");
            showDefaultAvatar();
        }
    }

    @Override
    public void showDefaultAvatar() {
        Log.d(TAG, "=== SHOW DEFAULT AVATAR ===");
        ivAvatar.setImageResource(R.drawable.sbg_rain_girl_frog);
    }

    @Override
    public void updateLoadingMessage(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
            Log.d(TAG, "Loading message updated: " + message);
        }
    }
}

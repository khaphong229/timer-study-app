package com.example.timerstudy.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.timerstudy.R;

import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.model.User;
import com.example.timerstudy.presenter.ProfilePresenter;
import com.example.timerstudy.view.contracts.ProfileContract;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
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

        // Load data
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
    }

    private void initPresenter() {
        UserRepository repository = UserRepository.getInstance(requireContext());
        presenter = new ProfilePresenter(this, repository);
    }

    private void initFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        btnLoginFacebook.setFragment(this);
        btnLoginFacebook.setReadPermissions("public_profile", "email", "user_friends");

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
                showMessage("Facebook login error: " + error.getMessage());
            }
        });
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> presenter.logout());

        btnNotifications.setOnClickListener(v -> showMessage("Notifications Settings (Coming soon)"));

        btnPrivacy.setOnClickListener(v -> showMessage("Privacy Settings (Coming soon)"));

        switchVibrator.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setVibratorEnabled(isChecked));
    }

    // --- View Interface Implementation ---

    @Override
    public void showUserProfile(User user) {
        tvUserName.setText(user.getName());
        btnLoginFacebook.setVisibility(View.GONE);
        // TODO: Load avatar image using Glide/Picasso if user.getProfileImageUrl() is
        // not empty
    }

    @Override
    public void showGuestMode() {
        tvUserName.setText("Guest User");
        btnLoginFacebook.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateVibratorSwitch(boolean isEnabled) {
        // Avoid triggering listener loop if needed, though simple setChecked is usually
        // fine
        if (switchVibrator.isChecked() != isEnabled) {
            switchVibrator.setChecked(isEnabled);
        }
    }

    @Override
    public void showLoading() {
        // Optional: Show a progress dialog or loading indicator
    }

    @Override
    public void hideLoading() {
        // Optional: Hide progress dialog
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

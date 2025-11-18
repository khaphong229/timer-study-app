package com.example.timerstudy.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView ivAvatar;
    private TextView tvUserName;
    private LoginButton btnLoginFacebook;
    private SwitchCompat switchVibrator;
    private LinearLayout btnNotifications;
    private LinearLayout btnPrivacy;
    private LinearLayout btnLogout;

    private UserRepository userRepository;
    private User currentUser;

    // Facebook and Firebase
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();
        initFacebookLogin();
        loadUserData();
        setupListeners();
    }
    
    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        btnLoginFacebook = view.findViewById(R.id.btnLoginFacebook);
        switchVibrator = view.findViewById(R.id.switchVibrator);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnPrivacy = view.findViewById(R.id.btnPrivacy);
        btnLogout = view.findViewById(R.id.btnLogout);

        userRepository = UserRepository.getInstance(requireContext());
    }

    private void initFirebase() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void initFacebookLogin() {
        // Initialize Facebook Login callback manager
        mCallbackManager = CallbackManager.Factory.create();

        // CRITICAL: Set the Fragment for LoginButton to handle lifecycle correctly
        btnLoginFacebook.setFragment(this);

        // Set read permissions to request user data
        // Note: Only use "public_profile" for now. "email" requires Advanced Access from Facebook
        btnLoginFacebook.setReadPermissions("public_profile");

        // Register callback for Facebook login
        btnLoginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                // Get Facebook access token and authenticate with Firebase
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(requireContext(), "Facebook login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(requireContext(), "Facebook login error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadUserData() {
        currentUser = userRepository.getCurrentUser();
        updateUI();
    }
    
    private void updateUI() {
        if (currentUser.isLoggedIn()) {
            tvUserName.setText(currentUser.getName());
            btnLoginFacebook.setVisibility(View.GONE);
        } else {
            tvUserName.setText("Guest User");
            btnLoginFacebook.setVisibility(View.VISIBLE);
        }
        
        switchVibrator.setChecked(currentUser.isVibratorEnabled());
    }
    
    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            // Logout from Firebase
            mAuth.signOut();

            // Logout from local user
            currentUser.logout();
            userRepository.saveUser(currentUser);
            updateUI();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Notifications Settings (Coming soon)", Toast.LENGTH_SHORT).show();
        });

        btnPrivacy.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Privacy Settings (Coming soon)", Toast.LENGTH_SHORT).show();
        });

        switchVibrator.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentUser.setVibratorEnabled(isChecked);
            userRepository.saveUser(currentUser);
            Toast.makeText(requireContext(),
                "Vibrator " + (isChecked ? "enabled" : "disabled"),
                Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Handle Facebook Access Token - Exchange it for Firebase credential
     * and authenticate with Firebase
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // Create Firebase credential from Facebook token
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        // Sign in to Firebase with the credential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        // Request Facebook user data with Graph API
                        requestFacebookUserData(token);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(requireContext(), "Authentication failed: " +
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Request user data from Facebook Graph API
     * This allows us to get additional user information beyond basic authentication
     */
    private void requestFacebookUserData(AccessToken accessToken) {
        // Create Graph API request to get user data
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    try {
                        Log.d(TAG, "Facebook Graph Response: " + object.toString());

                        // Extract user data from JSON response
                        String facebookId = object.optString("id", "");
                        String name = object.optString("name", "Facebook User");
                        String email = object.optString("email", "");

                        // Get profile picture URL
                        String profileImageUrl = "";
                        if (object.has("picture")) {
                            profileImageUrl = object.getJSONObject("picture")
                                    .getJSONObject("data")
                                    .optString("url", "");
                        }

                        // Create User object from Facebook data
                        User facebookUser = User.fromFacebookLogin(
                                facebookId, name, email, profileImageUrl);

                        // Preserve current user's study data
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

                        // Save to repository
                        currentUser = facebookUser;
                        userRepository.saveUser(currentUser);

                        // Update UI
                        updateUI();
                        Toast.makeText(requireContext(),
                                "Welcome " + name + "!", Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Facebook user data saved successfully");

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Facebook user data", e);
                        Toast.makeText(requireContext(),
                                "Error getting user data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set parameters to request specific fields
        // Note: "email" requires Advanced Access. Using only public_profile fields
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

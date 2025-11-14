package com.example.timerstudy.view.fragments;

import android.os.Bundle;
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


public class ProfileFragment extends Fragment {
    
    private ImageView ivAvatar;
    private TextView tvUserName;
    private LinearLayout btnLoginFacebook;
    private SwitchCompat switchVibrator;
    private LinearLayout btnNotifications;
    private LinearLayout btnPrivacy;
    private LinearLayout btnLogout;
    
    private UserRepository userRepository;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
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
        btnLoginFacebook.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Facebook Login (Coming soon)", Toast.LENGTH_SHORT).show();
        });
        
        btnLogout.setOnClickListener(v -> {
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
}

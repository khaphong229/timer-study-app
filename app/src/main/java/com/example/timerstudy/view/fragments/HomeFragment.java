package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.timerstudy.R;

public class HomeFragment extends Fragment {
    
    private TextView tvUserName;
    private TextView tvUserAge;
    private TextView tvUserEmail;
    private TextView tvUserBio;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        loadUserData();
    }
    
    private void initializeViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserAge = view.findViewById(R.id.tvUserAge);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        
        view.findViewById(R.id.btnRefresh).setOnClickListener(v -> loadUserData());
    }
    
    private void loadUserData() {
        // Load default data for now
        tvUserName.setText("Timer Study User");
        tvUserAge.setText("Age: 25");
        tvUserEmail.setText("user@timerstudy.com");
        tvUserBio.setText("Welcome to Timer Study App! This is your home page where you can see your profile information.");
    }
}

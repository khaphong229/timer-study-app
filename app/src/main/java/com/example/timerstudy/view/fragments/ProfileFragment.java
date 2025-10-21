package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.timerstudy.R;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {
    
    private TextInputEditText etName;
    private TextInputEditText etAge;
    private TextInputEditText etEmail;
    private TextInputEditText etBio;
    private TextView tvCurrentInfo;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        loadCurrentInfo();
    }
    
    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        etEmail = view.findViewById(R.id.etEmail);
        etBio = view.findViewById(R.id.etBio);
        tvCurrentInfo = view.findViewById(R.id.tvCurrentInfo);
        
        view.findViewById(R.id.btnUpdate).setOnClickListener(v -> updateProfile());
    }
    
    private void loadCurrentInfo() {
        // Load default data
        etName.setText("Timer Study User");
        etAge.setText("25");
        etEmail.setText("user@timerstudy.com");
        etBio.setText("I love using the Timer Study app to improve my productivity!");
        
        updateCurrentInfoDisplay();
    }
    
    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        
        if (name.isEmpty() || age.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateCurrentInfoDisplay();
        Toast.makeText(requireContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
    }
    
    private void updateCurrentInfoDisplay() {
        String info = "Tên: " + etName.getText().toString() + "\n" +
                     "Tuổi: " + etAge.getText().toString() + "\n" +
                     "Email: " + etEmail.getText().toString() + "\n" +
                     "Giới thiệu: " + etBio.getText().toString();
        tvCurrentInfo.setText(info);
    }
}

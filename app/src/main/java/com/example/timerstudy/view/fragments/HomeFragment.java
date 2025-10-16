package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.timerstudy.databinding.FragmentHomeBinding;
import com.example.timerstudy.model.User;
import com.example.timerstudy.viewmodel.MainViewModel;

/**
 * HomeFragment - Màn hình chính hiển thị thông tin user
 * Observe LiveData từ ViewModel và cập nhật UI
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout bằng ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        // ViewModelProvider đảm bảo ViewModel survive configuration changes
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Setup observers và listeners
        setupObservers();
        setupClickListeners();
    }

    /**
     * Setup LiveData observers
     * Quan sát các thay đổi từ ViewModel và cập nhật UI
     */
    private void setupObservers() {
        // Observe User data
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            }
        });

        // Observe loading state
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

                // Disable button khi đang loading
                binding.btnRefresh.setEnabled(!isLoading);

                // Optional: Hiển thị skeleton loading cho Card
                binding.cardUserInfo.setAlpha(isLoading ? 0.5f : 1.0f);
                binding.cardBio.setAlpha(isLoading ? 0.5f : 1.0f);
            }
        });

        // Observe error messages
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup click listeners cho các UI elements
     */
    private void setupClickListeners() {
        // Button refresh
        binding.btnRefresh.setOnClickListener(v -> {
            viewModel.refreshUserData();
            Toast.makeText(requireContext(), "Đang làm mới dữ liệu...", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Cập nhật UI với dữ liệu User
     * @param user User object từ ViewModel
     */
    private void updateUI(User user) {
        binding.tvUserName.setText(user.getName());
        binding.tvUserAge.setText("Tuổi: " + user.getAge());
        binding.tvUserEmail.setText(user.getEmail());
        binding.tvUserBio.setText(user.getBio());
    }

    /**
     * Clean up binding khi Fragment bị destroy
     * Tránh memory leaks
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

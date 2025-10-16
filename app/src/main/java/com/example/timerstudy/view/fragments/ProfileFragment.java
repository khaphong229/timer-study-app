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
import com.example.timerstudy.databinding.FragmentProfileBinding;
import com.example.timerstudy.model.User;
import com.example.timerstudy.viewmodel.MainViewModel;

/**
 * ProfileFragment - Màn hình chỉnh sửa thông tin user
 * Sử dụng Material TextInputLayout để nhập liệu
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private MainViewModel viewModel;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel - share với Activity và HomeFragment
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    /**
     * Setup observers để nhận data từ ViewModel
     */
    private void setupObservers() {
        // Observe User data và fill vào form
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                fillFormWithUserData(user);
                updateCurrentInfoDisplay(user);
            }
        });

        // Observe loading state
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.btnUpdate.setEnabled(!isLoading);

                // Disable inputs khi đang loading
                setInputsEnabled(!isLoading);
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
     * Setup click listeners
     */
    private void setupClickListeners() {
        binding.btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                updateUserInfo();
            }
        });
    }

    /**
     * Fill form với dữ liệu user hiện tại
     */
    private void fillFormWithUserData(User user) {
        binding.etName.setText(user.getName());
        binding.etAge.setText(String.valueOf(user.getAge()));
        binding.etEmail.setText(user.getEmail());
        binding.etBio.setText(user.getBio());
    }

    /**
     * Cập nhật hiển thị thông tin hiện tại
     */
    private void updateCurrentInfoDisplay(User user) {
        String info = "📛 Tên: " + user.getName() + "\n" +
                "🎂 Tuổi: " + user.getAge() + "\n" +
                "📧 Email: " + user.getEmail() + "\n" +
                "📝 Bio: " + user.getBio();
        binding.tvCurrentInfo.setText(info);
    }

    /**
     * Validate form inputs
     * @return true nếu tất cả inputs hợp lệ
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Name
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilName.setError("Vui lòng nhập tên");
            isValid = false;
        } else {
            binding.tilName.setError(null);
        }

        // Validate Age
        String ageStr = binding.etAge.getText().toString().trim();
        if (ageStr.isEmpty()) {
            binding.tilAge.setError("Vui lòng nhập tuổi");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age > 150) {
                    binding.tilAge.setError("Tuổi không hợp lệ (1-150)");
                    isValid = false;
                } else {
                    binding.tilAge.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.tilAge.setError("Tuổi phải là số");
                isValid = false;
            }
        }

        // Validate Email
        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.tilEmail.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validate Bio
        String bio = binding.etBio.getText().toString().trim();
        if (bio.isEmpty()) {
            binding.tilBio.setError("Vui lòng nhập giới thiệu");
            isValid = false;
        } else {
            binding.tilBio.setError(null);
        }

        return isValid;
    }

    /**
     * Gọi ViewModel để cập nhật user info
     */
    private void updateUserInfo() {
        String name = binding.etName.getText().toString().trim();
        int age = Integer.parseInt(binding.etAge.getText().toString().trim());
        String email = binding.etEmail.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();

        viewModel.updateUserInfo(name, age, email, bio);

        Toast.makeText(requireContext(),
                "Đang cập nhật thông tin...",
                Toast.LENGTH_SHORT).show();

        // Hide keyboard after update
        hideKeyboard();
    }

    /**
     * Enable/Disable tất cả input fields
     */
    private void setInputsEnabled(boolean enabled) {
        binding.etName.setEnabled(enabled);
        binding.etAge.setEnabled(enabled);
        binding.etEmail.setEnabled(enabled);
        binding.etBio.setEnabled(enabled);
    }

    /**
     * Hide soft keyboard
     */
    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

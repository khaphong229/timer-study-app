package com.example.timerstudy.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.timerstudy.model.User;
import com.example.timerstudy.repository.UserRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel class theo MVVM pattern
 * Quản lý UI-related data và business logic
 * Survive configuration changes (screen rotation)
 */
public class MainViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<User> userLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final ExecutorService executorService;

    public MainViewModel() {
        repository = UserRepository.getInstance();
        userLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();

        // Load dữ liệu ban đầu
        loadUserData();
    }

    /**
     * Expose LiveData cho View (Fragment/Activity)
     * Chỉ expose immutable LiveData để View không thể modify trực tiếp
     */
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Load user data từ Repository
     * Chạy trên background thread để không block UI
     */
    public void loadUserData() {
        loadingLiveData.postValue(true);

        executorService.execute(() -> {
            try {
                User user = repository.getCurrentUser();
                userLiveData.postValue(user);
                loadingLiveData.postValue(false);
            } catch (Exception e) {
                errorLiveData.postValue("Không thể tải dữ liệu: " + e.getMessage());
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Cập nhật thông tin user
     * @param name Tên mới
     * @param age Tuổi mới
     */
    public void updateUserInfo(String name, int age, String email, String bio) {
        loadingLiveData.postValue(true);

        executorService.execute(() -> {
            try {
                User updatedUser = new User(name, age, email, bio);
                repository.updateUser(updatedUser);
                userLiveData.postValue(updatedUser);
                loadingLiveData.postValue(false);
            } catch (Exception e) {
                errorLiveData.postValue("Không thể cập nhật: " + e.getMessage());
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Refresh dữ liệu (pull to refresh)
     */
    public void refreshUserData() {
        loadUserData();
    }

    /**
     * Clean up resources khi ViewModel bị destroy
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}

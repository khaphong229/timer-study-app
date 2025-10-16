package com.example.timerstudy.repository;

import com.example.timerstudy.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class để quản lý data source
 * Trong project thực tế, đây là nơi gọi API, database, SharedPreferences, v.v.
 * Hiện tại đang mock data từ local
 */
public class UserRepository {
    private static UserRepository instance;
    private User currentUser;

    // Singleton pattern
    private UserRepository() {
        // Khởi tạo mock data
        initializeMockData();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Khởi tạo dữ liệu mẫu
     * Trong thực tế, data sẽ được lấy từ API hoặc database
     */
    private void initializeMockData() {
        currentUser = new User(
                "Nguyễn Văn An",
                28,
                "nguyenvanan@example.com",
                "Android Developer with 5+ years of experience. " +
                        "Passionate about clean code and modern architecture patterns."
        );
    }

    /**
     * Lấy thông tin user hiện tại
     * Simulate network delay bằng Thread.sleep()
     */
    public User getCurrentUser() {
        // Simulate network delay (500ms)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return currentUser;
    }

    /**
     * Cập nhật thông tin user
     * @param user User object với thông tin mới
     */
    public void updateUser(User user) {
        // Simulate network delay
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.currentUser = user;
    }

    /**
     * Lấy danh sách users mẫu
     * Dùng cho RecyclerView trong tương lai
     */
    public List<User> getUserList() {
        List<User> users = new ArrayList<>();
        users.add(new User("Nguyễn Văn An", 28, "an@example.com", "Android Developer"));
        users.add(new User("Trần Thị Bình", 25, "binh@example.com", "UI/UX Designer"));
        users.add(new User("Lê Văn Cường", 30, "cuong@example.com", "Backend Developer"));
        users.add(new User("Phạm Thị Dung", 27, "dung@example.com", "Product Manager"));
        return users;
    }
}

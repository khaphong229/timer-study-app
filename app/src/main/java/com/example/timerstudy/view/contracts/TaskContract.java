package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.Date;
import java.util.List;

/**
 * Contract interface for Task MVP pattern
 * Định nghĩa các phương thức giao tiếp giữa View và Presenter
 */
public interface TaskContract {

    /**
     * Các phương thức View cung cấp để Presenter gọi
     */
    interface View {
        // Hiển thị danh sách task
        void showTasks(List<TaskEntity> tasks);
        // Hiển thị trạng thái trống
        void showEmptyState();
        void hideEmptyState();
        // Hiển thị/ẩn loading
        void showLoading();
        void hideLoading();
        // Hiển thị lỗi
        void showError(String message);

        // Thông báo thành công
        void showTaskAddedSuccess();
        // Cập nhật danh sách đã lọc
        void updateFilteredTasks(List<TaskEntity> filteredTasks);
        // Cập nhật số lượng task
        void updateTaskCount(int completed, int total);
    }

    /**
     * Các phương thức Presenter cung cấp để View gọi
     */
    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadTasks();
        void addTask(String title, String priority, Date selectedDate);
        void updateTask(TaskEntity task);
        void deleteTask(TaskEntity task);
        void toggleTaskCompletion(TaskEntity task, boolean isCompleted);
        void setFilter(String priorityFilter, boolean showCompleted);
        void applyFilter();
        void setSelectedDate(Date date);
    }
}
package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.Date;
import java.util.List;

/**
 * Contract interface for Task MVP pattern
 * ĐỊnh nghĩa các phương thức gaio tiếp giauwx View và Presenter
 */
public interface TaskContract {

    // view có những phương thức nào mà presenter có thể gọi
    interface View {
        void showTasks(List<TaskEntity> tasks);
        void showEmptyState();
        void hideEmptyState();
        void showLoading();
        void hideLoading();
        void showError(String message);
        void clearTaskInput();
        void resetPrioritySelection();
        void showTaskAddedSuccess();
        void updateFilteredTasks(List<TaskEntity> filteredTasks);
    }

    // presenter có những phương thức nào mà view có thể gọi
    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadTasks();
        void addTask(String title, String priority, Date selectedDate);
        void updateTask(TaskEntity task);
        void deleteTask(TaskEntity task);
        void toggleTaskCompletion(TaskEntity task, boolean isCompleted);
        void refreshTasks();
        void setFilter(String priorityFilter, boolean showCompleted);
        void applyFilter();
        void setSelectedDate(Date date);
    }
}
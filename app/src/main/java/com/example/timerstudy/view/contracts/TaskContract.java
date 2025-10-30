package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.List;

/**
 * Contract interface for Task MVP pattern
 * Defines the communication between View and Presenter
 */
public interface TaskContract {

    /**
     * View interface - what the presenter can call on the view
     */
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
    }

    /**
     * Presenter interface - what the view can call on the presenter
     */
    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadTasks();
        void addTask(String title, String priority);
        void updateTask(TaskEntity task);
        void deleteTask(TaskEntity task);
        void toggleTaskCompletion(TaskEntity task, boolean isCompleted);
        void refreshTasks();
    }
}
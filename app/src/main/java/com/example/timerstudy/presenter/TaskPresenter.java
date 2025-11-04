package com.example.timerstudy.presenter;

import android.content.Context;
import android.util.Log;

import com.example.timerstudy.view.contracts.TaskContract;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.repository.TaskRepository;
import com.example.timerstudy.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TaskPresenter - Handles business logic for Task operations
 * Implements MVP pattern by managing communication between View and Model
 */
public class TaskPresenter implements TaskContract.Presenter {

    private static final String TAG = "TaskPresenter";

    private TaskContract.View view;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private ExecutorService executor;
    private Date selectedDate;
    private String currentPriorityFilter = "all";
    private boolean showCompletedTasks = false; // Mặc định hiển thị task chưa hoàn thành
    private List<TaskEntity> allTasks;

    public TaskPresenter(TaskContract.View view, Context context) {
        this.view = view;
        this.taskRepository = new TaskRepository(context);
        this.userRepository = new UserRepository(context);
        this.executor = Executors.newFixedThreadPool(2);

        // Initialize user if needed
        initializeUser();
    }

    @Override
    public void attachView(TaskContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;

        loadTasks();
    }

    @Override
    public void loadTasks() {
        boolean isInitialLoad = view != null;
        if (isInitialLoad) {
            view.showLoading();
        }

        executor.execute(() -> {
            try {
                int userId = userRepository.getCurrentUserId();
                Date date = selectedDate;
                if (date == null)
                    date = new Date();
                long dayMillis = normalizeDate(date).getTime();
                allTasks = taskRepository.getTasksByUserAndDate(userId, dayMillis);

                // Tạo bản sao để tránh null pointer
                if (allTasks == null) {
                    allTasks = new ArrayList<>();
                }

                runOnMainThread(() -> {
                    if (view != null) {
                        if (isInitialLoad) {
                            view.hideLoading();
                        }
                        // Cập nhật task count
                        updateTaskCount();
                        // Apply filter sẽ xử lý việc hiển thị
                        applyFilter();
                    }
                });
            } catch (Exception e) {
                runOnMainThread(() -> {
                    if (view != null) {
                        if (isInitialLoad) {
                            view.hideLoading();
                        }
                        view.showError(e.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public void applyFilter() {
        if (allTasks == null || allTasks.isEmpty()) {
            if (view != null) {
                view.showEmptyState();
                view.updateTaskCount(0, 0);
            }
            return;
        }

        List<TaskEntity> filteredTasks = new ArrayList<>();
        for (TaskEntity task : allTasks) {
            boolean priorityMatch = currentPriorityFilter.equals("all") ||
                    task.getPriority().toLowerCase().equals(currentPriorityFilter.toLowerCase());
            boolean completedMatch = task.isCompleted() == showCompletedTasks;

            if (priorityMatch && completedMatch) {
                filteredTasks.add(task);
            }
        }

        if (view != null) {
            if (filteredTasks.isEmpty()) {
                view.showEmptyState();
            } else {
                view.hideEmptyState();
            }
            view.updateFilteredTasks(filteredTasks);
            // Cập nhật task count sau khi lọc
            updateTaskCount();
        }
    }

    private void updateTaskCount() {
        if (view != null && allTasks != null) {
            int completed = 0;
            int total = allTasks.size();

            for (TaskEntity task : allTasks) {
                if (task.isCompleted()) {
                    completed++;
                }
            }

            view.updateTaskCount(completed, total);
        }
    }

    @Override
    public void toggleTaskCompletion(TaskEntity task, boolean isChecked) {
        executor.execute(() -> {
            try {
                task.setCompleted(isChecked);
                taskRepository.updateTask(task);

                runOnMainThread(() -> {
                    // Cập nhật task count sau khi thay đổi trạng thái
                    updateTaskCount();
                    applyFilter();
                });
            } catch (Exception e) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showError(e.getMessage());
                    }
                });
            }
        });
    }

    private Date normalizeDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public void addTask(String title, String priority, Date selectedDate) {
        if (title == null || title.trim().isEmpty()) {
            if (view != null) {
                view.showError("Vui lòng nhập nội dung task");
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        executor.execute(() -> {
            try {
                // Create new task
                TaskEntity newTask = new TaskEntity();
                newTask.setTitle(title.trim());
                newTask.setPriority(priority);
                // Normalize the task date to avoid time issues
                Date normalizedDate = normalizeDate(selectedDate != null ? selectedDate : new Date());
                newTask.setTaskDate(normalizedDate);
                newTask.setCreatedAt(new Date());
                newTask.setUpdatedAt(new Date());
                newTask.setCompleted(false);
                newTask.setTotalTimeSpent(0);
                newTask.setEstimatedSessions(1);
                newTask.setActualSessions(0);
                newTask.setOrderIndex(0);

                // Set user ID
                int userId = userRepository.getCurrentUserId();
                newTask.setUserId(userId);

                // Insert task (synchronous since we're already in executor)
                taskRepository.insertTask(newTask);

                // Update UI on main thread
                if (view != null) {
                    runOnMainThread(() -> {
                        view.hideLoading();
                        view.clearTaskInput();
                        view.resetPrioritySelection();
                        view.showTaskAddedSuccess();
                        // Reload tasks from database
                        loadTasks();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error adding task", e);
                if (view != null) {
                    runOnMainThread(() -> {
                        view.hideLoading();
                        view.showError("Không thể thêm task: " + e.getMessage());
                    });
                }
            }
        });
    }

    @Override
    public void updateTask(TaskEntity task) {
        if (task == null)
            return;

        executor.execute(() -> {
            try {
                task.setUpdatedAt(new Date());
                taskRepository.updateTask(task);

                // Refresh task list on main thread
                if (view != null) {
                    runOnMainThread(() -> loadTasks());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error updating task", e);
                if (view != null) {
                    runOnMainThread(() -> view.showError("Không thể cập nhật task: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public void deleteTask(TaskEntity task) {
        if (task == null)
            return;

        executor.execute(() -> {
            try {
                taskRepository.deleteTask(task);

                // Refresh task list on main thread
                if (view != null) {
                    runOnMainThread(() -> loadTasks());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error deleting task", e);
                if (view != null) {
                    runOnMainThread(() -> view.showError("Không thể xóa task: " + e.getMessage()));
                }
            }
        });
    }


    @Override
    public void refreshTasks() {
        loadTasks();
    }

    /**
     * Clean up resources when presenter is destroyed
     */
    public void onDestroy() {
        detachView();
    }

    /**
     * Initialize user if needed
     */
    private void initializeUser() {
        executor.execute(() -> {
            try {
                userRepository.initializeUser();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing user", e);
            }
        });
    }

    @Override
    public void setFilter(String priorityFilter, boolean showCompleted) {
        this.currentPriorityFilter = priorityFilter;
        this.showCompletedTasks = showCompleted;
        applyFilter();
    }

   
    /**
     * Helper method to run code on main thread
     */
    private void runOnMainThread(Runnable runnable) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(runnable);
    }
}
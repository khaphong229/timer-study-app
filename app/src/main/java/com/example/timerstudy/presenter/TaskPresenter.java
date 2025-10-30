package com.example.timerstudy.presenter;

import android.content.Context;
import android.util.Log;

import com.example.timerstudy.view.contracts.TaskContract;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.repository.TaskRepository;
import com.example.timerstudy.data.repository.UserRepository;

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
    
    @Override
    public void loadTasks() {
        // Don't show loading for refresh operations
        boolean isInitialLoad = view != null;
        if (isInitialLoad) {
            view.showLoading();
        }
        
        executor.execute(() -> {
            try {
                int userId = userRepository.getCurrentUserId();
                List<TaskEntity> tasks = taskRepository.getTasksByUserId(userId);
                
                // Update UI on main thread
                runOnMainThread(() -> {
                    if (view != null) {
                        if (isInitialLoad) {
                            view.hideLoading();
                        }
                        
                        if (tasks == null || tasks.isEmpty()) {
                            view.showEmptyState();
                        } else {
                            view.hideEmptyState();
                            view.showTasks(tasks);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks", e);
                runOnMainThread(() -> {
                    if (view != null) {
                        if (isInitialLoad) {
                            view.hideLoading();
                        }
                        view.showError("Không thể tải danh sách task: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    @Override
    public void addTask(String title, String priority) {
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
                newTask.setTaskDate(new Date());
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
                        // Refresh task list after UI updates
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
        if (task == null) return;
        
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
                    runOnMainThread(() -> 
                        view.showError("Không thể cập nhật task: " + e.getMessage())
                    );
                }
            }
        });
    }
    
    @Override
    public void deleteTask(TaskEntity task) {
        if (task == null) return;
        
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
                    runOnMainThread(() -> 
                        view.showError("Không thể xóa task: " + e.getMessage())
                    );
                }
            }
        });
    }
    
    @Override
    public void toggleTaskCompletion(TaskEntity task, boolean isCompleted) {
        if (task == null) return;
        
        executor.execute(() -> {
            try {
                task.setCompleted(isCompleted);
                if (isCompleted) {
                    task.setCompletedAt(new Date());
                } else {
                    task.setCompletedAt(null);
                }
                task.setUpdatedAt(new Date());
                
                taskRepository.updateTask(task);
                
                // Refresh task list to show updated status
                if (view != null) {
                    runOnMainThread(() -> loadTasks());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error toggling task completion", e);
                if (view != null) {
                    runOnMainThread(() -> 
                        view.showError("Không thể cập nhật trạng thái task: " + e.getMessage())
                    );
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
    
    /**
     * Helper method to run code on main thread
     */
    private void runOnMainThread(Runnable runnable) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(runnable);
    }
}
package com.example.timerstudy.presenter;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.example.timerstudy.view.contracts.TaskContract;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.repository.TaskRepository;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.data.callback.DataCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * TaskPresenter - xử lí logic nghiệp vụ cho các thao tác Task
 */
public class TaskPresenter implements TaskContract.Presenter {
    private static final String TAG = "TaskPresenter";

    private TaskContract.View view;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private Date selectedDate;
    private String currentPriorityFilter = "all";
    private boolean showCompletedTasks = false;
    private List<TaskEntity> allTasks = new ArrayList<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Khởi tạo & Lifecycle
    public TaskPresenter(TaskContract.View view, Context context) {
        this.view = view;
        this.taskRepository = new TaskRepository(context);
        this.userRepository = new UserRepository(context);
        initializeUser();
    }

    @Override
    public void attachView(TaskContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    public void onDestroy() {
        detachView();
    }

    // Xử lý ngày & filter
    @Override
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        loadTasks();
    }

    @Override
    public void setFilter(String priorityFilter, boolean showCompleted) {
        this.currentPriorityFilter = priorityFilter;
        this.showCompletedTasks = showCompleted;
        applyFilter();
    }

    // Xử lý Task
    @Override
    public void loadTasks() {
        if (view != null) view.showLoading();

        long userId = userRepository.getCurrentUserId();
        Date date = selectedDate != null ? selectedDate : new Date();
        long dayMillis = normalizeDate(date).getTime();

        // Sử dụng truy vấn theo ngày
        taskRepository.getTasksByUserIdAndDate(userId, dayMillis, new DataCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        allTasks = tasks != null ? tasks : new ArrayList<>();
                        updateTaskCount();
                        applyFilter();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showError(errorMessage);
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
                    task.getPriority().equalsIgnoreCase(currentPriorityFilter);
            boolean completedMatch = task.isCompleted() == showCompletedTasks;
            if (priorityMatch && completedMatch) filteredTasks.add(task);
        }

        if (view != null) {
            if (filteredTasks.isEmpty()) view.showEmptyState();
            else view.hideEmptyState();
            view.updateFilteredTasks(filteredTasks);
            updateTaskCount();
        }
    }
 
    private void updateTaskCount() {
        if (view != null && allTasks != null) {
            int completed = 0, total = allTasks.size();
            for (TaskEntity task : allTasks) if (task.isCompleted()) completed++;
            view.updateTaskCount(completed, total);
        }
    }

    @Override
    public void toggleTaskCompletion(TaskEntity task, boolean isChecked) {
        if (task == null) return;
        task.setCompleted(isChecked);
        taskRepository.updateTask(task, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    updateTaskCount();
                    applyFilter();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null) view.showError(errorMessage);
                });
            }
        });
    }

    @Override
    public void addTask(String title, String priority, Date selectedDate) {
        if (title == null || title.trim().isEmpty()) {
            if (view != null) view.showError("Please enter task content");
            return;
        }
        if (view != null) view.showLoading();

        TaskEntity newTask = new TaskEntity();
        newTask.setTitle(title.trim());
        newTask.setPriority(priority);
        Date normalizedDate = normalizeDate(selectedDate != null ? selectedDate : new Date());
        newTask.setTaskDate(normalizedDate);
        newTask.setCreatedAt(new Date());
        newTask.setUpdatedAt(new Date());
        newTask.setCompleted(false);
        newTask.setTotalTimeSpent(0);
        newTask.setEstimatedSessions(1);
        newTask.setActualSessions(0);
        newTask.setOrderIndex(0);
        newTask.setUserId(userRepository.getCurrentUserId());

        taskRepository.insertTask(newTask, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showTaskAddedSuccess();
                        loadTasks();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showError("Cannot add task: " + errorMessage);
                    }
                });
            }
        });
    }

    @Override
    public void updateTask(TaskEntity task) {
        if (task == null) return;
        task.setUpdatedAt(new Date());
        taskRepository.updateTask(task, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(TaskPresenter.this::loadTasks);
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null)
                        view.showError("Cannot update task: " + errorMessage);
                });
            }
        });
    }

    @Override
    public void deleteTask(TaskEntity task) {
        if (task == null) return;
        taskRepository.deleteTask(task, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(TaskPresenter.this::loadTasks);
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null)
                        view.showError("Cannot delete task: " + errorMessage);
                });
            }
        });
    }

    // Tiện ích
    private Date normalizeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void initializeUser() {
        try {
            userRepository.initializeUser();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing user", e);
        }
    }
}

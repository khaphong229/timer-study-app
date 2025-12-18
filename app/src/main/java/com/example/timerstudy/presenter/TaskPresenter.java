package com.example.timerstudy.presenter;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.example.timerstudy.utils.UserManager;
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
    private Context context;

    // Khởi tạo & Lifecycle
    public TaskPresenter(TaskContract.View view, Context context) {
        this.view = view;
        this.taskRepository = new TaskRepository(context);
        this.userRepository = new UserRepository(context);
        this.context = context;
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

        // Ưu tiên gọi API; nếu lỗi thì fallback về local DB theo ngày
        String token = null;
        try {
            token = userRepository.getCurrentUser().getAccessToken();
        } catch (Exception ignored) {}

        if (token != null && !token.isEmpty()) {
            String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
            Log.d(TAG, "Loading tasks via API; token present: yes");
            taskRepository.fetchAllTasksFromApi(bearer, new DataCallback<List<TaskEntity>>() {
                @Override
                public void onSuccess(List<TaskEntity> tasks) {
                    mainHandler.post(() -> {
                        if (view != null) {
                            view.hideLoading();
                            // Lọc tasks theo ngày đã chọn
                            allTasks = filterTasksBySelectedDate(tasks);
                            Log.d(TAG, "API load success; total items: " + (tasks != null ? tasks.size() : 0) + ", filtered by date: " + allTasks.size());
                            updateTaskCount();
                            applyFilter();
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    // Fallback to local
                    Log.e(TAG, "API load failed: " + errorMessage + "; falling back to local");
                    loadTasksFromLocalWithDate(errorMessage);
                }
            });
        } else {
            // No token, use local
            Log.d(TAG, "No access token; loading tasks from local DB");
            loadTasksFromLocalWithDate(null);
        }
    }

    private List<TaskEntity> filterTasksBySelectedDate(List<TaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty() || selectedDate == null) {
            return new ArrayList<>();
        }

        List<TaskEntity> filtered = new ArrayList<>();
        long selectedDayMillis = normalizeDate(selectedDate).getTime();

        for (TaskEntity task : tasks) {
            if (task.getTaskDate() != null) {
                long taskDayMillis = normalizeDate(task.getTaskDate()).getTime();
                if (taskDayMillis == selectedDayMillis) {
                    filtered.add(task);
                }
            }
        }

        return filtered;
    }

    private void loadTasksFromLocalWithDate(String apiError) {
        long userId = UserManager.getInstance(context).getCurrentUserId();
        Date date = selectedDate != null ? selectedDate : new Date();
        long dayMillis = normalizeDate(date).getTime();

        taskRepository.getTasksByUserIdAndDate(userId, dayMillis, new DataCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        allTasks = tasks != null ? tasks : new ArrayList<>();
                        Log.d(TAG, "Local DB load; items: " + allTasks.size());
                        updateTaskCount();
                        applyFilter();
                        if (apiError != null && !apiError.isEmpty()) {
                            view.showError("Using local data. " + apiError);
                        }
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (view != null) {
                        view.hideLoading();
                        String msg = (apiError != null ? apiError + "; " : "") + errorMessage;
                        view.showError(msg);
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

            // Nếu showCompletedTasks = true: hiển thị tất cả (cả completed và chưa completed)
            // Nếu showCompletedTasks = false: chỉ hiển thị task chưa hoàn thành
            boolean completedMatch = showCompletedTasks || !task.isCompleted();

            if (priorityMatch && completedMatch) filteredTasks.add(task);
        }
        
        // Sắp xếp theo order_index ASC (số nhỏ hơn lên đầu)
        filteredTasks.sort((t1, t2) -> Integer.compare(t1.getOrderIndex(), t2.getOrderIndex()));

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
        
        // Lưu trạng thái cũ để revert nếu API fail
        boolean oldCompletedState = task.isCompleted();
        Date oldCompletedAt = task.getCompletedAt();
        
        // Cập nhật trạng thái mới
        task.setCompleted(isChecked);
        
        // Nếu đánh dấu hoàn thành, set completed_at = now
        // Nếu bỏ đánh dấu, set completed_at = null
        if (isChecked) {
            task.setCompletedAt(new Date());
            Log.d(TAG, "Marking task as COMPLETED: " + task.getTitle() + " (ID: " + task.getTaskId() + ")");
        } else {
            task.setCompletedAt(null);
            Log.d(TAG, "Marking task as PENDING: " + task.getTitle() + " (ID: " + task.getTaskId() + ")");
        }
        
        String token = null;
        try { token = userRepository.getCurrentUser().getAccessToken(); } catch (Exception ignored) {}
        if (token == null || token.isEmpty()) { 
            if (view != null) view.showError("Missing access token"); 
            return; 
        }
        
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        taskRepository.updateTaskViaApi(task, bearer, new DataCallback<TaskEntity>() {
            @Override
            public void onSuccess(TaskEntity result) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Task completion status updated successfully");
                    if (view != null) view.showTaskUpdatedSuccess();
                    updateTaskCount();
                    applyFilter();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> { 
                    // Revert lại trạng thái cũ nếu API fail
                    task.setCompleted(oldCompletedState);
                    task.setCompletedAt(oldCompletedAt);
                    
                    Log.e(TAG, "Failed to update task completion: " + errorMessage);
                    if (view != null) {
                        view.showError("Failed to update task: " + errorMessage);
                        // Cập nhật lại UI với trạng thái cũ
                        applyFilter();
                    }
                });
            }
        });
    }

    @Override
    public void addTask(String title, String priority, Date selectedDate) {
        addTask(title, priority, selectedDate, 0);
    }
    
    @Override
    public void addTask(String title, String priority, Date selectedDate, int orderIndex) {
        if (title == null || title.trim().isEmpty()) {
            if (view != null) view.showError("Please enter task content");
            return;
        }
        if (view != null) view.showLoading();

        TaskEntity newTask = new TaskEntity();
        newTask.setTitle(title.trim());
        newTask.setPriority(priority);
        // Sử dụng ngày đã chọn trên màn hình (this.selectedDate)
        Date taskDate = this.selectedDate != null ? this.selectedDate : new Date();
        Date normalizedDate = normalizeDate(taskDate);
        newTask.setTaskDate(normalizedDate);
        
        Log.d(TAG, "Adding task for date: " + normalizedDate + " with order index: " + orderIndex);
        newTask.setCreatedAt(new Date());
        newTask.setUpdatedAt(new Date());
        newTask.setCompleted(false);
        newTask.setTotalTimeSpent(0);
        newTask.setEstimatedSessions(1);
        newTask.setActualSessions(0);
        newTask.setOrderIndex(orderIndex);
        newTask.setUserId(userRepository.getCurrentUserId());

        String token = null;
        try {
            token = userRepository.getCurrentUser().getAccessToken();
        } catch (Exception ignored) {}

        if (token == null || token.isEmpty()) { if (view != null) { view.hideLoading(); view.showError("Missing access token"); } return; }
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        taskRepository.createTaskViaApi(newTask, bearer, new DataCallback<TaskEntity>() {
            @Override
            public void onSuccess(TaskEntity result) {
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
        // Giữ nguyên taskDate của task, không thay đổi
        Log.d(TAG, "Updating task ID: " + task.getTaskId() + ", Task Date: " + task.getTaskDate());

        String token = null; try { token = userRepository.getCurrentUser().getAccessToken(); } catch (Exception ignored) {}
        if (token == null || token.isEmpty()) { if (view != null) view.showError("Missing access token"); return; }
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        taskRepository.updateTaskViaApi(task, bearer, new DataCallback<TaskEntity>() {
            @Override
            public void onSuccess(TaskEntity result) {
                mainHandler.post(() -> {
                    if (view != null) view.showTaskUpdatedSuccess();
                    TaskPresenter.this.loadTasks();
                });
            }
            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> { if (view != null) view.showError("Cannot update task: " + errorMessage); });
            }
        });
    }

    @Override
    public void deleteTask(TaskEntity task) {
        if (task == null) return;
        String token = null; try { token = userRepository.getCurrentUser().getAccessToken(); } catch (Exception ignored) {}
        if (token == null || token.isEmpty()) { if (view != null) view.showError("Missing access token"); return; }
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        taskRepository.deleteTaskViaApi(task.getTaskId(), bearer, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    if (view != null) view.showTaskDeletedSuccess();
                    TaskPresenter.this.loadTasks();
                });
            }
            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> { if (view != null) view.showError("Cannot delete task: " + errorMessage); });
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
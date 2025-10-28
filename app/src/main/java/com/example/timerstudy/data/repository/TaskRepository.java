package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Task data operations
 * Wraps {@link TaskDao} with background execution and LiveData exposure.
 */
public class TaskRepository {

    private static final String TAG = "TaskRepository";

    // Database and DAO
    private final AppDatabase database;
    private final TaskDao taskDao;

    // Thread executor for background operations
    private final ExecutorService executorService;

    // LiveData for reactive updates
    private final MutableLiveData<List<TaskEntity>> allTasksLiveData;
    private final MutableLiveData<List<TaskEntity>> userTasksLiveData;
    private final MutableLiveData<TaskEntity> currentTaskLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    // Singleton instance
    private static volatile TaskRepository INSTANCE;

    private TaskRepository(Context context) {
        database = AppDatabase.getDatabase(context);
        taskDao = database.taskDao();
        executorService = Executors.newFixedThreadPool(4);

        allTasksLiveData = new MutableLiveData<>();
        userTasksLiveData = new MutableLiveData<>();
        currentTaskLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public static TaskRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TaskRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // LiveData getters
    public LiveData<List<TaskEntity>> getAllTasksLiveData() { return allTasksLiveData; }
    public LiveData<List<TaskEntity>> getUserTasksLiveData() { return userTasksLiveData; }
    public LiveData<TaskEntity> getCurrentTaskLiveData() { return currentTaskLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    // ==================== Operations ====================

    public void loadAllTasks() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<TaskEntity> tasks = taskDao.getAllTasks();
                allTasksLiveData.postValue(tasks);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all tasks", e);
                errorLiveData.postValue("Failed to load tasks: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void loadTasksByUserId(int userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<TaskEntity> tasks = taskDao.getTasksByUserId(userId);
                userTasksLiveData.postValue(tasks);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks by userId=" + userId, e);
                errorLiveData.postValue("Failed to load user tasks: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void getTaskById(int taskId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                TaskEntity task = taskDao.getTaskById(taskId);
                currentTaskLiveData.postValue(task);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting task by ID: " + taskId, e);
                errorLiveData.postValue("Failed to get task: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void createTask(TaskEntity task) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long id = taskDao.insertTask(task);
                task.setTaskId((int) id);
                currentTaskLiveData.postValue(task);
                loadTasksByUserId(task.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating task", e);
                errorLiveData.postValue("Failed to create task: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateTask(TaskEntity task) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                taskDao.updateTask(task);
                currentTaskLiveData.postValue(task);
                loadTasksByUserId(task.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating task", e);
                errorLiveData.postValue("Failed to update task: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void deleteTask(TaskEntity task) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                taskDao.deleteTask(task);
                loadTasksByUserId(task.getUserId());
                if (currentTaskLiveData.getValue() != null && currentTaskLiveData.getValue().getTaskId() == task.getTaskId()) {
                    currentTaskLiveData.postValue(null);
                }
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task", e);
                errorLiveData.postValue("Failed to delete task: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void clearAllData() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                taskDao.deleteAllTasks();
                allTasksLiveData.postValue(null);
                userTasksLiveData.postValue(null);
                currentTaskLiveData.postValue(null);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing tasks", e);
                errorLiveData.postValue("Failed to clear tasks: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public boolean isClosed() {
        return executorService == null || executorService.isShutdown();
    }
}



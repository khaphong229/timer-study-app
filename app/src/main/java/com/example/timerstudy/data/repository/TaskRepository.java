
package com.example.timerstudy.data.repository;

import android.content.Context;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.callback.DataCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    // Khớp với Presenter: lấy task theo userId và ngày
    public void getTasksByUserIdAndDate(int userId, long taskDate, DataCallback<List<TaskEntity>> callback) {
        getTasksByUserAndDate(userId, taskDate, callback);
    }
    private final TaskDao taskDao;
    private final ExecutorService executor;

    public TaskRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void getAllTasks(DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getAllTasks());
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUserId(int userId, DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getTasksByUserId(userId));
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUserAndDate(int userId, long taskDate, DataCallback<List<TaskEntity>> callback) {
        executor.execute(() -> {
            try {
                callback.onSuccess(taskDao.getTasksByUserAndDate(userId, taskDate));
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void insertTask(TaskEntity task, DataCallback<Void> callback){
        executor.execute(() -> {
            try {
                taskDao.insertTask(task);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updateTask(TaskEntity task, DataCallback<Void> callback){
        executor.execute(() -> {
            try {
                taskDao.updateTask(task);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteTask(TaskEntity task, DataCallback<Void> callback){
        executor.execute(() -> {
            try {
                taskDao.deleteTask(task);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteTaskById(int taskId, DataCallback<Void> callback){
        executor.execute(() -> {
            try {
                taskDao.deleteTaskById(taskId);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}

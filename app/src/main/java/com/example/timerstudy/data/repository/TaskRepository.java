
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
    private final TaskDao taskDao;
    private final ExecutorService executor;

    public TaskRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // ASYNC methods with callbacks - RECOMMENDED APPROACH

    public List<TaskEntity> getTasksByUserAndDate(int userId, long taskDate) {
        return taskDao.getTasksByUserAndDate(userId, taskDate);
    }
    public void getAllTasks(DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                List<TaskEntity> tasks = taskDao.getAllTasks();
                callback.onSuccess(tasks);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUserId(int userId, DataCallback<List<TaskEntity>> callback){
        executor.execute(() -> {
            try {
                List<TaskEntity> tasks = taskDao.getTasksByUserId(userId);
                callback.onSuccess(tasks);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // SYNC methods for backward compatibility - SHOULD BE DEPRECATED
    public List<TaskEntity> getAllTasks(){
        return taskDao.getAllTasks();
    }

    public List<TaskEntity> getTasksByUserId(int userId){
        return taskDao.getTasksByUserId(userId);
    }

    // Async methods with callbacks for proper sequencing
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

    public void insertTask(TaskEntity task){
        taskDao.insertTask(task);
    }

    public void updateTask(TaskEntity task){
        taskDao.updateTask(task);
    }

    public void deleteTask(TaskEntity task){
        taskDao.deleteTask(task);
    }

    public void deleteTaskById(int taskId){
        taskDao.deleteTaskById(taskId);
    }

}

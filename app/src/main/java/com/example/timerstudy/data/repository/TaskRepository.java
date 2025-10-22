package com.example.timerstudy.data.repository;

import android.content.Context;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;


    public TaskRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
    }

    public List<TaskEntity> getAllTasks(){
        return taskDao.getAllTasks();
    }

    public void insertTask(TaskEntity task){
        Executors.newSingleThreadExecutor().execute(()->taskDao.insertTask(task));
    }

    public void updateTask(TaskEntity task){
        Executors.newSingleThreadExecutor().execute(() -> taskDao.updateTask(task));
    }





}

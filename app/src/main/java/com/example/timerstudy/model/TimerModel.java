package com.example.timerstudy.model;

import android.content.Context;
import com.example.timerstudy.database.TimerDatabase;
import com.example.timerstudy.database.TimerDao;
import com.example.timerstudy.database.TimerEntity;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerModel {
    private TimerDao timerDao;
    private ExecutorService executor;
    
    public TimerModel(Context context) {
        TimerDatabase database = TimerDatabase.getInstance(context);
        this.timerDao = database.timerDao();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public List<TimerItem> getAllTimers() {
        try {
            List<TimerEntity> entities = timerDao.getAllTimersSync();
            List<TimerItem> timers = new ArrayList<>();
            for (TimerEntity entity : entities) {
                timers.add(convertToTimerItem(entity));
            }
            return timers;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public void addTimer(TimerItem timer) {
        executor.execute(() -> {
            try {
                TimerEntity entity = convertToTimerEntity(timer);
                timerDao.insert(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void updateTimer(TimerItem timer) {
        executor.execute(() -> {
            try {
                TimerEntity entity = convertToTimerEntity(timer);
                timerDao.update(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void deleteTimer(int id) {
        executor.execute(() -> {
            try {
                timerDao.deleteById(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private TimerItem convertToTimerItem(TimerEntity entity) {
        return new TimerItem(
            entity.getId(),
            entity.getName(),
            entity.getDuration(),
            entity.getRemainingTime(),
            entity.isActive()
        );
    }
    
    private TimerEntity convertToTimerEntity(TimerItem item) {
        TimerEntity entity = new TimerEntity(item.getName(), item.getTotalTime());
        entity.setId(item.getId());
        entity.setRemainingTime(item.getCurrentTime());
        entity.setActive(item.isRunning());
        return entity;
    }
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

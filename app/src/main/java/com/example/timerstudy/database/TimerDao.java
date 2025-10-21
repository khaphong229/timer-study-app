package com.example.timerstudy.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TimerDao {
    @Insert
    long insert(TimerEntity timer);
    
    @Insert
    long insertTimer(TimerEntity timer);
    
    @Update
    void update(TimerEntity timer);
    
    @Update
    void updateTimer(TimerEntity timer);
    
    @Delete
    void deleteTimer(TimerEntity timer);
    
    @Query("DELETE FROM timers WHERE id = :id")
    void deleteById(int id);
    
    @Query("SELECT * FROM timers ORDER BY createdAt DESC")
    LiveData<List<TimerEntity>> getAllTimers();
    
    @Query("SELECT * FROM timers ORDER BY createdAt DESC")
    List<TimerEntity> getAllTimersSync();
    
    @Query("SELECT * FROM timers WHERE id = :id")
    LiveData<TimerEntity> getTimerById(int id);
    
    @Query("SELECT * FROM timers WHERE isActive = 1")
    LiveData<List<TimerEntity>> getActiveTimers();
}

package com.example.timerstudy.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TimerEntity.class}, version = 1, exportSchema = false)
public abstract class TimerDatabase extends RoomDatabase {
    private static TimerDatabase INSTANCE;
    
    public abstract TimerDao timerDao();
    
    public static synchronized TimerDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                TimerDatabase.class,
                "timer_database"
            ).allowMainThreadQueries()
            .build();
        }
        return INSTANCE;
    }
    
    public static synchronized TimerDatabase getInstance(Context context) {
        return getDatabase(context);
    }
}

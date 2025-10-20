package com.example.timerstudy.data.local.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.example.timerstudy.data.local.database.converters.DateConverter;
import com.example.timerstudy.data.local.database.dao.GoalDao;
import com.example.timerstudy.data.local.database.dao.SessionDao;
import com.example.timerstudy.data.local.database.dao.SettingDao;
import com.example.timerstudy.data.local.database.dao.StatisticsDao;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.dao.UserDao;
import com.example.timerstudy.data.local.database.entities.DefaultSettingEntity;
import com.example.timerstudy.data.local.database.entities.GoalEntity;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.local.database.entities.SessionPauseEntity;
import com.example.timerstudy.data.local.database.entities.StatisticsCacheEntity;
import com.example.timerstudy.data.local.database.entities.StreakRecordEntity;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.local.database.entities.TaskSessionEntity;
import com.example.timerstudy.data.local.database.entities.UserEntity;
import com.example.timerstudy.data.local.database.entities.UserSettingEntity;

/**
 * Room Database class for Timer Study App
 * 
 * This class defines the database configuration, entities, DAOs, and migration strategies.
 * It follows the Singleton pattern to ensure only one database instance exists.
 * 
 * Database Version: 1
 * Entities: 10 Entity classes
 * DAOs: 6 DAO interfaces
 * Type Converters: DateConverter
 */
@Database(
    entities = {
        UserEntity.class,
        SessionEntity.class,
        SessionPauseEntity.class,
        TaskEntity.class,
        TaskSessionEntity.class,
        GoalEntity.class,
        UserSettingEntity.class,
        DefaultSettingEntity.class,
        StatisticsCacheEntity.class,
        StreakRecordEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    // Database name
    private static final String DATABASE_NAME = "timer_study_database";
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // DAO interfaces
    public abstract UserDao userDao();
    public abstract SessionDao sessionDao();
    public abstract TaskDao taskDao();
    public abstract GoalDao goalDao();
    public abstract SettingDao settingDao();
    public abstract StatisticsDao statisticsDao();
    
    /**
     * Get database instance using Singleton pattern
     * 
     * @param context Application context
     * @return AppDatabase instance
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .addCallback(roomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Get database instance with custom configuration
     * 
     * @param context Application context
     * @param allowMainThreadQueries Allow queries on main thread (for testing)
     * @return AppDatabase instance
     */
    public static AppDatabase getDatabase(final Context context, boolean allowMainThreadQueries) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    RoomDatabase.Builder<AppDatabase> builder = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    );
                    
                    if (allowMainThreadQueries) {
                        builder.allowMainThreadQueries();
                    }
                    
                    INSTANCE = builder
                        .addCallback(roomDatabaseCallback)
                        .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Room database callback for database creation and opening
     */
    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Database created for the first time
            // You can add initial data here if needed
        }
        
        @Override
        public void onOpen(SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Database opened
            // You can add any initialization here if needed
        }
    };
    
    /**
     * Close database connection
     * Call this when the app is being destroyed
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    
    /**
     * Clear database instance (useful for testing)
     */
    public static void clearInstance() {
        INSTANCE = null;
    }
    
    /**
     * Check if database is open
     * 
     * @return True if database is open, false otherwise
     */
    public boolean isDatabaseOpen() {
        return INSTANCE != null && INSTANCE.isOpen();
    }
    
    // ==================== MIGRATION STRATEGIES ====================
    
    /**
     * Migration from version 1 to 2
     * Example migration - add new column to users table
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Add new column to users table
            // database.execSQL("ALTER TABLE users ADD COLUMN last_activity INTEGER");
        }
    };
    
    /**
     * Migration from version 2 to 3
     * Example migration - create new table
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Create new table
            // database.execSQL("CREATE TABLE IF NOT EXISTS new_table (id INTEGER PRIMARY KEY, name TEXT)");
        }
    };
    
    /**
     * Migration from version 3 to 4
     * Example migration - drop table
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Drop table
            // database.execSQL("DROP TABLE IF EXISTS old_table");
        }
    };
    
    /**
     * Get all available migrations
     * 
     * @return Array of Migration objects
     */
    public static Migration[] getAllMigrations() {
        return new Migration[]{
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        };
    }
    
    // ==================== DATABASE UTILITIES ====================
    
    /**
     * Clear all data from all tables
     * WARNING: This will delete all data permanently
     */
    public void clearAllData() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            // Clear all tables in correct order (respecting foreign key constraints)
            INSTANCE.clearAllTables();
        }
    }
    
    /**
     * Get database file size in bytes
     * 
     * @param context Application context
     * @return Database file size in bytes, -1 if error
     */
    public static long getDatabaseSize(Context context) {
        try {
            return context.getDatabasePath(DATABASE_NAME).length();
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * Get database file path
     * 
     * @param context Application context
     * @return Database file path
     */
    public static String getDatabasePath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }
    
    /**
     * Check if database file exists
     * 
     * @param context Application context
     * @return True if database file exists, false otherwise
     */
    public static boolean databaseExists(Context context) {
        return context.getDatabasePath(DATABASE_NAME).exists();
    }
    
    /**
     * Delete database file
     * WARNING: This will delete the entire database permanently
     * 
     * @param context Application context
     * @return True if database was deleted, false otherwise
     */
    public static boolean deleteDatabase(Context context) {
        try {
            closeDatabase();
            return context.deleteDatabase(DATABASE_NAME);
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== DATABASE CONFIGURATION ====================
    
    /**
     * Database configuration constants
     */
    public static class Config {
        // Database name
        public static final String NAME = DATABASE_NAME;
        
        // Current database version
        public static final int VERSION = 1;
        
        // Maximum number of database connections
        public static final int MAX_CONNECTIONS = 1;
        
        // Database timeout in seconds
        public static final int TIMEOUT_SECONDS = 30;
        
        // Enable foreign key constraints
        public static final boolean ENABLE_FOREIGN_KEYS = true;
        
        // Enable WAL mode for better concurrency
        public static final boolean ENABLE_WAL_MODE = true;
        
        // Journal mode
        public static final String JOURNAL_MODE = "WAL";
        
        // Synchronous mode
        public static final String SYNCHRONOUS_MODE = "NORMAL";
        
        // Cache size (in KB)
        public static final int CACHE_SIZE_KB = 2000;
        
        // Page size (in bytes)
        public static final int PAGE_SIZE_BYTES = 4096;
    }
    
    /**
     * Database statistics
     */
    public static class Statistics {
        private int totalUsers;
        private int totalSessions;
        private int totalTasks;
        private int totalGoals;
        private long databaseSize;
        
        public Statistics(int totalUsers, int totalSessions, int totalTasks, int totalGoals, long databaseSize) {
            this.totalUsers = totalUsers;
            this.totalSessions = totalSessions;
            this.totalTasks = totalTasks;
            this.totalGoals = totalGoals;
            this.databaseSize = databaseSize;
        }
        
        // Getters
        public int getTotalUsers() { return totalUsers; }
        public int getTotalSessions() { return totalSessions; }
        public int getTotalTasks() { return totalTasks; }
        public int getTotalGoals() { return totalGoals; }
        public long getDatabaseSize() { return databaseSize; }
        
        @Override
        public String toString() {
            return "DatabaseStatistics{" +
                    "totalUsers=" + totalUsers +
                    ", totalSessions=" + totalSessions +
                    ", totalTasks=" + totalTasks +
                    ", totalGoals=" + totalGoals +
                    ", databaseSize=" + databaseSize +
                    '}';
        }
    }
}

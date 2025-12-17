package com.example.timerstudy.data.local.database;

import androidx.annotation.NonNull;
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
import com.example.timerstudy.data.local.database.dao.TimerDao;
import com.example.timerstudy.data.local.database.dao.UserDao;
import com.example.timerstudy.data.local.database.entities.DefaultSettingEntity;
import com.example.timerstudy.data.local.database.entities.GoalEntity;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.local.database.entities.SessionPauseEntity;
import com.example.timerstudy.data.local.database.entities.StatisticsCacheEntity;
import com.example.timerstudy.data.local.database.entities.StreakRecordEntity;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.local.database.entities.TaskSessionEntity;
import com.example.timerstudy.data.local.database.entities.TimerEntity;
import com.example.timerstudy.data.local.database.entities.UserEntity;
import com.example.timerstudy.data.local.database.entities.UserSettingEntity;


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
                StreakRecordEntity.class,
                TimerEntity.class
        },
        version = 3,
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
    public abstract TimerDao timerDao();

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
                            .fallbackToDestructiveMigration()
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
                            .fallbackToDestructiveMigration()
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Alternative method name for backward compatibility
     *
     * @param context Application context
     * @return AppDatabase instance
     */
    public static synchronized AppDatabase getInstance(Context context) {
        return getDatabase(context);
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

    public static void clearInstance() {
        INSTANCE = null;
    }

    public boolean isDatabaseOpen() {
        return INSTANCE != null && INSTANCE.isOpen();
    }

    // ==================== MIGRATION STRATEGIES ====================

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create temporary table with new schema for users
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS users_temp (" +
                "user_id TEXT NOT NULL PRIMARY KEY, " +
                "email TEXT, " +
                "display_name TEXT, " +
                "profile_picture_url TEXT, " +
                "created_at INTEGER, " +
                "last_login INTEGER, " +
                "is_anonymous INTEGER NOT NULL DEFAULT 1)"
            );

            // Create temporary tasks table with new schema
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks_temp (" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +  // Changed from INTEGER to TEXT
                "title TEXT, " +
                "description TEXT, " +
                "priority TEXT DEFAULT 'MEDIUM', " +
                "task_date INTEGER, " +
                "is_completed INTEGER NOT NULL DEFAULT 0, " +
                "completed_at INTEGER, " +
                "total_time_spent INTEGER NOT NULL DEFAULT 0, " +
                "estimated_sessions INTEGER NOT NULL DEFAULT 1, " +
                "actual_sessions INTEGER NOT NULL DEFAULT 0, " +
                "order_index INTEGER NOT NULL DEFAULT 0, " +
                "created_at INTEGER, " +
                "updated_at INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE)"
            );

            // Copy data from old users table to new users table, converting user_id to TEXT
            database.execSQL(
                "INSERT INTO users_temp (user_id, email, display_name, profile_picture_url, " +
                "created_at, last_login, is_anonymous) " +
                "SELECT CAST(user_id AS TEXT), email, display_name, profile_picture_url, " +
                "created_at, last_login, is_anonymous FROM users"
            );

            // Copy data from old tasks table to new tasks table, converting user_id to TEXT
            database.execSQL(
                "INSERT INTO tasks_temp (task_id, user_id, title, description, priority, " +
                "task_date, is_completed, completed_at, total_time_spent, estimated_sessions, " +
                "actual_sessions, order_index, created_at, updated_at) " +
                "SELECT task_id, CAST(user_id AS TEXT), title, description, priority, " +
                "task_date, is_completed, completed_at, total_time_spent, estimated_sessions, " +
                "actual_sessions, order_index, created_at, updated_at FROM tasks"
            );

            // Drop old tables
            database.execSQL("DROP TABLE users");
            database.execSQL("DROP TABLE tasks");

            // Rename temporary tables
            database.execSQL("ALTER TABLE users_temp RENAME TO users");
            database.execSQL("ALTER TABLE tasks_temp RENAME TO tasks");

            // Recreate indices for both tables
            database.execSQL("CREATE INDEX IF NOT EXISTS index_users_email ON users (email)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_users_created_at ON users (created_at)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_user_id ON tasks (user_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_task_date ON tasks (task_date)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_priority ON tasks (priority)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_is_completed ON tasks (is_completed)");
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_user_task_date ON tasks (user_id, task_date)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Create new table
            // database.execSQL("CREATE TABLE IF NOT EXISTS new_table (id INTEGER PRIMARY KEY, name TEXT)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Drop table
            // database.execSQL("DROP TABLE IF EXISTS old_table");
        }
    };

    public static Migration[] getAllMigrations() {
        return new Migration[]{
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4
        };
    }

    public void clearAllData() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            // Clear all tables in correct order (respecting foreign key constraints)
            INSTANCE.clearAllTables();
        }
    }

    public static long getDatabaseSize(Context context) {
        try {
            return context.getDatabasePath(DATABASE_NAME).length();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getDatabasePath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    public static boolean databaseExists(Context context) {
        return context.getDatabasePath(DATABASE_NAME).exists();
    }

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
        private int totalTimers;
        private long databaseSize;

        public Statistics(int totalUsers, int totalSessions, int totalTasks, int totalGoals, int totalTimers, long databaseSize) {
            this.totalUsers = totalUsers;
            this.totalSessions = totalSessions;
            this.totalTasks = totalTasks;
            this.totalGoals = totalGoals;
            this.totalTimers = totalTimers;
            this.databaseSize = databaseSize;
        }

        // Getters
        public int getTotalUsers() { return totalUsers; }
        public int getTotalSessions() { return totalSessions; }
        public int getTotalTasks() { return totalTasks; }
        public int getTotalGoals() { return totalGoals; }
        public int getTotalTimers() { return totalTimers; }
        public long getDatabaseSize() { return databaseSize; }

        @Override
        public String toString() {
            return "DatabaseStatistics{" +
                    "totalUsers=" + totalUsers +
                    ", totalSessions=" + totalSessions +
                    ", totalTasks=" + totalTasks +
                    ", totalGoals=" + totalGoals +
                    ", totalTimers=" + totalTimers +
                    ", databaseSize=" + databaseSize +
                    '}';
        }
    }
}
package com.example.timerstudy.utils;

import android.content.Context;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.SessionDao;
import com.example.timerstudy.data.local.database.dao.TaskDao;
import com.example.timerstudy.data.local.database.dao.GoalDao;
import com.example.timerstudy.data.local.database.dao.UserDao;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.local.database.entities.GoalEntity;
import com.example.timerstudy.data.local.database.entities.UserEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FakeDataSeeder {

    public static void seed(Context context) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext(), true);
            UserDao userDao = db.userDao();
            SessionDao sessionDao = db.sessionDao();
            TaskDao taskDao = db.taskDao();
            GoalDao goalDao = db.goalDao();

            // Ensure a user exists
            long userId = ensureUser(userDao);

            // Seed random non-overlapping sessions per day for the current month
            seedRandomNonOverlappingForCurrentMonth(sessionDao, userId);

            // Generate tasks for last 14 days
            taskDao.deleteAllTasks();

            goalDao.deleteAllGoals();
            // Generate goals for last 14 days
        }).start();
    }


    private static long ensureUser(UserDao userDao) {
        List<UserEntity> users = userDao.getAllUsers();
        if (users != null && !users.isEmpty()) {
            return users.get(0).getUserId();
        }
        UserEntity u = new UserEntity();
        u.setDisplayName("Demo User");
        u.setEmail("demo@example.com");
        long id = userDao.insertUser(u);
        return (int) id;
    }

    private static void seedRandomNonOverlappingForCurrentMonth(SessionDao sessionDao, long userId) {
        // Clear old demo sessions
        sessionDao.deleteAllSessions();

        Calendar cal = Calendar.getInstance();
        // Set to first day of current month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(Calendar.DAY_OF_MONTH, 1);
        monthEnd.set(Calendar.HOUR_OF_DAY, 0);
        monthEnd.set(Calendar.MINUTE, 0);
        monthEnd.set(Calendar.SECOND, 0);
        monthEnd.set(Calendar.MILLISECOND, 0);
        monthEnd.add(Calendar.MONTH, 1);

        while (cal.before(monthEnd)) {
            // session_date = date only (00:00 of the day)
            Calendar day = (Calendar) cal.clone();
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);

            // Generate 1-4 non-overlapping random sessions between 08:00 and 22:00
            java.util.Random rnd = new java.util.Random();
            int count = 1 + rnd.nextInt(4);
            java.util.List<int[]> intervals = new java.util.ArrayList<>(); // [startMin, endMin]

            int attempts = 0;
            while (intervals.size() < count && attempts < 100) {
                attempts++;
                int windowStart = 8 * 60;   // 08:00 in minutes
                int windowEnd = 22 * 60;    // 22:00 in minutes
                int duration = 15 + rnd.nextInt(60); // 15-74 minutes
                int startMin = windowStart + rnd.nextInt(Math.max(1, windowEnd - windowStart - duration));
                int endMin = startMin + duration;

                // Check overlap
                boolean overlaps = false;
                for (int[] it : intervals) {
                    if (!(endMin <= it[0] || startMin >= it[1])) { overlaps = true; break; }
                }
                if (overlaps) continue;

                intervals.add(new int[]{startMin, endMin});
            }

            // Sort by start time and insert
            intervals.sort(java.util.Comparator.comparingInt(a -> a[0]));
            for (int[] it : intervals) {
                int startMin = it[0];
                int endMin = it[1];

                Calendar start = (Calendar) day.clone();
                start.add(Calendar.MINUTE, startMin);
                Calendar end = (Calendar) day.clone();
                end.add(Calendar.MINUTE, endMin);

                int duration = endMin - startMin;
                SessionEntity s = new SessionEntity();
                s.setUserId(userId);
                s.setSessionDate(day.getTime());
                s.setStartTime(start.getTime());
                s.setEndTime(end.getTime());
                s.setDurationMinutes(duration);
                s.setActualDurationMinutes(duration);
                s.setSessionType(SessionEntity.TYPE_FOCUS_SESSION);
                s.setStatus(SessionEntity.STATUS_COMPLETED);
                s.setCompleted(true);
                sessionDao.insertSession(s);
            }

            // Move to next day
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // Task generation removed - only real tasks will be saved
    @Deprecated
    private static void generateTasks(TaskDao taskDao, long userId, int daysBack) {
        // Method removed to ensure only real user tasks are stored
    }


}



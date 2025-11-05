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
            int userId = ensureUser(userDao);

            // Generate sessions for last 30 days
            generateSessions(sessionDao, userId, 30);

            // Generate tasks for last 14 days
            taskDao.deleteAllTasks();

            goalDao.deleteAllGoals();
            // Generate goals for last 14 days
        }).start();
    }


    private static int ensureUser(UserDao userDao) {
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

    private static void generateSessions(SessionDao sessionDao, int userId, int daysBack) {
        Random rnd = new Random();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        for (int d = 0; d < daysBack; d++) {
            Date day = cal.getTime();

            int sessionsCount = 1 + rnd.nextInt(4);
            for (int i = 0; i < sessionsCount; i++) {
                int startMin = 8 * 60 + rnd.nextInt(10 * 60); // between 08:00 and ~18:00
                int duration = 20 + rnd.nextInt(60); // 20-80 minutes

                Calendar start = (Calendar) cal.clone();
                start.add(Calendar.MINUTE, startMin);
                Calendar end = (Calendar) start.clone();
                end.add(Calendar.MINUTE, duration);

                SessionEntity s = new SessionEntity();
                s.setUserId(userId);
                s.setSessionDate(day);
                s.setStartTime(start.getTime());
                s.setEndTime(end.getTime());
                s.setDurationMinutes(duration);
                s.setActualDurationMinutes(duration);
                s.setSessionType(SessionEntity.TYPE_FOCUS_SESSION);
                s.setStatus(SessionEntity.STATUS_COMPLETED);
                s.setCompleted(true);
                sessionDao.insertSession(s);
            }

            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
    }

    // Task generation removed - only real tasks will be saved
    @Deprecated
    private static void generateTasks(TaskDao taskDao, int userId, int daysBack) {
        // Method removed to ensure only real user tasks are stored
    }


}



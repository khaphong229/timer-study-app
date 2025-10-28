package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.GoalDao;
import com.example.timerstudy.data.local.database.entities.GoalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Goal data operations
 * Wraps {@link GoalDao} with background execution and LiveData exposure.
 */
public class GoalRepository {

    private static final String TAG = "GoalRepository";

    // Database and DAO
    private final AppDatabase database;
    private final GoalDao goalDao;

    // Thread executor for background operations
    private final ExecutorService executorService;

    // LiveData for reactive updates
    private final MutableLiveData<List<GoalEntity>> allGoalsLiveData;
    private final MutableLiveData<List<GoalEntity>> userGoalsLiveData;
    private final MutableLiveData<GoalEntity> currentGoalLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    // Singleton instance
    private static volatile GoalRepository INSTANCE;

    private GoalRepository(Context context) {
        database = AppDatabase.getDatabase(context);
        goalDao = database.goalDao();
        executorService = Executors.newFixedThreadPool(4);

        allGoalsLiveData = new MutableLiveData<>();
        userGoalsLiveData = new MutableLiveData<>();
        currentGoalLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public static GoalRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GoalRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GoalRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // LiveData getters
    public LiveData<List<GoalEntity>> getAllGoalsLiveData() { return allGoalsLiveData; }
    public LiveData<List<GoalEntity>> getUserGoalsLiveData() { return userGoalsLiveData; }
    public LiveData<GoalEntity> getCurrentGoalLiveData() { return currentGoalLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    // ==================== Operations ====================

    public void loadAllGoals() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<GoalEntity> goals = goalDao.getAllGoals();
                allGoalsLiveData.postValue(goals);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all goals", e);
                errorLiveData.postValue("Failed to load goals: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void loadGoalsByUserId(int userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<GoalEntity> goals = goalDao.getGoalsByUserId(userId);
                userGoalsLiveData.postValue(goals);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading goals by userId=" + userId, e);
                errorLiveData.postValue("Failed to load user goals: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void getGoalById(int goalId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                GoalEntity goal = goalDao.getGoalById(goalId);
                currentGoalLiveData.postValue(goal);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting goal by ID: " + goalId, e);
                errorLiveData.postValue("Failed to get goal: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void createGoal(GoalEntity goal) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long id = goalDao.insertGoal(goal);
                goal.setGoalId((int) id);
                currentGoalLiveData.postValue(goal);
                loadGoalsByUserId(goal.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating goal", e);
                errorLiveData.postValue("Failed to create goal: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateGoal(GoalEntity goal) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                goalDao.updateGoal(goal);
                currentGoalLiveData.postValue(goal);
                loadGoalsByUserId(goal.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating goal", e);
                errorLiveData.postValue("Failed to update goal: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void deleteGoal(GoalEntity goal) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                goalDao.deleteGoal(goal);
                loadGoalsByUserId(goal.getUserId());
                if (currentGoalLiveData.getValue() != null && currentGoalLiveData.getValue().getGoalId() == goal.getGoalId()) {
                    currentGoalLiveData.postValue(null);
                }
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting goal", e);
                errorLiveData.postValue("Failed to delete goal: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void clearAllData() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                goalDao.deleteAllGoals();
                allGoalsLiveData.postValue(null);
                userGoalsLiveData.postValue(null);
                currentGoalLiveData.postValue(null);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing goals", e);
                errorLiveData.postValue("Failed to clear goals: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public boolean isClosed() {
        return executorService == null || executorService.isShutdown();
    }
}



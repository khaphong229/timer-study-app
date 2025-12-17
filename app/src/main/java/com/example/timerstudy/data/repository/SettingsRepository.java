package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.SettingDao;
import com.example.timerstudy.data.local.database.entities.DefaultSettingEntity;
import com.example.timerstudy.data.local.database.entities.UserSettingEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Settings data operations (user and default settings)
 * Wraps {@link SettingDao} with background execution and LiveData exposure.
 */
public class SettingsRepository {

    private static final String TAG = "SettingsRepository";

    // Database and DAO
    private final AppDatabase database;
    private final SettingDao settingDao;

    // Thread executor for background operations
    private final ExecutorService executorService;

    // LiveData for reactive updates
    private final MutableLiveData<List<UserSettingEntity>> userSettingsLiveData;
    private final MutableLiveData<List<DefaultSettingEntity>> defaultSettingsLiveData;
    private final MutableLiveData<UserSettingEntity> currentUserSettingLiveData;
    private final MutableLiveData<DefaultSettingEntity> currentDefaultSettingLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    // Singleton instance
    private static volatile SettingsRepository INSTANCE;

    private SettingsRepository(Context context) {
        database = AppDatabase.getDatabase(context);
        settingDao = database.settingDao();
        executorService = Executors.newFixedThreadPool(4);

        userSettingsLiveData = new MutableLiveData<>();
        defaultSettingsLiveData = new MutableLiveData<>();
        currentUserSettingLiveData = new MutableLiveData<>();
        currentDefaultSettingLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public static SettingsRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingsRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // LiveData getters
    public LiveData<List<UserSettingEntity>> getUserSettingsLiveData() { return userSettingsLiveData; }
    public LiveData<List<DefaultSettingEntity>> getDefaultSettingsLiveData() { return defaultSettingsLiveData; }
    public LiveData<UserSettingEntity> getCurrentUserSettingLiveData() { return currentUserSettingLiveData; }
    public LiveData<DefaultSettingEntity> getCurrentDefaultSettingLiveData() { return currentDefaultSettingLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    // ==================== USER SETTINGS ====================

    public void loadAllUserSettings() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<UserSettingEntity> list = settingDao.getAllUserSettings();
                userSettingsLiveData.postValue(list);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all user settings", e);
                errorLiveData.postValue("Failed to load user settings: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void loadUserSettingsByUserId(long userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<UserSettingEntity> list = settingDao.getUserSettingsByUserId(userId);
                userSettingsLiveData.postValue(list);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading user settings by userId=" + userId, e);
                errorLiveData.postValue("Failed to load user settings: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void getUserSettingByKey(long userId, String key) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                UserSettingEntity item = settingDao.getUserSettingByKey(userId, key);
                currentUserSettingLiveData.postValue(item);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user setting by key", e);
                errorLiveData.postValue("Failed to get user setting: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void createUserSetting(UserSettingEntity setting) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long id = settingDao.insertUserSetting(setting);
                setting.setSettingId((int) id);
                currentUserSettingLiveData.postValue(setting);
                loadUserSettingsByUserId(setting.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating user setting", e);
                errorLiveData.postValue("Failed to create user setting: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateUserSetting(UserSettingEntity setting) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                settingDao.updateUserSetting(setting);
                currentUserSettingLiveData.postValue(setting);
                loadUserSettingsByUserId(setting.getUserId());
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user setting", e);
                errorLiveData.postValue("Failed to update user setting: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // ==================== DEFAULT SETTINGS ====================

    public void loadAllDefaultSettings() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<DefaultSettingEntity> list = settingDao.getAllDefaultSettings();
                defaultSettingsLiveData.postValue(list);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all default settings", e);
                errorLiveData.postValue("Failed to load default settings: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void getDefaultSettingByKey(String key) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                DefaultSettingEntity item = settingDao.getDefaultSettingByKey(key);
                currentDefaultSettingLiveData.postValue(item);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting default setting by key", e);
                errorLiveData.postValue("Failed to get default setting: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // ==================== COMBINED ====================

    public void getUserOrDefaultSettingValue(long userId, String key) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                // Note: returning via errorLiveData is not ideal; expose a dedicated LiveData if needed later
                String value = settingDao.getUserOrDefaultSettingValue(userId, key);
                // Reuse currentUserSettingLiveData is not appropriate; leave as is for now or extend API later
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user or default setting value", e);
                errorLiveData.postValue("Failed to get setting value: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void clearAllData() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                settingDao.deleteAllUserSettings();
                settingDao.deleteAllDefaultSettings();
                userSettingsLiveData.postValue(null);
                defaultSettingsLiveData.postValue(null);
                currentUserSettingLiveData.postValue(null);
                currentDefaultSettingLiveData.postValue(null);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing settings", e);
                errorLiveData.postValue("Failed to clear settings: " + e.getMessage());
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



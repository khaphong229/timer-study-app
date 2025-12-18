package com.example.timerstudy.presenter;

import android.content.Context;
import android.util.Log;

import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.TimerModel;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.TimerContract;

public class TimerPresenter implements TimerContract.Presenter, TimerModel.TimerListener {

    private TimerContract.View view;
    private TimerModel model;
    public static TimerPresenter instance;

    private Context context;
    private UserManager userManager;
    private SessionRepository sessionRepository;
    private int completedSessionsFromDb = 0;

    public static TimerPresenter getInstance() {
        if (instance == null) {
            instance = new TimerPresenter();
        }
        return instance;
    }

    public TimerPresenter() {
        model = new TimerModel();
        model.setTimerListener(this);
    }

    public void initialize(Context context) {
        this.context = context;
        this.userManager = UserManager.getInstance(context);
        this.sessionRepository = SessionRepository.getInstance(context);

        int duration = userManager.getTimerDuration();
        // Ensure duration is at least 1 minute
        if (duration <= 0) {
            duration = 25; // Default to 25 minutes
            userManager.setTimerDuration(duration);
        }
        
        // Convert minutes to milliseconds for the model
        model.setStudyDuration(duration * 60 * 1000L);

        loadCompletedSessionsCount();
    }

    @Override
    public void attachView(TimerContract.View view) {
        this.view = view;
        initializeView();

        observeCompletedSessionsCount();
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private void initializeView() {
        if (view != null) {
            view.updateTimeDisplay(formatTime(model.getCurrentTimeRemaining()));
            view.updateSessionType(model.isStudySession());
            view.updateCompletedSessions(completedSessionsFromDb);
            view.updateControlButtons(model.isRunning());
        }
    }

    @Override
    public void onStartClicked() {
        model.startTimer();
        if (view != null) {
            view.updateControlButtons(true);
            view.keepScreenOn();
        }
    }

    @Override
    public void onPauseClicked() {
        model.pauseTimer();
        if (view != null) {
            view.updateControlButtons(false);
            view.allowScreenOff();
        }
    }

    @Override
    public void onResetClicked() {
        model.resetTimer();
        if (view != null) {
            view.updateControlButtons(false);
            view.allowScreenOff();
        }
    }

    @Override
    public void onStudyDurationChanged(int minutes) {
        model.setStudyDuration(minutes * 60 * 1000L);
        userManager.setTimerDuration(minutes);
    }

    @Override
    public void onBreakDurationChanged(int minutes) {
        model.setBreakDuration(minutes * 60 * 1000L);
    }

    @Override
    public void onDestroy() {
        model.pauseTimer();
        detachView();
        if (view != null) {
            view.allowScreenOff();
        }
    }

    // TimerModel.TimerListener implementation
    @Override
    public void onTimeUpdate(long timeRemaining) {
        if (view != null) {
            view.updateTimeDisplay(formatTime(timeRemaining));
        }
    }

    @Override
    public void onSessionComplete() {
    
        saveSessionCompleted();
   
        if (view != null) {
            view.showSessionCompleted();
            view.playCompletionSound();
            view.vibrateDevice();
        }
    }

    @Override
    public void onSessionTypeChange(boolean isStudySession) {
        if (view != null) {
            view.updateSessionType(isStudySession);
        }
    }

    @Override
    public void onBreakComplete() {
        if (view != null) {
            view.showToast("Break time completed! Ready for next study session.");
            view.updateControlButtons(false);
            view.allowScreenOff();
            view.playCompletionSound();
            view.vibrateDevice();
        }
    }

    private String formatTime(long timeInMillis) {
        long minutes = timeInMillis / 1000 / 60;
        long seconds = (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public int getCompletedSessions() {
        return completedSessionsFromDb;
    }

    @Override
    public int getStudyDurationMinutes() {
        return userManager.getTimerDuration();
    }

    @Override
    public int getBreakDurationMinutes() {
        return (int) (model.getBreakDuration() / 1000 / 60);
    }

    @Override
    public void saveSessionCompleted() {
        if (sessionRepository != null) {
            int studyDuration = getStudyDurationMinutes();
            Log.d("FixBugSaveSession", "saveSessionCompleted: " + studyDuration);
            Log.d("FixBugSaveSession", "userManager.getCurrentUserId(): " + userManager.getCurrentUserId());
            sessionRepository.saveCompletedStudySession(userManager.getCurrentUserId(), studyDuration);
            
            // Add coins based on study duration (1 minute = 1 coin)
            userManager.addCoins(studyDuration);
            
            view.updateCompletedSessions(++completedSessionsFromDb);
        }
    }


    private void loadCompletedSessionsCount() {
        if (sessionRepository != null) {
            sessionRepository.loadCompletedSessionsCountToday(userManager.getCurrentUserId());
            Log.d("FixBugLoadSession", "loadCompletedSessionsCount: " + completedSessionsFromDb);
            Log.d("FixBugLoadSession", "userManager.getCurrentUserId(): " + userManager.getCurrentUserId());
        }
    }

    private void observeCompletedSessionsCount() {
        if (sessionRepository != null && view != null) {
            sessionRepository.getCompletedSessionsCountLiveData().observe(
                    ((androidx.lifecycle.LifecycleOwner) view),
                    count -> {
                        if (count != null) {
                            completedSessionsFromDb = count;
                            if (view != null) {
                                view.updateCompletedSessions(count);
                            }
                        }
                    }
            );
        }
    }


}

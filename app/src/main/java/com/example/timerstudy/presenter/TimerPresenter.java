package com.example.timerstudy.presenter;

import com.example.timerstudy.model.TimerModel;

public class TimerPresenter implements TimerContract.Presenter, TimerModel.TimerListener {
    
    private TimerContract.View view;
    private TimerModel model;
    
    public TimerPresenter() {
        model = new TimerModel();
        model.setTimerListener(this);
    }
    
    @Override
    public void attachView(TimerContract.View view) {
        this.view = view;
        initializeView();
    }
    
    @Override
    public void detachView() {
        this.view = null;
    }
    
    private void initializeView() {
        if (view != null) {
            view.updateTimeDisplay(formatTime(model.getCurrentTimeRemaining()));
            view.updateSessionType(model.isStudySession());
            view.updateCompletedSessions(model.getCompletedSessions());
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
        if (view != null) {
            view.showSessionCompleted();
            view.updateCompletedSessions(model.getCompletedSessions());
            view.updateControlButtons(false);
            view.allowScreenOff();
        }
    }
    
    @Override
    public void onSessionTypeChange(boolean isStudySession) {
        if (view != null) {
            view.updateSessionType(isStudySession);
        }
    }
    
    private String formatTime(long timeInMillis) {
        long minutes = timeInMillis / 1000 / 60;
        long seconds = (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

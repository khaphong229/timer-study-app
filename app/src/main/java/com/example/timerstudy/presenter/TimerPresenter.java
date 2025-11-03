package com.example.timerstudy.presenter;

import android.content.Context;

import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.TimerModel;
import com.example.timerstudy.view.contracts.TimerContract;

public class TimerPresenter implements TimerContract.Presenter, TimerModel.TimerListener {

    private TimerContract.View view;
    private TimerModel model;
    public static TimerPresenter instance;

    private Context context;
    private int userId = 1;
    private SessionRepository sessionRepository;
    private int completedSessionsFromDb = 0; // Lưu count từ DB

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
        this.sessionRepository = SessionRepository.getInstance(context);

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
            view.updateCompletedSessions(completedSessionsFromDb); // Hiển thị count từ DB
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
        if (model.isStudySession()) {
            saveSessionCompleted();
        }

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
        return completedSessionsFromDb; // Trả về count từ DB thay vì từ model
    }

    @Override
    public int getStudyDurationMinutes() {
        return (int) (model.getStudyDuration() / 1000 / 60);
    }

    @Override
    public int getBreakDurationMinutes() {
        return (int) (model.getBreakDuration() / 1000 / 60);
    }

    @Override
    public void saveSessionCompleted() {
        if (sessionRepository != null) {
            int studyDuration = getStudyDurationMinutes();
            sessionRepository.saveCompletedStudySession(userId, studyDuration);
            // Count sẽ tự động update qua LiveData observer
        }
    }


    private void loadCompletedSessionsCount() {
        if (sessionRepository != null) {
            sessionRepository.loadCompletedSessionsCountToday(userId);
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

    public void setUserId(int userId) {
        this.userId = userId;
        loadCompletedSessionsCount(); 
    }
}

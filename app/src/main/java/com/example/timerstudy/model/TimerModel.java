package com.example.timerstudy.model;

import android.os.CountDownTimer;

public class TimerModel {

    public interface TimerListener {
        void onTimeUpdate(long timeRemaining);

        void onSessionComplete();

        void onSessionTypeChange(boolean isStudySession);

        void onBreakComplete();
    }

    private static final long DEFAULT_STUDY_DURATION = 25 * 60 * 1000;
    private static final long DEFAULT_BREAK_DURATION = 5 * 60 * 1000;

    private long studyDuration = DEFAULT_STUDY_DURATION;
    private long breakDuration = DEFAULT_BREAK_DURATION;
    private long currentTimeRemaining;
    private boolean isRunning = false;
    private boolean isStudySession = true;

    private CountDownTimer countDownTimer;
    private TimerListener listener;

    public TimerModel() {
        currentTimeRemaining = studyDuration;
    }

    public void setTimerListener(TimerListener listener) {
        this.listener = listener;
    }

    public void startTimer() {
        if (isRunning)
            return;

        isRunning = true;
        countDownTimer = new CountDownTimer(currentTimeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentTimeRemaining = millisUntilFinished;
                if (listener != null) {
                    listener.onTimeUpdate(currentTimeRemaining);
                }
            }

            @Override
            public void onFinish() {
                completeSession();
            }
        };
        countDownTimer.start();
    }

    public void pauseTimer() {
        if (!isRunning)
            return;

        isRunning = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resetTimer() {
        pauseTimer();
        currentTimeRemaining = isStudySession ? studyDuration : breakDuration;
        if (listener != null) {
            listener.onTimeUpdate(currentTimeRemaining);
        }
    }

    private void completeSession() {
        isRunning = false;

        if (isStudySession) {
            isStudySession = false;
            
            if (listener != null) {
                listener.onSessionComplete();
            }

            currentTimeRemaining = isStudySession ? studyDuration : breakDuration;

            if (listener != null) {

                listener.onSessionTypeChange(isStudySession);
                listener.onTimeUpdate(currentTimeRemaining);
            }
            startTimer();

        } else {
            isStudySession = true;
            currentTimeRemaining = studyDuration;

            if (listener != null) {
                listener.onBreakComplete();
                listener.onSessionTypeChange(isStudySession);
                listener.onTimeUpdate(currentTimeRemaining);
            }
        }
    }

    // Getters and Setters
    public long getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(long studyDuration) {
        this.studyDuration = studyDuration;
        if (isStudySession && !isRunning) {
            resetTimer();
        }
    }

    public long getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(long breakDuration) {
        this.breakDuration = breakDuration;
        if (!isStudySession && !isRunning) {
            resetTimer();
        }
    }

    public long getCurrentTimeRemaining() {
        return currentTimeRemaining;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStudySession() {
        return isStudySession;
    }

}

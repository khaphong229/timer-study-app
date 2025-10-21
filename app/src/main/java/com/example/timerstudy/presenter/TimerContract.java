package com.example.timerstudy.presenter;

public interface TimerContract {
    
    interface View {
        void updateTimeDisplay(String timeText);
        void updateSessionType(boolean isStudySession);
        void updateCompletedSessions(int count);
        void showSessionCompleted();
        void updateControlButtons(boolean isRunning);
        void showToast(String message);
    }
    
    interface Presenter {
        void attachView(View view);
        void detachView();
        void onStartClicked();
        void onPauseClicked();
        void onResetClicked();
        void onStudyDurationChanged(int minutes);
        void onBreakDurationChanged(int minutes);
        void onDestroy();
    }
}

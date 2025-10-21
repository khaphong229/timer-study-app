package com.example.timerstudy.mvp;

import com.example.timerstudy.model.TimerItem;
import java.util.List;

public interface ITimerView {
    void showTimerList(List<TimerItem> timers);
    void showTimerCompleted(String timerName);
    void updateTimerDisplay(int position, String timeDisplay);
    void showError(String message);
    void showSuccess(String message);
    void navigateToAddTimer();
    void refreshTimerList();
}

package com.example.timerstudy.mvp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.timerstudy.model.TimerItem;
import com.example.timerstudy.model.TimerModel;
import java.util.List;

public class TimerPresenter {
    private ITimerView view;
    private TimerModel model;
    private Handler handler;
    private Runnable timerRunnable;
    
    public TimerPresenter(ITimerView view, Context context) {
        this.view = view;
        this.model = new TimerModel(context);
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void loadTimers() {
        List<TimerItem> timers = model.getAllTimers();
        view.showTimerList(timers);
    }
    
    public void addTimer(String name, int hours, int minutes, int seconds) {
        try {
            long totalSeconds = hours * 3600 + minutes * 60 + seconds;
            if (totalSeconds <= 0) {
                view.showError("Thời gian phải lớn hơn 0");
                return;
            }
            
            TimerItem timer = new TimerItem(name, totalSeconds);
            model.addTimer(timer);
            view.showSuccess("Đã thêm timer thành công");
            loadTimers();
        } catch (Exception e) {
            view.showError("Lỗi khi thêm timer: " + e.getMessage());
        }
    }
    
    public void startTimer(int position) {
        List<TimerItem> timers = model.getAllTimers();
        if (position >= 0 && position < timers.size()) {
            TimerItem timer = timers.get(position);
            if (!timer.isRunning()) {
                timer.setRunning(true);
                model.updateTimer(timer);
                startTimerCountdown(timer, position);
            }
        }
    }
    
    public void pauseTimer(int position) {
        List<TimerItem> timers = model.getAllTimers();
        if (position >= 0 && position < timers.size()) {
            TimerItem timer = timers.get(position);
            timer.setRunning(false);
            model.updateTimer(timer);
            stopTimerCountdown();
        }
    }
    
    public void resetTimer(int position) {
        List<TimerItem> timers = model.getAllTimers();
        if (position >= 0 && position < timers.size()) {
            TimerItem timer = timers.get(position);
            timer.reset();
            model.updateTimer(timer);
            stopTimerCountdown();
            view.updateTimerDisplay(position, timer.getFormattedTime());
        }
    }
    
    public void deleteTimer(int position) {
        List<TimerItem> timers = model.getAllTimers();
        if (position >= 0 && position < timers.size()) {
            TimerItem timer = timers.get(position);
            model.deleteTimer(timer.getId());
            loadTimers();
        }
    }
    
    private void startTimerCountdown(TimerItem timer, int position) {
        stopTimerCountdown();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timer.isRunning() && timer.getCurrentTime() > 0) {
                    timer.tick();
                    model.updateTimer(timer);
                    view.updateTimerDisplay(position, timer.getFormattedTime());
                    handler.postDelayed(this, 1000);
                } else if (timer.getCurrentTime() <= 0) {
                    timer.setRunning(false);
                    model.updateTimer(timer);
                    view.showTimerCompleted(timer.getName());
                }
            }
        };
        handler.post(timerRunnable);
    }
    
    private void stopTimerCountdown() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
    
    public void onDestroy() {
        stopTimerCountdown();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
    
    public void onAddTimerClicked() {
        view.navigateToAddTimer();
    }
}

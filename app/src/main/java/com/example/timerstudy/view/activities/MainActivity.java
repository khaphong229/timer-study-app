package com.example.timerstudy.view.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.timerstudy.R;
import com.example.timerstudy.presenter.TimerContract;
import com.example.timerstudy.presenter.TimerPresenter;

public class MainActivity extends AppCompatActivity implements TimerContract.View {
    
    private TextView tvTime;
    private TextView tvSessionType;
    private TextView tvCompletedSessions;
    private Button btnStart;
    private Button btnPause;
    private Button btnReset;
    private SeekBar seekBarStudy;
    private SeekBar seekBarBreak;
    private TextView tvStudyDuration;
    private TextView tvBreakDuration;
    
    private TimerPresenter presenter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        
        initializeViews();
        setupPresenter();
        setupListeners();
    }
    
    private void initializeViews() {
        tvTime = findViewById(R.id.tv_time);
        tvSessionType = findViewById(R.id.tv_session_type);
        tvCompletedSessions = findViewById(R.id.tv_completed_sessions);
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnReset = findViewById(R.id.btn_reset);
        seekBarStudy = findViewById(R.id.seekbar_study);
        seekBarBreak = findViewById(R.id.seekbar_break);
        tvStudyDuration = findViewById(R.id.tv_study_duration);
        tvBreakDuration = findViewById(R.id.tv_break_duration);
        
        // Set default values
        seekBarStudy.setMax(60);
        seekBarStudy.setProgress(25);
        seekBarBreak.setMax(30);
        seekBarBreak.setProgress(5);
        
        updateDurationLabels();
    }
    
    private void setupPresenter() {
        presenter = new TimerPresenter();
        presenter.attachView(this);
    }
    
    private void setupListeners() {
        btnStart.setOnClickListener(v -> presenter.onStartClicked());
        btnPause.setOnClickListener(v -> presenter.onPauseClicked());
        btnReset.setOnClickListener(v -> presenter.onResetClicked());
        
        seekBarStudy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress > 0) {
                    updateDurationLabels();
                    presenter.onStudyDurationChanged(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekBarBreak.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress > 0) {
                    updateDurationLabels();
                    presenter.onBreakDurationChanged(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void updateDurationLabels() {
        tvStudyDuration.setText("Study: " + seekBarStudy.getProgress() + " min");
        tvBreakDuration.setText("Break: " + seekBarBreak.getProgress() + " min");
    }
    
    // TimerContract.View implementation
    @Override
    public void updateTimeDisplay(String timeText) {
        runOnUiThread(() -> tvTime.setText(timeText));
    }
    
    @Override
    public void updateSessionType(boolean isStudySession) {
        runOnUiThread(() -> {
            if (isStudySession) {
                tvSessionType.setText("Study Session");
                tvSessionType.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            } else {
                tvSessionType.setText("Break Time");
                tvSessionType.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        });
    }
    
    @Override
    public void updateCompletedSessions(int count) {
        runOnUiThread(() -> tvCompletedSessions.setText("Completed: " + count));
    }
    
    @Override
    public void showSessionCompleted() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                .setTitle("Session Completed!")
                .setMessage("Great job! Take a break or start the next session.")
                .setPositiveButton("OK", null)
                .show();
        });
    }
    
    @Override
    public void updateControlButtons(boolean isRunning) {
        runOnUiThread(() -> {
            btnStart.setEnabled(!isRunning);
            btnPause.setEnabled(isRunning);
        });
    }
    
    @Override
    public void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}

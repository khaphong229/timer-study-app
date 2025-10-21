package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.timerstudy.R;
import com.example.timerstudy.presenter.TimerContract;
import com.example.timerstudy.presenter.TimerPresenter;

public class TimerFragment extends Fragment implements TimerContract.View {
    
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
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupPresenter();
        setupListeners();
    }
    
    private void initializeViews(View view) {
        tvTime = view.findViewById(R.id.tv_time);
        tvSessionType = view.findViewById(R.id.tv_session_type);
        tvCompletedSessions = view.findViewById(R.id.tv_completed_sessions);
        btnStart = view.findViewById(R.id.btn_start);
        btnPause = view.findViewById(R.id.btn_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        seekBarStudy = view.findViewById(R.id.seekbar_study);
        seekBarBreak = view.findViewById(R.id.seekbar_break);
        tvStudyDuration = view.findViewById(R.id.tv_study_duration);
        tvBreakDuration = view.findViewById(R.id.tv_break_duration);
        
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> tvTime.setText(timeText));
        }
    }
    
    @Override
    public void updateSessionType(boolean isStudySession) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isStudySession) {
                    tvSessionType.setText("Study Session");
                    tvSessionType.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
                } else {
                    tvSessionType.setText("Break Time");
                    tvSessionType.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                }
            });
        }
    }
    
    @Override
    public void updateCompletedSessions(int count) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> tvCompletedSessions.setText("Completed: " + count));
        }
    }
    
    @Override
    public void showSessionCompleted() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Session Completed!")
                    .setMessage("Great job! Take a break or start the next session.")
                    .setPositiveButton("OK", null)
                    .show();
            });
        }
    }
    
    @Override
    public void updateControlButtons(boolean isRunning) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                btnStart.setEnabled(!isRunning);
                btnPause.setEnabled(isRunning);
            });
        }
    }
    
    @Override
    public void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}

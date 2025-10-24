package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import pl.droidsonroids.gif.GifImageView;

import com.example.timerstudy.R;
import com.example.timerstudy.presenter.TimerContract;
import com.example.timerstudy.presenter.TimerPresenter;
import com.example.timerstudy.view.activities.MainActivity;

public class TimerFragment extends Fragment implements TimerContract.View {
    
    private TextView tvTime;
    private TextView tvSessionType;
    private TextView tvCompletedSessions;
    private ImageButton btnPlayPause;
    private ImageButton btnPause;
    private ImageButton btnReset;
    private ImageButton btnTimerSettings;
    private SeekBar seekBarStudy;
    private SeekBar seekBarBreak;
    private TextView tvStudyDuration;
    private TextView tvBreakDuration;
    private MaterialCardView cardSeekbarPanel;
    private GifImageView gifImageView;
    
    private TimerPresenter presenter;
    private boolean isRunning = false;
    private boolean isSeekbarVisible = false;
    
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
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnTimerSettings = view.findViewById(R.id.btn_timer_settings);
        cardSeekbarPanel = view.findViewById(R.id.card_seekbar_panel);
        seekBarStudy = view.findViewById(R.id.seekbar_study);
        seekBarBreak = view.findViewById(R.id.seekbar_break);
        tvStudyDuration = view.findViewById(R.id.tv_study_duration);
        tvBreakDuration = view.findViewById(R.id.tv_break_duration);
        gifImageView = view.findViewById(R.id.gifImageView);
        
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
        // Play/Pause button - Ẩn navbar khi bắt đầu timer
        btnPlayPause.setOnClickListener(v -> {
         if (presenter == null) return;
            if (isRunning) {
                presenter.onPauseClicked();
            } else {
                presenter.onStartClicked();
                // Ẩn Navigation Rail khi nhấn Play
                hideNavigationRail();
            }
        });

        btnReset.setOnClickListener(v -> presenter.onResetClicked());
        
        // Toggle seekbar panel when clicking timer settings
        btnTimerSettings.setOnClickListener(v -> toggleSeekbarPanel());
        
        // Click vào GIF background để toggle Navigation Rail
        gifImageView.setOnClickListener(v -> toggleNavigationRail());
        
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
    
    private void toggleSeekbarPanel() {
        isSeekbarVisible = !isSeekbarVisible;
        
        if (isSeekbarVisible) {
            // Hiện panel với animation
            cardSeekbarPanel.setVisibility(View.VISIBLE);
            cardSeekbarPanel.setAlpha(0f);
            cardSeekbarPanel.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null);
        } else {
            // Ẩn panel với animation
            cardSeekbarPanel.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardSeekbarPanel.setVisibility(View.GONE);
                    }
                });
        }
    }
    
    /**
     * Toggle Navigation Rail visibility
     */
    private void toggleNavigationRail() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleNavigationRail();
        }
    }
    
    /**
     * Ẩn Navigation Rail
     */
    private void hideNavigationRail() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideNavigationRail();
        }
    }
    
    /**
     * Hiện Navigation Rail
     */
    private void showNavigationRail() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showNavigationRail();
        }
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
    public void updateControlButtons(boolean running) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                isRunning = running;
          
                btnPlayPause.setImageResource(isRunning ? R.drawable.ic_pause_48 : R.drawable.ic_play_48);
                // btn vẫn luôn clickable; nếu cần disable khi không hợp lệ, xử lý thêm ở đây
                btnPlayPause.setEnabled(true);
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

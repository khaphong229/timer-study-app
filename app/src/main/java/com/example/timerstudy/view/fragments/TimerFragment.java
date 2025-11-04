package com.example.timerstudy.view.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.timerstudy.presenter.ShopPresenter;
import com.example.timerstudy.utils.ViewAnimator;
import com.example.timerstudy.view.contracts.TimerContract;
import com.google.android.material.card.MaterialCardView;
import pl.droidsonroids.gif.GifImageView;

import com.example.timerstudy.R;
import com.example.timerstudy.presenter.TimerPresenter;
import com.example.timerstudy.view.activities.MainActivity;

public class TimerFragment extends Fragment implements TimerContract.View {

    private TextView tvTime;
    private TextView tvSessionType;
    private TextView tvCompletedSessions;
    private ImageButton btnPlayPause;
    private ImageButton btnReset;
    private ImageButton btnTimerSettings;
    private SeekBar seekBarStudy;
    private SeekBar seekBarBreak;
    private TextView tvStudyDuration;
    private TextView tvBreakDuration;
    private MaterialCardView cardSeekbarPanel;
    private MaterialCardView cardTime;
    private View timerBlurBackground;
    private GifImageView gifImageView;

    private TimerPresenter presenter;
    private boolean isRunning = false;
    private boolean isSeekbarVisible = false;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private ShopPresenter shopPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupShopPresenter();
        setupPresenter();
        setupListeners();
        applyBackground();
    }

    private void setupShopPresenter() {
        shopPresenter = ShopPresenter.getInstance();
        shopPresenter.initialize(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (presenter != null) {
            presenter.attachView(this);
        }

        applyBackground();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (presenter != null) {
            presenter.detachView();
        }
    }

    public void applyBackground() {
        int id = shopPresenter.getBackgroundSelectedResourceId();
        gifImageView.setImageResource(id);
    }

    private void initializeViews(View view) {
        tvTime = view.findViewById(R.id.tv_time);
        tvSessionType = view.findViewById(R.id.tv_session_type);
        tvCompletedSessions = view.findViewById(R.id.tv_completed_sessions);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnTimerSettings = view.findViewById(R.id.btn_timer_settings);
        cardSeekbarPanel = view.findViewById(R.id.card_seekbar_panel);
        cardTime = view.findViewById(R.id.card_time);
        timerBlurBackground = view.findViewById(R.id.timer_blur_background);

        seekBarStudy = view.findViewById(R.id.seekbar_study);
        seekBarBreak = view.findViewById(R.id.seekbar_break);
        tvStudyDuration = view.findViewById(R.id.tv_study_duration);
        tvBreakDuration = view.findViewById(R.id.tv_break_duration);
        gifImageView = view.findViewById(R.id.gifImageView);

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.completed_session);
        if (getContext() != null) {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        // Set default values

        seekBarStudy.setMax(60);
        seekBarStudy.setProgress(25);
        seekBarBreak.setMax(30);
        seekBarBreak.setProgress(5);

        updateDurationLabels();

        // Apply blur effect
        applyBlurEffect();
    }

    private void applyBlurEffect() {
        if (cardTime != null) {
            cardTime.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (timerBlurBackground != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            timerBlurBackground.setRenderEffect(
                    RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP));
        }
    }

    private void setupPresenter() {
        presenter = TimerPresenter.getInstance();
        presenter.initialize(requireContext());
        presenter.attachView(this);
    }

    private void setupListeners() {
        // Play/Pause button - Ẩn navbar khi bắt đầu timer
        btnPlayPause.setOnClickListener(v -> {
            if (presenter == null) {
                return;
            }
            ;
            ViewAnimator.animateButtonClick(btnPlayPause);
            if (isRunning) {
                presenter.onPauseClicked();
            } else {
                presenter.onStartClicked();
                // Ẩn Navigation Rail khi nhấn Play
                hideNavigationRail();
            }
        });

        btnReset.setOnClickListener(v -> {
            ViewAnimator.animateButtonClick(btnReset);
            presenter.onResetClicked();
        });

        // Toggle seekbar panel when clicking timer settings
        btnTimerSettings.setOnClickListener(v -> {
            ViewAnimator.animateButtonClick(btnTimerSettings);
            toggleSeekbarPanel();
        });

        gifImageView.setOnClickListener(v -> {
            toggleNavigationRail();
            hideSeekbarPanel();

        });

        seekBarStudy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress > 0) {
                    updateDurationLabels();
                    presenter.onStudyDurationChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateDurationLabels() {
        tvStudyDuration.setText("Study: " + seekBarStudy.getProgress() + " min");
        tvBreakDuration.setText("Break: " + seekBarBreak.getProgress() + " min");
    }

    private void toggleSeekbarPanel() {
        isSeekbarVisible = !isSeekbarVisible;

        if (isSeekbarVisible) {
            showSeekbarPanel();
        } else {
            hideSeekbarPanel();
        }
    }

    private void hideSeekbarPanel() {
        isSeekbarVisible = false;
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

    private void showSeekbarPanel() {
        isSeekbarVisible = true;
        cardSeekbarPanel.setVisibility(View.VISIBLE);
        cardSeekbarPanel.setAlpha(0f);
        cardSeekbarPanel.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null);
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
                } else {
                    tvSessionType.setText("Break Time");
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
            presenter.detachView();
        }
        allowScreenOff();
        showNavigationRail();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void keepScreenOn() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }
    }

    @Override
    public void allowScreenOff() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }
    }

    @Override
    public void playCompletionSound() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.prepare();
                        }
                        mediaPlayer.start();
                    } else {
                        AudioManager audioManager = (AudioManager) requireContext()
                                .getSystemService(Context.AUDIO_SERVICE);
                        if (audioManager != null) {
                            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void vibrateDevice() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (vibrator != null && vibrator.hasVibrator()) {
                    long[] pattern = { 0, 500, 200, 500, 200, 500 };
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    } else {
                        vibrator.vibrate(pattern, -1);
                    }
                }
            });
        }
    }

}

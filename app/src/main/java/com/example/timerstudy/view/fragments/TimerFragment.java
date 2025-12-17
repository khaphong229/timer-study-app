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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.data.repository.TaskRepository;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.view.adapters.TaskAdapter;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.callback.DataCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

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
    private TextView tvCurrentTask;
    private ImageButton btnPlayPause;
    private ImageButton btnReset;
    private ImageButton btnTimerSettings;
    private ImageButton btnTodoList;
    private SeekBar seekBarStudy;
    private SeekBar seekBarBreak;
    private TextView tvStudyDuration;
    private TextView tvBreakDuration;
    private MaterialCardView cardSeekbarPanel;
    private MaterialCardView cardTodoPanel;
    private MaterialCardView cardTime;
    private View timerBlurBackground;
    private GifImageView gifImageView;

    private TimerPresenter presenter;
    private boolean isRunning = false;
    private boolean isSeekbarVisible = false;
    private boolean isTodoVisible = false;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private ShopPresenter shopPresenter;
    
    // Todo list components
    private RecyclerView rvTodoList;
    private TextView tvTaskCount;
    private ImageButton btnAddTask;
    private View layoutEmptyTasks;
    private TaskAdapter todoAdapter;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private List<TaskEntity> currentTasks = new ArrayList<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = "TimerFragment";

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
        setupTaskList(view);
        applyBackground();
        
        // Load todo list automatically when fragment is created
        loadTodayTasks();
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
        tvCurrentTask = view.findViewById(R.id.tv_current_task);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnTimerSettings = view.findViewById(R.id.btn_timer_settings);
        btnTodoList = view.findViewById(R.id.btn_todo_list);
        cardSeekbarPanel = view.findViewById(R.id.card_seekbar_panel);
        cardTodoPanel = view.findViewById(R.id.card_todo_panel);
        cardTime = view.findViewById(R.id.card_time);
        timerBlurBackground = view.findViewById(R.id.timer_blur_background);

        seekBarStudy = view.findViewById(R.id.seekbar_study);
        seekBarBreak = view.findViewById(R.id.seekbar_break);
        tvStudyDuration = view.findViewById(R.id.tv_study_duration);
        tvBreakDuration = view.findViewById(R.id.tv_break_duration);
        gifImageView = view.findViewById(R.id.gifImageView);
        
        // Todo list views
        rvTodoList = view.findViewById(R.id.rv_todo_list);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        btnAddTask = view.findViewById(R.id.btn_add_task);
        layoutEmptyTasks = view.findViewById(R.id.layout_empty_tasks);

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

        // Toggle todo list panel when clicking todo button
        btnTodoList.setOnClickListener(v -> {
            ViewAnimator.animateButtonClick(btnTodoList);
            toggleTodoPanel();
        });
        
        // Add task button
        if (btnAddTask != null) {
            btnAddTask.setOnClickListener(v -> {
                showAddTaskDialog();
            });
        }

        gifImageView.setOnClickListener(v -> {
            toggleNavigationRail();
            hideSeekbarPanel();
            hideTodoPanel();

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

    private void toggleTodoPanel() {
        isTodoVisible = !isTodoVisible;

        if (isTodoVisible) {
            showTodoPanel();
        } else {
            hideTodoPanel();
        }
    }

    private void hideTodoPanel() {
        isTodoVisible = false;
        if (cardTodoPanel != null) {
            cardTodoPanel.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cardTodoPanel.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void showTodoPanel() {
        isTodoVisible = true;
        if (cardTodoPanel != null) {
            cardTodoPanel.setVisibility(View.VISIBLE);
            cardTodoPanel.setAlpha(0f);
            cardTodoPanel.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
            
            // Load today's tasks when panel opens
            loadTodayTasks();
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
    
    // ===== Todo List Methods =====
    
    private void setupTaskList(View view) {
        // Initialize repositories
        taskRepository = new TaskRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        
        try {
            userRepository.initializeUser();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing user", e);
        }
        
        // Setup RecyclerView
        if (rvTodoList != null) {
            rvTodoList.setLayoutManager(new LinearLayoutManager(requireContext()));
            todoAdapter = new TaskAdapter(
                new ArrayList<>(),
                this::onTaskCheckedChanged,
                null, // No delete callback
                null  // No edit callback
            );
            rvTodoList.setAdapter(todoAdapter);
        }
    }
    
    private void loadTodayTasks() {
        String token = null;
        try {
            if (userRepository.getCurrentUser() != null) {
                token = userRepository.getCurrentUser().getAccessToken();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting access token", e);
        }
        
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "No access token available");
            loadTasksFromLocal();
            return;
        }
        
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        Log.d(TAG, "Loading today's tasks from API");
        
        taskRepository.fetchAllTasksFromApi(bearer, new DataCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                mainHandler.post(() -> {
                    Log.d(TAG, "API returned " + (tasks != null ? tasks.size() : 0) + " tasks");
                    List<TaskEntity> todayTasks = filterTodayPendingTasks(tasks);
                    updateTaskList(todayTasks);
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "API error: " + errorMessage);
                mainHandler.post(() -> loadTasksFromLocal());
            }
        });
    }
    
    private void loadTasksFromLocal() {
        int userId = 0;
        try {
            userId = userRepository.getCurrentUserId();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID", e);
        }
        
        Date today = normalizeDate(new Date());
        long todayMillis = today.getTime();
        
        taskRepository.getTasksByUserIdAndDate(userId, todayMillis, new DataCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> tasks) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Local DB returned " + (tasks != null ? tasks.size() : 0) + " tasks");
                    List<TaskEntity> pendingTasks = filterPendingTasks(tasks);
                    updateTaskList(pendingTasks);
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Local DB error: " + errorMessage);
                    updateTaskList(new ArrayList<>());
                });
            }
        });
    }
    
    private List<TaskEntity> filterTodayPendingTasks(List<TaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TaskEntity> filtered = new ArrayList<>();
        Date today = normalizeDate(new Date());
        long todayMillis = today.getTime();
        
        for (TaskEntity task : tasks) {
            if (task.getTaskDate() != null) {
                long taskMillis = normalizeDate(task.getTaskDate()).getTime();
                if (taskMillis == todayMillis && !task.isCompleted()) {
                    filtered.add(task);
                }
            }
        }
        
        // Sort by order_index (ascending)
        filtered.sort((t1, t2) -> Integer.compare(t1.getOrderIndex(), t2.getOrderIndex()));
        
        Log.d(TAG, "Filtered " + filtered.size() + " today's pending tasks from " + tasks.size() + " total");
        return filtered;
    }
    
    private List<TaskEntity> filterPendingTasks(List<TaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TaskEntity> filtered = new ArrayList<>();
        for (TaskEntity task : tasks) {
            if (!task.isCompleted()) {
                filtered.add(task);
            }
        }
        
        // Sort by order_index (ascending)
        filtered.sort((t1, t2) -> Integer.compare(t1.getOrderIndex(), t2.getOrderIndex()));
        
        return filtered;
    }
    
    private void updateTaskList(List<TaskEntity> tasks) {
        currentTasks = tasks;
        
        // Sort by order_index before updating adapter
        if (tasks != null && !tasks.isEmpty()) {
            tasks.sort((t1, t2) -> Integer.compare(t1.getOrderIndex(), t2.getOrderIndex()));
        }
        
        if (todoAdapter != null) {
            todoAdapter.updateTasks(tasks);
        }
        
        // Update task count
        if (tvTaskCount != null) {
            int count = tasks != null ? tasks.size() : 0;
            tvTaskCount.setText(count + " tasks remaining");
        }
        
        // Show/hide empty state
        if (rvTodoList != null && layoutEmptyTasks != null) {
            if (tasks == null || tasks.isEmpty()) {
                rvTodoList.setVisibility(View.GONE);
                layoutEmptyTasks.setVisibility(View.VISIBLE);
            } else {
                rvTodoList.setVisibility(View.VISIBLE);
                layoutEmptyTasks.setVisibility(View.GONE);
            }
        }
        
        // Update current task display in timer
        updateCurrentTaskDisplay(tasks);
    }
    
    private void onTaskCheckedChanged(TaskEntity task, boolean isChecked) {
        if (task == null) return;
        
        // Save old state for revert
        boolean oldState = task.isCompleted();
        Date oldCompletedAt = task.getCompletedAt();
        
        // Update new state
        task.setCompleted(isChecked);
        if (isChecked) {
            task.setCompletedAt(new Date());
            Log.d(TAG, "Marking task as COMPLETED: " + task.getTitle());
        } else {
            task.setCompletedAt(null);
            Log.d(TAG, "Marking task as PENDING: " + task.getTitle());
        }
        
        String token = null;
        try {
            if (userRepository.getCurrentUser() != null) {
                token = userRepository.getCurrentUser().getAccessToken();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting access token", e);
        }
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Missing access token", Toast.LENGTH_SHORT).show();
            task.setCompleted(oldState);
            task.setCompletedAt(oldCompletedAt);
            return;
        }
        
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        taskRepository.updateTaskViaApi(task, bearer, new DataCallback<TaskEntity>() {
            @Override
            public void onSuccess(TaskEntity result) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Task completion updated successfully");
                    // Reload to refresh list
                    loadTodayTasks();
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Failed to update task: " + errorMessage);
                    // Revert state
                    task.setCompleted(oldState);
                    task.setCompletedAt(oldCompletedAt);
                    if (todoAdapter != null) {
                        todoAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private Date normalizeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private void updateCurrentTaskDisplay(List<TaskEntity> tasks) {
        if (tvCurrentTask == null) return;
        
        if (tasks != null && !tasks.isEmpty()) {
            TaskEntity firstTask = tasks.get(0);
            tvCurrentTask.setText(firstTask.getTitle());
            tvCurrentTask.setVisibility(View.VISIBLE);
        } else {
            tvCurrentTask.setVisibility(View.GONE);
        }
    }
    
    private void showAddTaskDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter task title");
        input.setPadding(50, 30, 50, 30);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(input)
            .setPositiveButton("Add", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    addNewTask(title);
                } else {
                    Toast.makeText(requireContext(), "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void addNewTask(String title) {
        String token = null;
        try {
            if (userRepository.getCurrentUser() != null) {
                token = userRepository.getCurrentUser().getAccessToken();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting access token", e);
        }
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Missing access token", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create new task entity
        TaskEntity newTask = new TaskEntity();
        newTask.setUserId(userRepository.getCurrentUserId());
        newTask.setTitle(title);
        newTask.setDescription("");
        newTask.setPriority(TaskEntity.PRIORITY_MEDIUM);
        newTask.setTaskDate(new Date()); // Today
        newTask.setCompleted(false);
        newTask.setOrderIndex(0);
        newTask.setTotalTimeSpent(0);
        newTask.setEstimatedSessions(1);
        newTask.setActualSessions(0);
        
        String bearer = token.startsWith("Bearer ") ? token : ("Bearer " + token);
        
        taskRepository.createTaskViaApi(newTask, bearer, new DataCallback<TaskEntity>() {
            @Override
            public void onSuccess(TaskEntity result) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Task created successfully: " + result.getTitle());
                    Toast.makeText(requireContext(), "Task added", Toast.LENGTH_SHORT).show();
                    // Reload tasks
                    loadTodayTasks();
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Failed to create task: " + errorMessage);
                    Toast.makeText(requireContext(), "Failed to add task: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
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

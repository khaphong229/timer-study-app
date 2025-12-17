package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.presenter.TaskPresenter;
import com.example.timerstudy.view.adapters.MonthAdapter;
import com.example.timerstudy.view.adapters.PriorityAdapter;
import com.example.timerstudy.view.adapters.TaskAdapter;
import com.example.timerstudy.view.contracts.TaskContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment implements TaskContract.View {
    // UI Components
    private RecyclerView rvTasks, rvMonth;
    private TextView tvEmptyState, tvMonthYear, tvTaskCount, tvProgressPercentage;
    private FloatingActionButton fabAddTask;
    private ProgressBar progressBarDaily, progressBarLoading;
    private Spinner spinnerFilterPriority;
    private CheckBox cbShowCompleted;
    private View btnPrevMonth, btnNextMonth, filterLayout;

    private TaskAdapter taskAdapter;
    private MonthAdapter monthAdapter;
    private TaskPresenter presenter;

    private Calendar monthBase;
    private Date selectedDate;

    // Utility
    private Date normalizeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Lifecycle
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectedDate = normalizeDate(new Date());
        presenter = new TaskPresenter(this, requireContext());
        presenter.setSelectedDate(selectedDate);
        // Ánh xạ view
        rvTasks = view.findViewById(R.id.rvTasks);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        View monthHeader = view.findViewById(R.id.layout_month_header);
        rvMonth = monthHeader.findViewById(R.id.rvMonth);
        tvMonthYear = monthHeader.findViewById(R.id.tvMonthYear);
        btnPrevMonth = monthHeader.findViewById(R.id.btnPrevMonth);
        btnNextMonth = monthHeader.findViewById(R.id.btnNextMonth);
        filterLayout = view.findViewById(R.id.layout_filter);
        spinnerFilterPriority = filterLayout.findViewById(R.id.spinnerFilterPriority);
        cbShowCompleted = filterLayout.findViewById(R.id.cbShowCompleted);
        tvTaskCount = filterLayout.findViewById(R.id.tvTaskCount);
        progressBarDaily = filterLayout.findViewById(R.id.progressBarDaily);
        tvProgressPercentage = filterLayout.findViewById(R.id.tvProgressPercentage);
        // Setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>(),
                (task, isChecked) -> presenter.toggleTaskCompletion(task, isChecked),
                this::showDeleteConfirmation,
                this::showEditTaskDialog);
        rvTasks.setAdapter(taskAdapter);
        setupScrollBehavior();
        setupMonthHeader();
        setupFilters();
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        updateAddTaskButtonVisibility();
        presenter.loadTasks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) presenter.onDestroy();
    }

    // Month Header
    private void setupMonthHeader() {
        rvMonth.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        monthBase = Calendar.getInstance();
        monthBase.set(Calendar.DAY_OF_MONTH, 1);
        updateMonthHeader();
        btnPrevMonth.setOnClickListener(v -> {
            monthBase.add(Calendar.MONTH, -1);
            updateMonthHeader();
        });
        btnNextMonth.setOnClickListener(v -> {
            monthBase.add(Calendar.MONTH, 1);
            updateMonthHeader();
        });
    }

    private void updateMonthHeader() {
        List<Date> days = new ArrayList<>();
        Calendar c = (Calendar) monthBase.clone();
        int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            c.set(Calendar.DAY_OF_MONTH, i);
            days.add(c.getTime());
        }
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM, yyyy", Locale.ENGLISH);
        tvMonthYear.setText(monthFmt.format(monthBase.getTime()));
        monthAdapter = new MonthAdapter(days, (selectedDate, position) -> {
            onDateSelected(selectedDate);
            rvMonth.smoothScrollToPosition(position);
        });
        rvMonth.setAdapter(monthAdapter);
        scrollToToday(days);
    }

    private void scrollToToday(List<Date> days) {
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.YEAR) == monthBase.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == monthBase.get(Calendar.MONTH)) {
            int todayPosition = today.get(Calendar.DAY_OF_MONTH) - 1;
            rvMonth.post(() -> rvMonth.scrollToPosition(todayPosition));
        }
    }

    private void onDateSelected(Date date) {
        if (date == null) return;
        selectedDate = normalizeDate(date);
        presenter.setSelectedDate(this.selectedDate);
        updateAddTaskButtonVisibility();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));
        Toast.makeText(requireContext(), "Đã chọn ngày: " + fmt.format(selectedDate), Toast.LENGTH_SHORT).show();
    }

    // Dialogs
    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        EditText etTaskTitle = dialogView.findViewById(R.id.etNewTask);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        EditText etOrderIndex = dialogView.findViewById(R.id.etOrderIndex);
        
        String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
        int[] colors = {R.color.priority_low, R.color.priority_medium, R.color.priority_high};
        PriorityAdapter adapter = new PriorityAdapter(requireContext(), List.of(priorityNames), colors);
        spinnerPriority.setAdapter(adapter);
        spinnerPriority.setSelection(1);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTaskTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        String[] priorityValues = getResources().getStringArray(R.array.priority_values);
                        String priority = priorityValues[spinnerPriority.getSelectedItemPosition()];
                        
                        // Get order index
                        int orderIndex = 0;
                        try {
                            String orderStr = etOrderIndex.getText().toString().trim();
                            if (!orderStr.isEmpty()) {
                                orderIndex = Integer.parseInt(orderStr);
                            }
                        } catch (NumberFormatException e) {
                            orderIndex = 0;
                        }
                        
                        presenter.addTask(title, priority, selectedDate, orderIndex);
                    } else {
                        Toast.makeText(requireContext(), "Please enter task name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditTaskDialog(TaskEntity task) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null);
        EditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
        Spinner spinnerEditPriority = dialogView.findViewById(R.id.spinnerEditPriority);
        EditText etEditOrderIndex = dialogView.findViewById(R.id.etEditOrderIndex);
        
        etEditTitle.setText(task.getTitle());
        etEditOrderIndex.setText(String.valueOf(task.getOrderIndex()));
        
        String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
        int[] colors = {R.color.priority_low, R.color.priority_medium, R.color.priority_high};
        PriorityAdapter adapter = new PriorityAdapter(requireContext(), List.of(priorityNames), colors);
        spinnerEditPriority.setAdapter(adapter);
        String[] priorityValues = getResources().getStringArray(R.array.priority_values);
        for (int i = 0; i < priorityValues.length; i++) {
            if (priorityValues[i].equals(task.getPriority())) {
                spinnerEditPriority.setSelection(i);
                break;
            }
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etEditTitle.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        task.setTitle(newTitle);
                        task.setPriority(priorityValues[spinnerEditPriority.getSelectedItemPosition()]);
                        
                        // Lưu order index
                        try {
                            String orderStr = etEditOrderIndex.getText().toString().trim();
                            if (!orderStr.isEmpty()) {
                                task.setOrderIndex(Integer.parseInt(orderStr));
                            }
                        } catch (NumberFormatException e) {
                            // Giữ nguyên giá trị cũ
                        }
                        
                        presenter.updateTask(task);
                        Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(TaskEntity task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> presenter.deleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Filter
    private void setupFilters() {
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.filter_priority,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterPriority.setAdapter(filterAdapter);
        spinnerFilterPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.setFilter(getCurrentPriorityFilter(), cbShowCompleted.isChecked());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        cbShowCompleted.setChecked(false);
        cbShowCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setFilter(getCurrentPriorityFilter(), isChecked);
        });
    }

    private String getCurrentPriorityFilter() {
        int position = spinnerFilterPriority.getSelectedItemPosition();
        switch (position) {
            case 1: return "low";
            case 2: return "medium";
            case 3: return "high";
            default: return "all";
        }
    }

    // UI Update (TaskContract.View)
    @Override
    public void showTasks(List<TaskEntity> tasks) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> taskAdapter.setTasks(tasks));
        }
    }

    @Override
    public void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvTasks.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        rvTasks.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.VISIBLE);
                    rvTasks.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.GONE);
                }
            });
        }
    }
    
    @Override
    public void hideLoading() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                    rvTasks.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
  
    @Override
    public void showTaskAddedSuccess() {
        Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void updateTaskCount(int completed, int total) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                tvTaskCount.setText(completed + "/" + total);
                int percentage = total > 0 ? (completed * 100) / total : 0;
                if (progressBarDaily != null) progressBarDaily.setProgress(percentage);
                if (tvProgressPercentage != null) tvProgressPercentage.setText(percentage + "%");
            });
        }
    }
    @Override
    public void updateFilteredTasks(List<TaskEntity> filteredTasks) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                taskAdapter.setTasks(filteredTasks);
                boolean isEmpty = filteredTasks.isEmpty();
                tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            });
        }
    }

    // Scroll Behavior
    private void setupScrollBehavior() {
        rvTasks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean isFilterVisible = true;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (filterLayout != null) {
                    if (dy > 10 && isFilterVisible) {
                        isFilterVisible = false;
                        filterLayout.animate()
                                .translationY(-filterLayout.getHeight())
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> filterLayout.setVisibility(View.GONE))
                                .start();
                    } else if (dy < -10 && !isFilterVisible) {
                        isFilterVisible = true;
                        filterLayout.setVisibility(View.VISIBLE);
                        filterLayout.animate()
                                .translationY(0)
                                .alpha(1f)
                                .setDuration(200)
                                .start();
                    }
                }
            }
        });
    }

    private void updateAddTaskButtonVisibility() {
        Date today = normalizeDate(new Date());
        if (fabAddTask != null) {
            fabAddTask.setVisibility(selectedDate.compareTo(today) < 0 ? View.GONE : View.VISIBLE);
        }
    }
}

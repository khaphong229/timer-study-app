package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.data.repository.TaskRepository;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.view.adapters.TaskAdapter;
import com.example.timerstudy.view.adapters.WeekAdapter;
import com.example.timerstudy.view.adapters.PriorityAdapter;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

	private RecyclerView rvTasks;
	private TaskAdapter adapter;
	private TaskRepository repository;
	private UserRepository userRepository;
	private TextView tvEmptyState;
	// week header views
	private RecyclerView rvWeek;
	private TextView tvMonthYear;
	private ImageButton btnPrevWeek, btnNextWeek;
	private WeekAdapter weekAdapter;
	private java.util.Calendar weekBase;
	
	// Add task views
	private EditText etNewTask;
	private ImageButton btnAddTask;
	private Spinner spinnerPriority;
	private PriorityAdapter priorityAdapter;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_task, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Initialize views
		rvTasks = view.findViewById(R.id.rvTasks);
		tvEmptyState = view.findViewById(R.id.tvEmptyState);
		etNewTask = view.findViewById(R.id.etNewTask);
		btnAddTask = view.findViewById(R.id.btnAddTask);
		spinnerPriority = view.findViewById(R.id.spinnerPriority);

		// Setup RecyclerView
		rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
		adapter = new TaskAdapter(new ArrayList<>(), (task, isChecked) -> {
			// toggle completed and update repository
			task.setCompleted(isChecked);
			if (isChecked) task.setCompletedAt(new java.util.Date());
			else task.setCompletedAt(null);
			repository.updateTask(task);
			// no UI changes here because adapter will reflect the same object state
		});

		rvTasks.setAdapter(adapter);
		
		// Setup Priority Spinner
		setupPrioritySpinner();

		// setup week header
		rvWeek = view.findViewById(R.id.rvWeek);
		tvMonthYear = view.findViewById(R.id.tvMonthYear);
		btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
		btnNextWeek = view.findViewById(R.id.btnNextWeek);

		rvWeek.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
		weekBase = java.util.Calendar.getInstance();
		// normalize to start of week (Monday)
		setWeekStartToMonday(weekBase);
		updateWeekHeader();


		btnPrevWeek.setOnClickListener(v -> {
			weekBase.add(java.util.Calendar.DATE, -7);
			updateWeekHeader();
		});

		btnNextWeek.setOnClickListener(v -> {
			weekBase.add(java.util.Calendar.DATE, 7);
			updateWeekHeader();
		});

		repository = new TaskRepository(requireContext());
		userRepository = new UserRepository(requireContext());

		// Đảm bảo user được khởi tạo trước khi thêm task
		initializeUserIfNeeded();

		// Setup add task button listener
		btnAddTask.setOnClickListener(v -> addNewTask());

		loadTasks();
	}

	private void loadTasks(){
		// Load on background thread then post to UI
		new Thread(() -> {
			int currentUserId = userRepository.getCurrentUserId();
			List<TaskEntity> tasks = repository.getTasksByUserId(currentUserId);
			if (tasks == null) tasks = new ArrayList<>();
			List<TaskEntity> finalTasks = tasks;
			requireActivity().runOnUiThread(() -> {
				adapter.setTasks(finalTasks);
				toggleEmptyState(finalTasks.isEmpty());
			});
		}).start();
	}

	private void toggleEmptyState(boolean isEmpty){
		tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
	}

	private void setWeekStartToMonday(java.util.Calendar cal){
		// set to start of current week (Monday)
		int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
		// Java Calendar: Sunday=1 ... Saturday=7; we want Monday=1
		int diff;
		if (dow == java.util.Calendar.SUNDAY) diff = -6; // go back to Monday
		else diff = java.util.Calendar.MONDAY - dow;
		cal.add(java.util.Calendar.DATE, diff);
	}

	private void updateWeekHeader() {
		java.util.List<java.util.Date> days = new java.util.ArrayList<>();
		java.util.Calendar c = (java.util.Calendar) weekBase.clone();
		for (int i = 0; i < 7; i++) {
			days.add(c.getTime());
			c.add(java.util.Calendar.DATE, 1);
		}

		java.text.SimpleDateFormat monthFmt = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
		tvMonthYear.setText(monthFmt.format(days.get(3)));

		weekAdapter = new WeekAdapter(days);
		rvWeek.setAdapter(weekAdapter);

		// 👉 Đảm bảo danh sách ngày nằm giữa màn hình
		rvWeek.post(() -> {
			int itemWidth = getResources().getDimensionPixelSize(R.dimen.item_day_width);
			int totalItemWidth = itemWidth * weekAdapter.getItemCount();
			int screenWidth = rvWeek.getWidth();
			if (totalItemWidth < screenWidth) {
				int padding = (screenWidth - totalItemWidth) / 2;
				rvWeek.setPadding(padding, 0, padding, 0);
			} else {
				rvWeek.setPadding(0, 0, 0, 0);
			}
		});
	}

	private void initializeUserIfNeeded() {
		new Thread(() -> {
			userRepository.initializeUser();
		}).start();
	}

	private void setupPrioritySpinner() {
		// Lấy danh sách priority từ resources
		String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
		String[] priorityValues = getResources().getStringArray(R.array.priority_values);
		int[] priorityColors = {
			R.color.priority_low,
			R.color.priority_medium,
			R.color.priority_high,
			R.color.priority_urgent
		};

		// Tạo adapter cho spinner
		priorityAdapter = new PriorityAdapter(requireContext(), 
			java.util.Arrays.asList(priorityNames), priorityColors);
		
		spinnerPriority.setAdapter(priorityAdapter);
		
		// Đặt mặc định là "Trung bình" (index 1)
		spinnerPriority.setSelection(1);
	}

	private void addNewTask() {
		String taskTitle = etNewTask.getText().toString().trim();
		
		if (taskTitle.isEmpty()) {
			etNewTask.setError("Vui lòng nhập nội dung task");
			return;
		}

		// Lấy priority được chọn
		int selectedPosition = spinnerPriority.getSelectedItemPosition();
		String[] priorityValues = getResources().getStringArray(R.array.priority_values);
		String selectedPriority = priorityValues[selectedPosition];

		// Tạo task mới
		TaskEntity newTask = new TaskEntity();
		newTask.setTitle(taskTitle);
		newTask.setPriority(selectedPriority);
		newTask.setTaskDate(new java.util.Date());
		newTask.setCreatedAt(new java.util.Date());
		newTask.setUpdatedAt(new java.util.Date());
		newTask.setCompleted(false);
		newTask.setTotalTimeSpent(0);
		newTask.setEstimatedSessions(1);
		newTask.setActualSessions(0);
		newTask.setOrderIndex(0);
		
		// Set userId từ UserRepository
		int currentUserId = userRepository.getCurrentUserId();
		newTask.setUserId(currentUserId);

		// Thêm vào database trong background thread
		new Thread(() -> {
			repository.insertTask(newTask);
			
			// Refresh danh sách tasks
			requireActivity().runOnUiThread(() -> {
				etNewTask.setText("");
				spinnerPriority.setSelection(1); // Reset về "Trung bình"
				loadTasks();
			});
		}).start();
	}

}

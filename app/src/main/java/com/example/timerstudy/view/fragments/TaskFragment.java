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
import com.example.timerstudy.view.contracts.TaskContract;
import com.example.timerstudy.data.local.database.entities.TaskEntity;
import com.example.timerstudy.presenter.TaskPresenter;
import com.example.timerstudy.view.adapters.TaskAdapter;
import com.example.timerstudy.view.adapters.WeekAdapter;
import com.example.timerstudy.view.adapters.PriorityAdapter;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment implements TaskContract.View {

	private RecyclerView rvTasks;
	private TaskAdapter adapter;
	private TaskPresenter presenter;
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

		presenter = new TaskPresenter(this, requireContext());

		// Initialize views
		rvTasks = view.findViewById(R.id.rvTasks);
		tvEmptyState = view.findViewById(R.id.tvEmptyState);
		etNewTask = view.findViewById(R.id.etNewTask);
		btnAddTask = view.findViewById(R.id.btnAddTask);
		spinnerPriority = view.findViewById(R.id.spinnerPriority);

		// Setup RecyclerView
		rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
		adapter = new TaskAdapter(new ArrayList<>(), 
			(task, isChecked) -> {
				// Use presenter to handle task completion
				presenter.toggleTaskCompletion(task, isChecked);
			},
			(task) -> {
				// Use presenter to handle task deletion with confirmation
				showDeleteConfirmation(task);
			},
			(task) -> {
				// Handle task editing
				showEditTaskDialog(task);
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

		// Setup add task button listener
		btnAddTask.setOnClickListener(v -> addNewTask());

		presenter.loadTasks();
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



	private void setupPrioritySpinner() {
		// Lấy danh sách priority từ resources
		String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
		String[] priorityValues = getResources().getStringArray(R.array.priority_values);
		int[] priorityColors = {
			R.color.priority_low,
			R.color.priority_medium,
			R.color.priority_high,
		};

		// Tạo adapter cho spinner
		priorityAdapter = new PriorityAdapter(requireContext(), 
			java.util.Arrays.asList(priorityNames), priorityColors);
		
		spinnerPriority.setAdapter(priorityAdapter);
		
		// Đặt mặc định là "Trung bình" (index 1)
		spinnerPriority.setSelection(1);
	}

	private void addNewTask() {
		// Just collect UI data and pass to presenter - NO VALIDATION in View
		String taskTitle = etNewTask.getText().toString().trim();
		int selectedPosition = spinnerPriority.getSelectedItemPosition();
		String[] priorityValues = getResources().getStringArray(R.array.priority_values);
		String selectedPriority = priorityValues[selectedPosition];

		// Let presenter handle validation and business logic
		presenter.addTask(taskTitle, selectedPriority);
	}

	// TaskContract.View implementation
	@Override
	public void showTasks(List<TaskEntity> tasks) {
		adapter.setTasks(tasks);
		toggleEmptyState(tasks.isEmpty());
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
		// You can implement loading indicator here if needed
	}

	@Override
	public void hideLoading() {
		// You can implement loading indicator here if needed
	}

	@Override
	public void showError(String message) {
		android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
	}

	@Override
	public void clearTaskInput() {
		etNewTask.setText("");
	}

	@Override
	public void resetPrioritySelection() {
		spinnerPriority.setSelection(1); // Reset về "Trung bình"
	}

	@Override
	public void showTaskAddedSuccess() {
		android.widget.Toast.makeText(requireContext(), "Đã thêm task thành công", android.widget.Toast.LENGTH_SHORT).show();
	}

	private void showEditTaskDialog(TaskEntity task) {
		// Tạo layout cho dialog edit
		View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null);
		
		EditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
		Spinner spinnerEditPriority = dialogView.findViewById(R.id.spinnerEditPriority);
		
		// Thiết lập giá trị hiện tại
		etEditTitle.setText(task.getTitle());
		
		// Setup priority spinner cho dialog
		String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
		PriorityAdapter editPriorityAdapter = new PriorityAdapter(requireContext(), 
			java.util.Arrays.asList(priorityNames), new int[]{
				R.color.priority_low,
				R.color.priority_medium, 
				R.color.priority_high
			});
		spinnerEditPriority.setAdapter(editPriorityAdapter);
		
		// Đặt priority hiện tại
		String[] priorityValues = getResources().getStringArray(R.array.priority_values);
		for (int i = 0; i < priorityValues.length; i++) {
			if (priorityValues[i].equals(task.getPriority())) {
				spinnerEditPriority.setSelection(i);
				break;
			}
		}
		
		new androidx.appcompat.app.AlertDialog.Builder(requireContext())
			.setTitle("Sửa Task")
			.setView(dialogView)
			.setPositiveButton("Lưu", (dialog, which) -> {
				String newTitle = etEditTitle.getText().toString().trim();
				if (!newTitle.isEmpty()) {
					String newPriority = priorityValues[spinnerEditPriority.getSelectedItemPosition()];
					
					// Cập nhật task
					task.setTitle(newTitle);
					task.setPriority(newPriority);
					
					// Sử dụng presenter để cập nhật
					presenter.updateTask(task);
					
					android.widget.Toast.makeText(requireContext(), "Đã cập nhật task", android.widget.Toast.LENGTH_SHORT).show();
				} else {
					android.widget.Toast.makeText(requireContext(), "Tiêu đề không được để trống", android.widget.Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("Hủy", null)
			.show();
	}

	private void showDeleteConfirmation(TaskEntity task) {
		new androidx.appcompat.app.AlertDialog.Builder(requireContext())
			.setTitle("Xác nhận xóa")
			.setMessage("Bạn có chắc chắn muốn xóa task \"" + task.getTitle() + "\"?")
			.setPositiveButton("Xóa", (dialog, which) -> {
				presenter.deleteTask(task);
				android.widget.Toast.makeText(requireContext(), "Đã xóa task", android.widget.Toast.LENGTH_SHORT).show();
			})
			.setNegativeButton("Hủy", null)
			.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (presenter != null) {
			presenter.onDestroy();
		}
	}
}

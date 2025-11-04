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

	/**
	 * Đưa giờ, phút, giây, millis về 0 để tránh lỗi lệch ngày khi lưu và so sánh
	 */
	private Date normalizeDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private RecyclerView rvTasks, rvMonth;
	private TextView tvEmptyState, tvMonthYear;
	private FloatingActionButton fabAddTask;
	private TaskAdapter taskAdapter;
	private TaskPresenter presenter;
	private MonthAdapter monthAdapter;
	private Calendar monthBase;
	private Date selectedDate;
	private View btnPrevMonth, btnNextMonth;
	private Spinner spinnerFilterPriority;
	private CheckBox cbShowCompleted;
	private TextView tvTaskCount;
	private ProgressBar progressBarDaily;
	private TextView tvProgressPercentage;
	private View filterLayout;

	@Nullable
	@Override // khởi tạo giao diện
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_task, container, false);
	}

	@Override //ánh xạ, gắn sự kiện, kêt nối các adapter
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		selectedDate = normalizeDate(new Date());

		presenter = new TaskPresenter(this, requireContext());
		presenter.setSelectedDate(selectedDate); // Set initial date in presenter

		//  Ánh xạ view
		rvTasks = view.findViewById(R.id.rvTasks);
		tvEmptyState = view.findViewById(R.id.tvEmptyState);
		fabAddTask = view.findViewById(R.id.fabAddTask);

		// Lấy header từ layout include
		View monthHeader = view.findViewById(R.id.layout_month_header);
		rvMonth = monthHeader.findViewById(R.id.rvMonth);
		tvMonthYear = monthHeader.findViewById(R.id.tvMonthYear);
		btnPrevMonth = monthHeader.findViewById(R.id.btnPrevMonth);
		btnNextMonth = monthHeader.findViewById(R.id.btnNextMonth);

		// anh xa filter
		filterLayout = view.findViewById(R.id.layout_filter);
		spinnerFilterPriority = filterLayout.findViewById(R.id.spinnerFilterPriority);
		cbShowCompleted = filterLayout.findViewById(R.id.cbShowCompleted);
		tvTaskCount = filterLayout.findViewById(R.id.tvTaskCount);
		progressBarDaily = filterLayout.findViewById(R.id.progressBarDaily);
		tvProgressPercentage = filterLayout.findViewById(R.id.tvProgressPercentage);

		//  Setup RecyclerView task
		rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
		taskAdapter = new TaskAdapter(new ArrayList<>(),
				(task, isChecked) -> presenter.toggleTaskCompletion(task, isChecked),
				this::showDeleteConfirmation,
				this::showEditTaskDialog);
		rvTasks.setAdapter(taskAdapter);

		// Setup scroll listener để ẩn/hiện filter khi scroll
		setupScrollBehavior();

		//  Setup tháng (header)
		setupMonthHeader();

		setupFilters();

		// FAB thêm task
		fabAddTask.setOnClickListener(v -> showAddTaskDialog());

		//Load dữ liệu ban đầu
		presenter.loadTasks();
	}

	// MONTH HEADER SETUP
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
		};

		SimpleDateFormat monthFmt = new SimpleDateFormat("'Tháng' MM, yyyy", Locale.forLanguageTag("vi-VN"));
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

		// Load tasks cho ngày được chọn
		presenter.setSelectedDate(this.selectedDate);
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));
		Toast.makeText(requireContext(), "Đã chọn ngày: " + fmt.format(selectedDate), Toast.LENGTH_SHORT).show();
	}

	// DIALOG: THÊM TASK MỚI
	private void showAddTaskDialog() {
		View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
		EditText etTaskTitle = dialogView.findViewById(R.id.etNewTask);
		Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);

		// Setup priority spinner
		String[] priorityNames = getResources().getStringArray(R.array.priority_levels);
		int[] colors = {R.color.priority_low, R.color.priority_medium, R.color.priority_high};
		PriorityAdapter adapter = new PriorityAdapter(requireContext(), List.of(priorityNames), colors);
		spinnerPriority.setAdapter(adapter);
		spinnerPriority.setSelection(1);

		new AlertDialog.Builder(requireContext())
				.setTitle("Thêm công việc mới")
				.setView(dialogView)
				.setPositiveButton("Thêm", (dialog, which) -> {
					String title = etTaskTitle.getText().toString().trim();
					if (!title.isEmpty()) {
						String[] priorityValues = getResources().getStringArray(R.array.priority_values);
						String priority = priorityValues[spinnerPriority.getSelectedItemPosition()];
						// Use the current selectedDate from presenter
						presenter.addTask(title, priority, selectedDate);
					} else {
						Toast.makeText(requireContext(), "Vui lòng nhập tên task", Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("Hủy", null)
				.show();
	}

	// ==========================
	// DIALOG: SỬA TASK
	// ==========================
	private void showEditTaskDialog(TaskEntity task) {
		View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null);
		EditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
		Spinner spinnerEditPriority = dialogView.findViewById(R.id.spinnerEditPriority);

		etEditTitle.setText(task.getTitle());
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
				.setTitle("Sửa Task")
				.setView(dialogView)
				.setPositiveButton("Lưu", (dialog, which) -> {
					String newTitle = etEditTitle.getText().toString().trim();
					if (!newTitle.isEmpty()) {
						task.setTitle(newTitle);
						task.setPriority(priorityValues[spinnerEditPriority.getSelectedItemPosition()]);
						presenter.updateTask(task);
						Toast.makeText(requireContext(), "Đã cập nhật task", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(requireContext(), "Tiêu đề không được để trống", Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("Hủy", null)
				.show();
	}

	// ==========================
	// DIALOG: XÓA TASK
	// ==========================
	private void showDeleteConfirmation(TaskEntity task) {
		new AlertDialog.Builder(requireContext())
				.setTitle("Xác nhận xóa")
				.setMessage("Bạn có chắc muốn xóa \"" + task.getTitle() + "\"?")
				.setPositiveButton("Xóa", (dialog, which) -> presenter.deleteTask(task))
				.setNegativeButton("Hủy", null)
				.show();
	}

	// ==========================
	// INTERFACE IMPLEMENTATION
	// ==========================
	@Override
	public void showTasks(List<TaskEntity> tasks) {
		if (isAdded()) {
			requireActivity().runOnUiThread(() -> {
				taskAdapter.setTasks(tasks);
			});
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
	public void showLoading() {}

	@Override
	public void hideLoading() {}

	@Override
	public void showError(String message) {
		Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void clearTaskInput() {}

	@Override
	public void resetPrioritySelection() {}

	@Override
	public void showTaskAddedSuccess() {
		Toast.makeText(requireContext(), "Đã thêm task thành công", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (presenter != null) presenter.onDestroy();
	}

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
				String priorityFilter;
				switch (position) {
					case 1: priorityFilter = "low"; break;
					case 2: priorityFilter = "medium"; break;
					case 3: priorityFilter = "high"; break;
					default: priorityFilter = "all"; break;
				}
				presenter.setFilter(priorityFilter, cbShowCompleted.isChecked());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		// Mặc định hiển thị task chưa hoàn thành
		cbShowCompleted.setChecked(false);
		cbShowCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
			presenter.setFilter(
				getCurrentPriorityFilter(),
				isChecked
			);
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
	@Override
	public void updateTaskCount(int completed, int total) {
		if (isAdded()) {
			requireActivity().runOnUiThread(() -> {
				// Cập nhật task count
				tvTaskCount.setText(completed + "/" + total);

				// Tính phần trăm tiến độ
				int percentage = total > 0 ? (completed * 100) / total : 0;
				
				// Cập nhật progress bar và percentage
				if (progressBarDaily != null) {
					progressBarDaily.setProgress(percentage);
				}
				if (tvProgressPercentage != null) {
					tvProgressPercentage.setText(percentage + "%");
				}

				// Giữ màu gradient đẹp - không thay đổi màu
				// Progress bar đã có gradient riêng trong drawable
			});
		}
	}

	@Override
	public void updateFilteredTasks(List<TaskEntity> filteredTasks) {
		if (isAdded()) {
			requireActivity().runOnUiThread(() -> {
				taskAdapter.setTasks(filteredTasks);
				tvEmptyState.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
				rvTasks.setVisibility(filteredTasks.isEmpty() ? View.GONE : View.VISIBLE);
			});
		}
	}

	/**
	 * Setup scroll behavior để ẩn/hiện filter khi scroll
	 */
	private void setupScrollBehavior() {
		rvTasks.addOnScrollListener(new RecyclerView.OnScrollListener() {
			private boolean isFilterVisible = true;

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				
				if (filterLayout != null) {
					if (dy > 10 && isFilterVisible) {
						// Scroll xuống - ẩn filter
						isFilterVisible = false;
						filterLayout.animate()
								.translationY(-filterLayout.getHeight())
								.alpha(0f)
								.setDuration(200)
								.withEndAction(() -> filterLayout.setVisibility(View.GONE))
								.start();
					} else if (dy < -10 && !isFilterVisible) {
						// Scroll lên - hiện filter
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
}

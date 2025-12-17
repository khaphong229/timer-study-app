package com.example.timerstudy.view.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;
import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskAdapter - Adapter cho danh sách Task
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskEntity> tasks;
    private final OnTaskCheckedListener listener;
    private final OnTaskDeleteListener deleteListener;
    private final OnTaskEditListener editListener;
    private List<TaskEntity> originalTasks;

    // Listener interfaces
    public interface OnTaskCheckedListener { void onCheckedChange(TaskEntity task, boolean isChecked); }
    public interface OnTaskDeleteListener { void onDeleteTask(TaskEntity task); }
    public interface OnTaskEditListener { void onEditTask(TaskEntity task); }

    // Constructor & Data
    public TaskAdapter(List<TaskEntity> tasks, OnTaskCheckedListener listener, OnTaskDeleteListener deleteListener, OnTaskEditListener editListener) {
        this.tasks = tasks;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }
    public void setTasks(List<TaskEntity> newTasks){
        this.tasks = new ArrayList<>(newTasks);
        this.originalTasks = new ArrayList<>(tasks);
        notifyDataSetChanged();
    }
    public void updateTasks(List<TaskEntity> newTasks){
        this.tasks = new ArrayList<>(newTasks);
        notifyDataSetChanged();
    }
    public List<TaskEntity> getOriginalTasks(){ return originalTasks; }
    public List<TaskEntity> getTasks(){ return new ArrayList<>(tasks); }

    // RecyclerView.Adapter
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent,false);
        return new TaskViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = tasks.get(position);
        
        // Hiển thị order index cùng dòng với title
        String titleWithOrder = task.getOrderIndex() + ". " + task.getTitle();
        holder.tvTitle.setText(titleWithOrder);
        
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());
        
        // Mô tả
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        // Ưu tiên
        String priority = task.getPriority();
        int color;
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case TaskEntity.PRIORITY_HIGH: color = Color.parseColor("#F44336"); break;
                case TaskEntity.PRIORITY_MEDIUM: color = Color.parseColor("#FFC107"); break;
                case TaskEntity.PRIORITY_LOW: color = Color.parseColor("#4CAF50"); break;
                default: color = Color.GRAY;
            }
        } else {
            color = Color.GRAY;
        }
        holder.priorityIndicator.setBackgroundColor(color);
        
        // Show/hide action buttons based on listeners
        if (editListener != null && deleteListener != null) {
            holder.actionButtons.setVisibility(View.VISIBLE);
            holder.ivEdit.setOnClickListener(v -> editListener.onEditTask(task));
            holder.ivDelete.setOnClickListener(v -> deleteListener.onDeleteTask(task));
        } else {
            holder.actionButtons.setVisibility(View.GONE);
        }
        
        // Sự kiện checkbox
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onCheckedChange(task, isChecked);
        });
    }
    public int getItemCount(){ return tasks == null ? 0 : tasks.size(); }

    // ViewHolder
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTitle, tvDescription;
        View priorityIndicator;
        android.widget.ImageView ivEdit, ivDelete;
        ViewGroup actionButtons;
        TaskViewHolder(View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            priorityIndicator = itemView.findViewById(R.id.viewPriority);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            actionButtons = itemView.findViewById(R.id.actionButtons);
        }
    }
}

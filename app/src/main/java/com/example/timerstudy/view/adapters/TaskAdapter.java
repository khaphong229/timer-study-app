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

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskEntity> tasks;
    private final OnTaskCheckedListener listener;
    private final OnTaskDeleteListener deleteListener;
    private final OnTaskEditListener editListener;

    private List<TaskEntity> originalTasks;

    public interface OnTaskCheckedListener {
        void onCheckedChange(TaskEntity task, boolean isChecked);
    }

    public interface OnTaskDeleteListener {
        void onDeleteTask(TaskEntity task);
    }

    public interface OnTaskEditListener {
        void onEditTask(TaskEntity task);
    }

    public TaskAdapter(List<TaskEntity> tasks, OnTaskCheckedListener listener, OnTaskDeleteListener deleteListener, OnTaskEditListener editListener) {
        this.tasks = tasks;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setTasks(List<TaskEntity> newTasks){
        this.tasks=new ArrayList<>(newTasks);
        this.originalTasks= new ArrayList<>(tasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent,false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        // avoid triggering listener when programmatically changing checked state
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        String priority = task.getPriority();
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case TaskEntity.PRIORITY_HIGH:
                    holder.priorityIndicator.setBackgroundColor(Color.RED);
                    break;
                case TaskEntity.PRIORITY_MEDIUM:
                    holder.priorityIndicator.setBackgroundColor(Color.parseColor("#FFC107"));
                    break;
                case TaskEntity.PRIORITY_LOW:
                    holder.priorityIndicator.setBackgroundColor(Color.GREEN);
                    break;
                default:
                    holder.priorityIndicator.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.priorityIndicator.setBackgroundColor(Color.GRAY);
        }

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCheckedChange(task, isChecked));
        
        // Handle edit button click
        holder.ivEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditTask(task);
            }
        });
        
        // Handle delete button click
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteTask(task);
            }
        });
    }

    public List<TaskEntity> getOriginalTasks(){
        return originalTasks;
    }

    public List<TaskEntity> getTasks(){
        return new ArrayList<>(tasks);
    }

    @Override
    public int getItemCount(){
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTitle, tvDescription;
        View priorityIndicator;
        android.widget.ImageView ivEdit, ivDelete;

        TaskViewHolder(View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            priorityIndicator = itemView.findViewById(R.id.viewPriority);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

}

package com.example.timerstudy.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.timerstudy.R;

import java.util.List;

public class PriorityAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> priorityNames;
    private int[] priorityColors;

    public PriorityAdapter(Context context, List<String> priorityNames, int[] priorityColors) {
        super(context, R.layout.item_priority_spinner, priorityNames);
        this.context = context;
        this.priorityNames = priorityNames;
        this.priorityColors = priorityColors;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_priority_spinner, parent, false);

        TextView tvPriorityName = view.findViewById(R.id.tvPriorityName);
        View viewPriorityColor = view.findViewById(R.id.viewPriorityColor);

        tvPriorityName.setText(priorityNames.get(position));
        
        // Đặt màu cho priority dot
        if (position < priorityColors.length) {
            viewPriorityColor.setBackgroundColor(context.getResources().getColor(priorityColors[position]));
        }

        return view;
    }
}
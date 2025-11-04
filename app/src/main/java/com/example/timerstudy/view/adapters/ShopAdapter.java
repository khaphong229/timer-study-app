package com.example.timerstudy.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.example.timerstudy.R;
import com.example.timerstudy.model.ShopItem;

import java.util.List;

public class ShopAdapter extends BaseAdapter {
    private Context context;
    private List<ShopItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }

    public ShopAdapter(Context context, List<ShopItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setItems(List<ShopItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void updateItemPurchased(int itemId) {
        for (ShopItem item : items) {
            if (item.getId() == itemId) {
                item.setPurchased(true);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false);
            holder = new ViewHolder();
            holder.ivItemImage = convertView.findViewById(R.id.ivItemImage);
            holder.btnAction = convertView.findViewById(R.id.btnAction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ShopItem item = items.get(position);
        holder.ivItemImage.setImageResource(item.getImageResourceId());

        if (item.isPurchased()) {
            if (item.isSelected()) {
                holder.btnAction.setText("Applied");
                holder.btnAction.setEnabled(false);
            } else {
                holder.btnAction.setText("Apply");
                holder.btnAction.setEnabled(true);
            }
            convertView.setAlpha(1.0f);
        } else {
            holder.btnAction.setText(item.getPrice() + " coins");
            holder.btnAction.setEnabled(true);
            convertView.setAlpha(0.8f);
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivItemImage;
        Button btnAction;
    }

    public void updateSelectedItem(int itemId) {
        for (ShopItem item : items) {
            item.setSelected(item.getId() == itemId && item.isPurchased());
        }
        notifyDataSetChanged();
    }
}
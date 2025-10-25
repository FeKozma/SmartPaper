package com.fekozma.wallpaperchanger.lists.time_labels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.models.TimeLabel;

import java.util.List;

public class TimeLabelAdapter extends RecyclerView.Adapter<TimeLabelViewHolder> {

    private List<TimeLabel> timeLabels;
    private OnTimeLabelClickListener listener;

    public interface OnTimeLabelClickListener {
        void onEditClick(TimeLabel timeLabel, int position);
        void onDeleteClick(TimeLabel timeLabel, int position);
    }

    public TimeLabelAdapter(List<TimeLabel> timeLabels, OnTimeLabelClickListener listener) {
        this.timeLabels = timeLabels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeLabelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_label_item, parent, false);
        return new TimeLabelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeLabelViewHolder holder, int position) {
        TimeLabel timeLabel = timeLabels.get(position);
        holder.bind(timeLabel, listener, position);
    }

    @Override
    public int getItemCount() {
        return timeLabels.size();
    }

    public void updateList(List<TimeLabel> newList) {
        this.timeLabels = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < timeLabels.size()) {
            timeLabels.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateItem(int position, TimeLabel timeLabel) {
        if (position >= 0 && position < timeLabels.size()) {
            timeLabels.set(position, timeLabel);
            notifyItemChanged(position);
        }
    }

    public void addItem(TimeLabel timeLabel) {
        timeLabels.add(timeLabel);
        notifyItemInserted(timeLabels.size() - 1);
    }
}

package com.fekozma.wallpaperchanger.lists.time_labels;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.models.TimeLabel;

public class TimeLabelViewHolder extends RecyclerView.ViewHolder {

    private TextView nameTextView;
    private TextView fromTextView;
    private TextView toTextView;
    private ImageView editButton;
    private ImageView deleteButton;

    public TimeLabelViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.time_label_name);
        fromTextView = itemView.findViewById(R.id.time_label_from);
        toTextView = itemView.findViewById(R.id.time_label_to);
        editButton = itemView.findViewById(R.id.time_label_edit);
        deleteButton = itemView.findViewById(R.id.time_label_delete);
    }

    public void bind(TimeLabel timeLabel, TimeLabelAdapter.OnTimeLabelClickListener listener, int position) {
        nameTextView.setText(timeLabel.getName());
        fromTextView.setText(String.format("%02d:%02d", timeLabel.getStartHour(), timeLabel.getStartMinute()));
        toTextView.setText(String.format("%02d:%02d", timeLabel.getEndHour(), timeLabel.getEndMinute()));

        // Hide delete button for permanent labels
        if (timeLabel.isPermanent()) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
        }

        // Set click listeners
        editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(timeLabel, position);
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (listener != null && !timeLabel.isPermanent()) {
                listener.onDeleteClick(timeLabel, position);
            }
        });
    }
}

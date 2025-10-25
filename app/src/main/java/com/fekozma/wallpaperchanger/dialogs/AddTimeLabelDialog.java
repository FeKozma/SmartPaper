package com.fekozma.wallpaperchanger.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.models.TimeLabel;

public class AddTimeLabelDialog {

    public interface OnTimeLabelCreatedListener {
        void onTimeLabelCreated(String name, int startHour, int startMinute, int endHour, int endMinute);
    }

    public static void show(Context context, TimeLabel existingLabel, OnTimeLabelCreatedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_time_label, null);
        
        EditText nameInput = dialogView.findViewById(R.id.time_label_name_input);
        View timeRangeDisplay = dialogView.findViewById(R.id.time_range_display);
        
        // Store time range values
        final int[] startHour = {6};
        final int[] startMinute = {0};
        final int[] endHour = {12};
        final int[] endMinute = {0};
        
        // If editing existing label, populate fields
        if (existingLabel != null) {
            nameInput.setText(existingLabel.getName());
            startHour[0] = existingLabel.getStartHour();
            startMinute[0] = existingLabel.getStartMinute();
            endHour[0] = existingLabel.getEndHour();
            endMinute[0] = existingLabel.getEndMinute();
        }
        
        // Update time range display
        updateTimeRangeDisplay(timeRangeDisplay, startHour[0], startMinute[0], endHour[0], endMinute[0]);
        
        // Set click listener for time range selection
        timeRangeDisplay.setOnClickListener(v -> {
            TimeRangePickerDialog timePickerDialog = new TimeRangePickerDialog(
                context, 
                "Select Time Range",
                startHour[0], 
                startMinute[0], 
                endHour[0], 
                endMinute[0]
            );
            timePickerDialog.setOnTimeRangeSelectedListener((sh, sm, eh, em) -> {
                startHour[0] = sh;
                startMinute[0] = sm;
                endHour[0] = eh;
                endMinute[0] = em;
                updateTimeRangeDisplay(timeRangeDisplay, sh, sm, eh, em);
            });
            timePickerDialog.show();
        });
        
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(existingLabel != null ? "Edit Time Label" : "Add Time Label")
            .setView(dialogView)
            .setPositiveButton("Save", (d, which) -> {
                String name = nameInput.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {
                    listener.onTimeLabelCreated(name, startHour[0], startMinute[0], endHour[0], endMinute[0]);
                }
            })
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
    }
    
    private static void updateTimeRangeDisplay(View view, int startHour, int startMinute, int endHour, int endMinute) {
        // Find TextViews in the time range display
        android.widget.TextView fromText = view.findViewById(R.id.time_label_from_display);
        android.widget.TextView toText = view.findViewById(R.id.time_label_to_display);
        
        fromText.setText(String.format("%02d:%02d", startHour, startMinute));
        toText.setText(String.format("%02d:%02d", endHour, endMinute));
    }
}

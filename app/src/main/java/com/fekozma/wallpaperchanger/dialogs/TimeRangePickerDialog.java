package com.fekozma.wallpaperchanger.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.views.CircularTimeRangeView;

public class TimeRangePickerDialog extends Dialog {

    private CircularTimeRangeView circularTimeRangeView;
    private TextView timeRangeDisplay;
    private TextView dialogTitle;
    private Button btnOk;
    private Button btnCancel;

    private int startHour;
    private int endHour;
    private String title;

    private OnTimeRangeSelectedListener listener;

    public interface OnTimeRangeSelectedListener {
        void onTimeRangeSelected(int startHour, int endHour);
    }

    public TimeRangePickerDialog(@NonNull Context context, String title, int startHour, int endHour) {
        super(context);
        this.title = title;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_time_range_picker, null);
        setContentView(view);

        // Get window and set background
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        dialogTitle = view.findViewById(R.id.dialog_title);
        timeRangeDisplay = view.findViewById(R.id.time_range_display);
        circularTimeRangeView = view.findViewById(R.id.circular_time_range_view);
        btnOk = view.findViewById(R.id.btn_ok);
        btnCancel = view.findViewById(R.id.btn_cancel);

        dialogTitle.setText(title);

        // Set initial values
        circularTimeRangeView.setStartHour(startHour);
        circularTimeRangeView.setEndHour(endHour);
        updateTimeDisplay(startHour, endHour);

        // Set listener for time range changes
        circularTimeRangeView.setOnTimeRangeChangeListener(new CircularTimeRangeView.OnTimeRangeChangeListener() {
            @Override
            public void onTimeRangeChanged(int startHour, int endHour) {
                updateTimeDisplay(startHour, endHour);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTimeRangeSelected(
                        circularTimeRangeView.getStartHour(),
                        circularTimeRangeView.getEndHour()
                    );
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void updateTimeDisplay(int startHour, int endHour) {
        String display = String.format("%02d:00 - %02d:00", startHour, endHour);
        timeRangeDisplay.setText(display);
    }

    public void setOnTimeRangeSelectedListener(OnTimeRangeSelectedListener listener) {
        this.listener = listener;
    }
}

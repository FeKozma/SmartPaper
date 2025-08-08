package com.fekozma.wallpaperchanger.lists.logs;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.util.ContextUtil;

public class LogItemHolder extends RecyclerView.ViewHolder {
	private final TextView message;
	private final TextView date;

	public LogItemHolder(@NonNull View itemView) {
		super(itemView);
		message = itemView.findViewById(R.id.logs_item_message);
		date = itemView.findViewById(R.id.logs_item_date);
	}

	public void setMessag(CharSequence message) {
		this.message.setText(message);
	}

	public void setDate(CharSequence date) {
		if (!date.isEmpty()) {
			this.date.setText(date.subSequence(0, Math.min(date.length(), 14)));
		}
	}

	public void setLevel(String level) {
		if (level.equals(DBLog.LEVELS.DEBUG.getName()) || level.equals(DBLog.LEVELS.INFO)) {
			itemView.setBackgroundColor(Color.TRANSPARENT);
		} else if (level.equals(DBLog.LEVELS.WARNING.getName())) {
			itemView.setBackgroundColor(ContextUtil.getContext().getColor(R.color.log_warn));
		} else if (level.equals(DBLog.LEVELS.ERROR.getName())) {
			itemView.setBackgroundColor(ContextUtil.getContext().getColor(R.color.log_error));
		}
	}
}

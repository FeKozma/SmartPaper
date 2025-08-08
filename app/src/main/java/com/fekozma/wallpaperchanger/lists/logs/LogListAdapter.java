package com.fekozma.wallpaperchanger.lists.logs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.lists.images.ImageListViewHolder;

import java.util.List;

public class LogListAdapter extends RecyclerView.Adapter<LogItemHolder> {

	List<DBLog> logs;

	public LogListAdapter() {
		logs = DBLog.db.getLogs();
		Log.d("donkey", "donkey kong, file LogListAdapter.java:21, func LogListAdapter");
	}

	@NonNull
	@Override
	public LogItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new LogItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull LogItemHolder holder, int position) {
		holder.setMessag(logs.get(holder.getBindingAdapterPosition()).message);
		holder.setDate(logs.get(holder.getBindingAdapterPosition()).date);
		holder.setLevel(logs.get(holder.getBindingAdapterPosition()).level);
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}
}

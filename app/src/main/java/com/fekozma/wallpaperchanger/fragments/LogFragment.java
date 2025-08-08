package com.fekozma.wallpaperchanger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.LogsBinding;
import com.fekozma.wallpaperchanger.lists.logs.LogListAdapter;

public class LogFragment extends Fragment {

	private LogsBinding binding;

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {

		binding = LogsBinding.inflate(inflater, container, false);
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "-> Logs");
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.logsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		binding.logsList.setAdapter(new LogListAdapter());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}
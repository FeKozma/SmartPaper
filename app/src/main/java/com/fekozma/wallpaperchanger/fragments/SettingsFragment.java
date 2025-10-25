package com.fekozma.wallpaperchanger.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.WallpaperApplication;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.DBTimes;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.databinding.SettingsBinding;
import com.fekozma.wallpaperchanger.dialogs.AddTimeLabelDialog;
import com.fekozma.wallpaperchanger.dialogs.TimeRangePickerDialog;
import com.fekozma.wallpaperchanger.lists.job_category_order.CategoryAdapter;
import com.fekozma.wallpaperchanger.lists.time_labels.TimeLabelAdapter;
import com.fekozma.wallpaperchanger.models.TimeLabel;
import com.fekozma.wallpaperchanger.util.FirebaseLogUtil;
import com.fekozma.wallpaperchanger.util.GestureUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SettingsFragment extends Fragment {

	private SettingsBinding binding;
	private TimeLabelAdapter timeLabelAdapter;
	private List<TimeLabel> timeLabels;

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {

		FirebaseLogUtil.logScreenEvent(this.getClass());
		binding = SettingsBinding.inflate(inflater, container, false);
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "-> Settings");
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setGeneralSettings();
		setTimeSettingS();
		setWeatherSettings();
		setLocationSettings();
		setTagGroupsSettings();
	}

	private void setTimeSettingS() {
		// Load time labels from SharedPreferences or create defaults
		timeLabels = loadTimeLabels();
		
		// Setup RecyclerView
		timeLabelAdapter = new TimeLabelAdapter(timeLabels, new TimeLabelAdapter.OnTimeLabelClickListener() {
			@Override
			public void onEditClick(TimeLabel timeLabel, int position) {
				if (timeLabel.isPermanent()) {
					// For permanent labels, just edit time range
					showTimeRangePickerDialog(
						"Edit " + timeLabel.getName(),
						timeLabel.getStartHour(),
						timeLabel.getStartMinute(),
						timeLabel.getEndHour(),
						timeLabel.getEndMinute(),
						(startHour, startMinute, endHour, endMinute) -> {
							timeLabel.setStartHour(startHour);
							timeLabel.setStartMinute(startMinute);
							timeLabel.setEndHour(endHour);
							timeLabel.setEndMinute(endMinute);
							timeLabelAdapter.updateItem(position, timeLabel);
							saveTimeLabels();
							DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Updated time label: " + timeLabel.getName());
						}
					);
				} else {
					// For custom labels, allow editing name and time range
					AddTimeLabelDialog.show(getContext(), timeLabel, (name, startHour, startMinute, endHour, endMinute) -> {
						// Check for duplicate names (excluding current label)
						if (isNameDuplicate(name, timeLabel.getId())) {
							showDuplicateNameError(name);
							return;
						}
						
						timeLabel.setName(name);
						timeLabel.setStartHour(startHour);
						timeLabel.setStartMinute(startMinute);
						timeLabel.setEndHour(endHour);
						timeLabel.setEndMinute(endMinute);
						timeLabelAdapter.updateItem(position, timeLabel);
						saveTimeLabels();
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Updated custom time label: " + name);
					});
				}
			}

			@Override
			public void onDeleteClick(TimeLabel timeLabel, int position) {
				if (!timeLabel.isPermanent()) {
					AlertDialog deleteDialog = new AlertDialog.Builder(getContext())
						.setTitle("Delete Time Label")
						.setMessage("Are you sure you want to delete '" + timeLabel.getName() + "'?")
						.setPositiveButton("Delete", (d, which) -> {
							timeLabelAdapter.removeItem(position);
							saveTimeLabels();
							DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Deleted time label: " + timeLabel.getName());
						})
						.setNegativeButton("Cancel", null)
						.create();
					deleteDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
					deleteDialog.show();
				}
			}
		});

		binding.timeLabelsRecycler.setLayoutManager(new LinearLayoutManager(getContext()) {
			@Override
			public boolean canScrollVertically() {
				return false;
			}
		});
		binding.timeLabelsRecycler.setAdapter(timeLabelAdapter);

		// Add button click listener
		binding.addTimeLabel.setOnClickListener(v -> {
			AddTimeLabelDialog.show(getContext(), null, (name, startHour, startMinute, endHour, endMinute) -> {
				// Check for duplicate names
				if (isNameDuplicate(name, null)) {
					showDuplicateNameError(name);
					return;
				}
				
				TimeLabel newLabel = new TimeLabel(name, startHour, startMinute, endHour, endMinute, false, UUID.randomUUID().toString());
				timeLabelAdapter.addItem(newLabel);
				saveTimeLabels();
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Added new time label: " + name);
			});
		});
	}

	private List<TimeLabel> loadTimeLabels() {
		// Load time labels from database
		// Default labels are initialized during database creation/upgrade
		return DBTimes.db.getAllTimeLabels();
	}

	private void saveTimeLabels() {
		DBTimes.db.saveAllTimeLabels(timeLabels);
	}
	
	/**
	 * Check if a name already exists in the current time labels list (case-insensitive)
	 * @param name The name to check
	 * @param excludeId ID to exclude from check (for edits), can be null
	 * @return true if duplicate found, false otherwise
	 */
	private boolean isNameDuplicate(String name, String excludeId) {
		for (TimeLabel label : timeLabels) {
			if (excludeId != null && label.getId().equals(excludeId)) {
				continue; // Skip the current label being edited
			}
			if (label.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Show an error dialog when a duplicate name is detected
	 */
	private void showDuplicateNameError(String name) {
		AlertDialog errorDialog = new AlertDialog.Builder(getContext())
			.setTitle("Duplicate Name")
			.setMessage("A time label with the name '" + name + "' already exists. Please choose a different name.")
			.setPositiveButton(android.R.string.ok, null)
			.create();
		errorDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
		errorDialog.show();
	}

	private void showTimeRangePickerDialog(String title, int startHour, int startMinute, int endHour, int endMinute, TimeRangePickerDialog.OnTimeRangeSelectedListener listener) {
		TimeRangePickerDialog dialog = new TimeRangePickerDialog(getContext(), title, startHour, startMinute, endHour, endMinute);
		dialog.setOnTimeRangeSelectedListener(listener);
		dialog.show();
	}

	private void setGeneralSettings() {
		binding.settingsLockscreenSwitch.setChecked(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.ONLY_LOCKSCREEN));
		binding.settingsLockscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Setting only lockscreen to " + isChecked);
				SharedPreferencesUtil.setBoolean(SharedPreferencesUtil.KEYS.ONLY_LOCKSCREEN, isChecked);
			}
		});

		String updateFrequencySelection = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.UPDATE_FREQUENCY);
		String[] updateFrequencyAlternatives = getResources().getStringArray(R.array.update_frequency);
		for (int i = 0; i < updateFrequencyAlternatives.length; i++) {
			if (updateFrequencyAlternatives[i].equals(updateFrequencySelection)) {
				binding.updateFrequency.setSelection(i);
				break;
			}
		}
		binding.updateFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				String selection = getResources().getStringArray(R.array.update_frequency)[i];
				SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.UPDATE_FREQUENCY, selection);

				WallpaperApplication.updateWallpaperJob(selection);

			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
	}

	private void setTagGroupsSettings() {

		binding.tagGroupInfo.setOnClickListener(view -> {
			AlertDialog infoDialog = new AlertDialog.Builder(getContext())
				.setIcon(R.drawable.info_24dp)
				.setTitle("Wallpaper Rules")
				.setMessage("Here you can arrange and toggle the rules that decide your wallpapers. The app checks them in order: the first matching rule is used, then the next, and so on. If a rule is turned off, it will be ignored.")
				.setPositiveButton(android.R.string.ok, (d, i) -> {
				})
				.create();
			infoDialog.show();
			infoDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
		});

		CategoryAdapter adapter = new CategoryAdapter(new ArrayList<>(Arrays.asList(ImageCategories.values())));

		binding.tagGroups.setLayoutManager(new LinearLayoutManager(getContext()) {
			@Override
			public boolean canScrollVertically() {
				return false;
			}
		});
		binding.tagGroups.setAdapter(adapter);

		new ItemTouchHelper(GestureUtil.getSimpleTouchCallback(adapter::onItemMove)).attachToRecyclerView(binding.tagGroups);

	}

	private void setLocationSettings() {
		setLocationRadius();

		binding.settingsLocationRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				String selection = getResources().getStringArray(R.array.dropdown_radius_items)[i];
				selection = selection.substring(0, selection.indexOf(" "));
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Changed radius to '" + selection + "'");
				SharedPreferencesUtil.setInt(SharedPreferencesUtil.KEYS.LOCATION_RADIUS, Integer.valueOf(selection));
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				SharedPreferencesUtil.setInt(SharedPreferencesUtil.KEYS.LOCATION_RADIUS, 5);
			}
		});
	}

	private void setLocationRadius() {
		int radius = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.LOCATION_RADIUS);
		int pos;
		switch (radius) {
			case 5:
				pos = 0;
				break;
			case 10:
				pos = 1;
				break;
			case 20:
				pos = 2;
				break;
			case 50:
				pos = 3;
				break;
			case 100:
				pos = 4;
				break;
			default:
				pos = 0;
		}

		binding.settingsLocationRadius.setSelection(pos);

	}

	private void setWeatherSettings() {
		// Weather
		boolean useGPS = SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.USE_GPS);
		boolean isPermitted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		binding.settingsPhonePositioningSwitch.setChecked(useGPS && isPermitted);

		binding.settingsPhonePositioningSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
				if (!isPermitted && b) {
					AlertDialog dialog = new AlertDialog.Builder(getContext())
						.setTitle("GPS Permission")
						.setMessage("you have not accepted gps positioning for this application, please go to settings and allow these")
						.setPositiveButton(android.R.string.ok, (d, i) -> {
						})
						.create();
					dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
					dialog.show();
					binding.settingsPhonePositioningSwitch.setChecked(false);
				} else if (b) {
					SharedPreferencesUtil.setBoolean(SharedPreferencesUtil.KEYS.USE_GPS, true);
					setLocationSetting();
				} else if (isPermitted && !b) {
					SharedPreferencesUtil.setBoolean(SharedPreferencesUtil.KEYS.USE_GPS, false);
					setLocationSetting();
				}

			}
		});

		setLocationSetting();
	}

	private void setLocationSetting() {
		String lat = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT);
		String lon = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LONG);

		if (lat != null && lon != null) {

			LocationUtil.getLocationName(Double.parseDouble(lat), Double.parseDouble(lon), this::setMapButton);

		}
	}

	private void setMapButton(String location) {
		Activity activity = getActivity();
		if (activity != null && !activity.isFinishing()) {
			if (location.endsWith(" län")) {
				location = location.substring(0, location.length() - 4);
			}
			if (location.length() > 20 && location.contains(",")) {
				location = location.substring(0, location.indexOf(","));
			}
			String finalLocation = location;
			binding.getRoot().post(() -> {
				binding.settingsPhonePositioningMapLocation.setText(finalLocation);

				binding.settingsPhonePositioningMapLocation.setOnClickListener(view -> LocationUtil.showMapDialog(getContext(), () -> {
					setLocationSetting();
				}));
			});
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}

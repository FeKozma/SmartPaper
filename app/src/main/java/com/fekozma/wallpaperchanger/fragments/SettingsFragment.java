package com.fekozma.wallpaperchanger.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.api.HttpClient;
import com.fekozma.wallpaperchanger.api.NominatimService;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.SettingsBinding;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.NetworkUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import org.osmdroid.util.GeoPoint;

import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

	private SettingsBinding binding;

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {

		binding = SettingsBinding.inflate(inflater, container, false);
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "-> Settings");
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// General
		binding.settingsLockscreenSwitch.setChecked(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.ONLY_LOCKSCREEN));
		binding.settingsLockscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Setting only lockscreen to " + b);
				SharedPreferencesUtil.setBoolean(SharedPreferencesUtil.KEYS.ONLY_LOCKSCREEN, b);
			}
		});

		setWeatherSettings();
		setLocationSettings();


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
				location = location.substring(0, location.length()-4);
			}
			if (location.length() > 20 && location.contains(",")) {
				location = location.substring(0, location.indexOf(","));
			}
			String finalLocation = location;
			binding.getRoot().post(() -> {
				binding.settingsPhonePositioningMapLocation.setText(finalLocation);

				binding.settingsPhonePositioningMapLocation.setOnClickListener(view -> LocationUtil.showMapDialog(getContext(), () -> {setLocationSetting();}));
			});
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}
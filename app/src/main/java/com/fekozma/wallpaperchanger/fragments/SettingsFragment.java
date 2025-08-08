package com.fekozma.wallpaperchanger.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.fekozma.wallpaperchanger.MainActivity;
import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.SettingsBinding;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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

			GeoPoint geoPoint = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
			Geocoder geocoder = new Geocoder(ContextUtil.getContext(), Locale.getDefault());

			Executors.newSingleThreadExecutor().execute(() -> {
				geocoder.getFromLocation(
					geoPoint.getLatitude(),
					geoPoint.getLongitude(),
					1,
					addresses -> {
						if (!addresses.isEmpty()) {
							Address address = addresses.get(0);
							String addressText = (address.getLocality() == null ? "" : address.getLocality() + ", ") + address.getAdminArea();

							Activity activity = getActivity();

							if (activity != null && !activity.isFinishing()) {
								binding.getRoot().post(() -> {
									binding.settingsPhonePositioningMapLocation.setText(addressText);

									binding.settingsPhonePositioningMapLocation.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
											LocationUtil.showMapDialog(getContext(), () -> {setLocationSetting();});
										}
									});
								});
							}
						}
					}
				);
				});


		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}
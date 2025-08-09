package com.fekozma.wallpaperchanger.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.api.HttpClient;
import com.fekozma.wallpaperchanger.api.NominatimService;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationUtil {

	static String TAG = LocationUtil.class.getSimpleName();

	public static void showMapDialog(Context context, Runnable onDismiss) {
		showMapDialog(false, context, onDismiss, (lat, lon) -> {
			SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.LOCATION_LAT, lat + "");
			SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.LOCATION_LONG, lon + "");
		});
	}


	public static void showMapDialog(boolean showContinue, Context context, Runnable onDismiss, BiConsumer<Double, Double> onResult) {
		// Load OSMDroid config

		Configuration.getInstance().setUserAgentValue(ContextUtil.getContext().getPackageName());

		LayoutInflater inflater = LayoutInflater.from(context);
		View mapViewLayout = inflater.inflate(com.fekozma.wallpaperchanger.R.layout.map, null);
		MapView mapView = mapViewLayout.findViewById(com.fekozma.wallpaperchanger.R.id.mapView);

		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setMultiTouchControls(true);

		GeoPoint startPoint = LocationUtil.getDefaultCountryGeoPoint();
		mapView.getController().setZoom(5.0);
		mapView.getController().setCenter(startPoint);

		final Marker marker = new Marker(mapView);
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

		String savedLat = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT);
		String savedLon = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LONG);

		if (savedLat != null && savedLon != null) {
			marker.setPosition(new GeoPoint(Double.parseDouble(savedLat), Double.parseDouble(savedLon)));
		}
		mapView.getOverlays().add(marker);

		mapView.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Projection proj = mapView.getProjection();
				GeoPoint tapped = (GeoPoint) proj.fromPixels((int) event.getX(), (int) event.getY());

				marker.setPosition(tapped);
				mapViewLayout.findViewById(com.fekozma.wallpaperchanger.R.id.posiviveButton).setVisibility(View.VISIBLE);
				mapViewLayout.findViewById(com.fekozma.wallpaperchanger.R.id.posiviveButtonBackground).setVisibility(View.VISIBLE);
				mapView.invalidate(); // Refresh marker

				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "user selected: Lat: " + tapped.getLatitude() + ", Lon: " + tapped.getLongitude());
			}
			return false;
		});


		AlertDialog dialog = new AlertDialog.Builder(context)
			.setView(mapViewLayout)
			.setOnDismissListener(dialogInterface -> onDismiss.run())
			.create();

		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


		dialog.show();

		mapViewLayout.findViewById(com.fekozma.wallpaperchanger.R.id.close).setOnClickListener(v -> dialog.dismiss());
		mapViewLayout.findViewById(R.id.posiviveButton).setOnClickListener(v -> {
			IGeoPoint selectedPoint = marker.getPosition();
			double lat;
			double lon;
			if (selectedPoint != null && (lat = selectedPoint.getLatitude()) != 0 && (lon = selectedPoint.getLongitude()) != 0) {

				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "user commited: Lat: " + lat + ", Lon: " + lon);

				onResult.accept(lat, lon);
				dialog.dismiss();
			} else {
				Toast.makeText(v.getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
			}
		});


		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawableResource(android.R.color.transparent);

			DisplayMetrics dm = context.getResources().getDisplayMetrics();


			dialog.getWindow().getDecorView().setPadding((int)(dm.widthPixels * 0.07), 0, (int)(dm.widthPixels * 0.07), 0);
			dialog.getWindow().getDecorView().getLayoutParams().height = (int)(dm.heightPixels * 0.85);
		}
	}

	public static void getCurrentLocation(OnSuccessListener<Location> onSuccessListener) {
		FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(ContextUtil.getContext());

		boolean useGPS = SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.USE_GPS);

		if (ActivityCompat.checkSelfPermission(ContextUtil.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && useGPS) {
			// Request permission in your activity
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Location permission not granted");

			String lat = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT);
			String lon = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LONG);

			if (lat != null && lon != null) {
				Location location = new Location("");
				location.setLatitude(Double.parseDouble(lat));
				location.setLongitude(Double.parseDouble(lon));
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Using cashed location; " + lat + ", " + lon);
				onSuccessListener.onSuccess(location);
			} else  {
				DBLog.db.addLog(DBLog.LEVELS.WARNING, "Location not found");
			}

			return;
		}
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Searching for GPS position");

		fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
			.addOnSuccessListener(new OnSuccessListener<Location>() {
				@Override
				public void onSuccess(Location location) {
					if (location == null) {
						String lat = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT);
						String lon = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LONG);
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Using cashed location; " + lat + ", " + lon);
						if (lat != null && lon != null) {
							location = new Location("");
							location.setLatitude(Double.parseDouble(lat));
							location.setLongitude(Double.parseDouble(lon));
						}
					} else {
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "position retrieved " + location.getLatitude() + ", " + location.getLongitude());
					}

					if (location == null) {
						DBLog.db.addLog(DBLog.LEVELS.WARNING, "Location not found");
					} else {
						SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.LOCATION_LAT, Double.toString(location.getLatitude()));
						SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.LOCATION_LONG, Double.toString(location.getLongitude()));
					}

					onSuccessListener.onSuccess(location);
				}
			})
			.addOnFailureListener(e -> {
				DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error getting location: " + e.getMessage());
			});
	}


	public static void getLocationName(double lat, double lon, Consumer<String> callback) {

			GeoPoint geoPoint = new GeoPoint(lat, lon);
			Geocoder geocoder = new Geocoder(ContextUtil.getContext(), Locale.getDefault());
		Executors.newSingleThreadExecutor().execute(() -> {

			geocoder.getFromLocation(
				geoPoint.getLatitude(),
				geoPoint.getLongitude(),
				1,
				addresses -> {
					if (!addresses.isEmpty()) {
						Address address = addresses.get(0);

						String city = address.getLocality(); // Main city/town
						if (city == null) {
							city = address.getSubAdminArea(); // County or district, often used when no city
						}

						String addressText = (city == null ? "" : city + ", ") + address.getAdminArea();

						callback.accept(addressText);
						getAddressFromAPI(geoPoint.getLatitude(), geoPoint.getLongitude(), callback);
					}
				}
			);
		});
	}

	private static void getAddressFromAPI(double lat, double lon, Consumer<String> callback) {
		if (NetworkUtil.isInternetAvailable(ContextUtil.getContext())) {
			NominatimService adress = HttpClient.getAdressApi();

			adress.reverseGeocode(lat, lon, "json", 1).enqueue(new Callback<NominatimService.NominatimResponse>() {
				@Override
				public void onResponse(Call<NominatimService.NominatimResponse> call, Response<NominatimService.NominatimResponse> response) {
					if (response.isSuccessful() && response.body() != null) {
						NominatimService.NominatimResponse res = response.body();
						callback.accept(res.getGeneralLocation());
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Retrieved adress from api; " + res.getGeneralLocation());
					} else {
						DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not retriev adress from api; " + response.code());
					}
				}

				@Override
				public void onFailure(Call<NominatimService.NominatimResponse> call, Throwable throwable) {
					DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not retriev adress from api; " + throwable.getMessage(), throwable);
				}
			});
		}
	}


	public static GeoPoint getDefaultCountryGeoPoint() {
		return getDefaultLocationForCountry(Locale.getDefault().getCountry().toUpperCase());
	}

	private static GeoPoint getDefaultLocationForCountry(String countryCode) {
		switch (countryCode) {
			case "SE": return new GeoPoint(59.3293, 18.0686); // Sweden - Stockholm
			case "NO": return new GeoPoint(59.9139, 10.7522); // Norway - Oslo
			case "DK": return new GeoPoint(55.6761, 12.5683); // Denmark - Copenhagen
			case "FI": return new GeoPoint(60.1695, 24.9354); // Finland - Helsinki
			case "IS": return new GeoPoint(64.1265, -21.8174); // Iceland - Reykjavik

			case "DE": return new GeoPoint(51.1657, 10.4515); // Germany
			case "FR": return new GeoPoint(46.2276, 2.2137); // France
			case "GB": return new GeoPoint(51.509865, -0.118092); // UK - London
			case "IE": return new GeoPoint(53.3498, -6.2603); // Ireland - Dublin
			case "NL": return new GeoPoint(52.3676, 4.9041); // Netherlands - Amsterdam
			case "BE": return new GeoPoint(50.8503, 4.3517); // Belgium - Brussels
			case "CH": return new GeoPoint(46.8182, 8.2275); // Switzerland
			case "AT": return new GeoPoint(47.5162, 14.5501); // Austria
			case "IT": return new GeoPoint(41.8719, 12.5674); // Italy - Rome
			case "ES": return new GeoPoint(40.4637, -3.7492); // Spain - Madrid
			case "PT": return new GeoPoint(38.7169, -9.1399); // Portugal - Lisbon

			case "PL": return new GeoPoint(51.9194, 19.1451); // Poland
			case "CZ": return new GeoPoint(49.8175, 15.4730); // Czech Republic
			case "SK": return new GeoPoint(48.6690, 19.6990); // Slovakia
			case "HU": return new GeoPoint(47.1625, 19.5033); // Hungary
			case "RO": return new GeoPoint(45.9432, 24.9668); // Romania
			case "BG": return new GeoPoint(42.7339, 25.4858); // Bulgaria
			case "HR": return new GeoPoint(45.1000, 15.2000); // Croatia

			case "GR": return new GeoPoint(37.9838, 23.7275); // Greece - Athens
			case "TR": return new GeoPoint(39.9208, 32.8541); // Turkey - Ankara

			case "US": return new GeoPoint(39.8283, -98.5795); // USA - Center
			case "CA": return new GeoPoint(56.1304, -106.3468); // Canada
			case "MX": return new GeoPoint(23.6345, -102.5528); // Mexico

			case "BR": return new GeoPoint(-14.2350, -51.9253); // Brazil
			case "AR": return new GeoPoint(-38.4161, -63.6167); // Argentina
			case "CL": return new GeoPoint(-35.6751, -71.5430); // Chile

			case "CN": return new GeoPoint(35.8617, 104.1954); // China
			case "JP": return new GeoPoint(36.2048, 138.2529); // Japan
			case "KR": return new GeoPoint(37.5665, 126.9780); // South Korea - Seoul
			case "IN": return new GeoPoint(20.5937, 78.9629); // India

			case "AU": return new GeoPoint(-25.2744, 133.7751); // Australia
			case "NZ": return new GeoPoint(-40.9006, 174.8860); // New Zealand

			case "RU": return new GeoPoint(61.5240, 105.3188); // Russia
			case "UA": return new GeoPoint(48.3794, 31.1656); // Ukraine

			default: return new GeoPoint(0.0, 0.0); // Fallback - Equator
		}
	}
}

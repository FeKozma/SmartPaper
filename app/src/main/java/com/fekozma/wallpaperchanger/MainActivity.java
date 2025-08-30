package com.fekozma.wallpaperchanger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.MainActivityBinding;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;
import com.fekozma.wallpaperchanger.util.ZipUtil;

public class MainActivity extends AppCompatActivity {

	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
	private static final int REQUEST_BACKGROUND_LOCATION = 1002;
	private static boolean forceNavbar = false;
	private static WindowInsetsController windowInsetsController;
	private static SystemBarListener systemBarShowListerner;
	private AppBarConfiguration appBarConfiguration;
	private MainActivityBinding binding;
	private ActivityResultLauncher<Intent> importZipLauncher;

	public static void forceNavbar() {
		forceNavbar = true;
		if (windowInsetsController != null) {
			windowInsetsController.show(WindowInsetsCompat.Type.navigationBars());
		}
	}

	public static void freeNavbar() {
		forceNavbar = false;
	}

	public static void setSystemBarListener(SystemBarListener viewMoved) {
		systemBarShowListerner = viewMoved;
		systemBarShowListerner.newOffset(0);

	}

	private static void systemBarStateChanged(int offset) {
		if (systemBarShowListerner != null) {
			systemBarShowListerner.newOffset(offset);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkBatteryOptimization();
		checkGpsPermissions();

		importZipLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == RESULT_OK && result.getData() != null) {
					Uri zipUri = result.getData().getData();
					ZipUtil.importFromZip(this, zipUri);
				}
			}
		);

		WorkManager.getInstance(ContextUtil.getContext())
			.getWorkInfosForUniqueWorkLiveData(WallpaperApplication.wallpaperWorker)
			.observe(this, workInfos -> {
				for (WorkInfo workInfo : workInfos) {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Background worker state: " + workInfo.getState());
				}
			});

		/*Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", getPackageName(), null);
		intent.setData(uri);
		startActivity(intent);*/

		binding = MainActivityBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.toolbar);

		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
			@Override
			public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, Bundle arguments) {
				binding.appBarLayout.setExpanded(true, true);
			}
		});
		appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
		navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			// Force onPrepareOptionsMenu() to run again
			invalidateOptionsMenu();
		});

		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
		View fragView;
		if (frag != null && (fragView = frag.getView()) != null) {

			ViewCompat.setOnApplyWindowInsetsListener(fragView, new OnApplyWindowInsetsListener() {
				@NonNull
				@Override
				public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
					Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.displayCutout());

					v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
					return insets;
				}
			});
		}

		ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, new OnApplyWindowInsetsListener() {
			@NonNull
			@Override
			public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
				Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.displayCutout());

				v.setPadding(systemBars.left, 0, systemBars.right, 0);
				return insets;
			}
		});

		binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
			int totalScrollRange = appBarLayout.getTotalScrollRange();

			//verticalOffset
			if (forceNavbar || verticalOffset == 0) {
				systemBarStateChanged(verticalOffset);
				// fully extended
				WindowInsetsController insetsController = getWindow().getInsetsController();
				if (insetsController != null) {
					windowInsetsController = insetsController;
					insetsController.show(WindowInsetsCompat.Type.navigationBars());
				}

			} else if (Math.abs(verticalOffset) >= totalScrollRange) {
				// Fully collapsed
				systemBarStateChanged(totalScrollRange);
				binding.toolbar.setVisibility(View.INVISIBLE);
				WindowInsetsController insetsController = getWindow().getInsetsController();
				if (insetsController != null) {
					windowInsetsController = insetsController;
					// Allow swipe gesture to temporarily reveal nav bar
					insetsController.setSystemBarsBehavior(
						WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
					);

					// Hide navigation bar only
					insetsController.hide(WindowInsetsCompat.Type.navigationBars());
				}
			} else {
				// Somewhere in between
				systemBarStateChanged(verticalOffset);
				binding.toolbar.setVisibility(View.VISIBLE);
				Log.d("SCROLL", "Toolbar partially visible: offset=" + verticalOffset);
			}

		});

		NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		;

		// Get the current destination
		NavDestination destination = navController.getCurrentDestination();

		if (destination != null && destination.getId() == R.id.action_settings) {
			// Hide the Settings menu item when we are *already* in Settings
			menu.findItem(R.id.action_settings).setEnabled(false);
		} else {
			// Show it otherwise
			menu.findItem(R.id.action_settings).setEnabled(true);
		}

		if (destination != null && destination.getId() == R.id.action_log) {
			menu.findItem(R.id.action_log).setEnabled(false);
		} else {
			menu.findItem(R.id.action_log).setEnabled(true);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {

			NavController navcontroller = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
			;
			navcontroller.navigate(R.id.action_to_settingsFragment);

			return true;
		} else if (id == R.id.action_export) {
			ZipUtil.exportAppDataAndShare(this);
		} else if (id == R.id.action_import) {

			android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).setTitle("Warning!").setMessage("Importing an existing dataset will remove all local data. Do you want to continue anyway?").setPositiveButton(android.R.string.ok, (d, v) -> {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.setType("*/*");

				intent.addCategory(Intent.CATEGORY_OPENABLE);
				importZipLauncher.launch(intent);

			}).setNegativeButton(android.R.string.cancel, (d, v) -> {
			}).create();
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

			dialog.show();

		} else if (id == R.id.action_log) {
			NavController navcontroller = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
			;
			navcontroller.navigate(R.id.action_to_logFragment);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		return NavigationUI.navigateUp(navController, appBarConfiguration)
			|| super.onSupportNavigateUp();
	}

	private void checkGpsPermissions() {
		if (SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT) != null && SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LONG) != null) {
			return;
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			AlertDialog dialog = new AlertDialog.Builder(this).setTitle("GPS Permission")
				.setMessage("This app uses position data to retrieve weather information. It is either through your phone's tracker GPS (if permission is granted) or from a selected location.\\n\\nYou can change your preference later in Settings.")
				.setPositiveButton("Grant GPS permission", (d, i) -> {
					ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						LOCATION_PERMISSION_REQUEST_CODE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
						ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

						ActivityCompat.requestPermissions(this, new String[]{
							Manifest.permission.ACCESS_BACKGROUND_LOCATION
						}, REQUEST_BACKGROUND_LOCATION);

						AlertDialog dialog2 = new AlertDialog.Builder(this)
							.setTitle("Background location required")
							.setMessage("To keep providing features even when the app isn't visible, we need permission to access your location in the background.")
							.setPositiveButton("Open Settings", (d1, which) -> {
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								Uri uri = Uri.fromParts("package", getPackageName(), null);
								intent.setData(uri);
								startActivity(intent);
							})
							.setNegativeButton("Cancel", null).create();
						dialog2.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

						dialog2.show();
					}

				}).setNeutralButton("Select a location", (d, i) -> {
					LocationUtil.showMapDialog(this, this::checkGpsPermissions);
				}).create();

			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

			dialog.show();

		}
	}

	private void checkBatteryOptimization() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		String packageName = getPackageName();

		if (!pm.isIgnoringBatteryOptimizations(packageName)) {
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Allow the app to run in the background")
				.setMessage("To keep your wallpaper updating automatically, the app needs permission to run without battery restrictions. If battery optimization is on, updates may stop or be delayed.")
				.setPositiveButton(android.R.string.ok, (d, v) -> {
				})
				.setOnDismissListener((d) -> {
					try {
						Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
						intent.setData(Uri.parse("package:" + packageName));
						startActivity(intent);
					} catch (Exception e) {
						// fallback: open battery optimization settings page
						Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
						startActivity(intent);
					}
				}).create();
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

			dialog.show();
		}
	}

	public interface SystemBarListener {
		public void newOffset(int offset);
	}
}
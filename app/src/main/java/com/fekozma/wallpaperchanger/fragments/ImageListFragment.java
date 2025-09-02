package com.fekozma.wallpaperchanger.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fekozma.wallpaperchanger.MainActivity;
import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.ImageListBinding;
import com.fekozma.wallpaperchanger.lists.images.ImageListAdapter;
import com.fekozma.wallpaperchanger.util.FirebaseLogUtil;
import com.fekozma.wallpaperchanger.util.ImageUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImageListFragment extends Fragment {

	private final int[] iconResIds = {
		R.drawable.mobile_intelligence_24dp,
		R.drawable.calendar_month_24dp,
		R.drawable.water_drop_24dp,
		R.drawable.nights_stay_24dp,
		R.drawable.partly_cloudy_day_24dp,
		R.drawable.nest_clock_farsight_analog_24dp,
	};
	private final Handler handler = new Handler(Looper.getMainLooper());
	private ImageListBinding binding;
	private ActivityResultLauncher<Intent> imagePickerLauncher;
	private ImageListAdapter adapter;
	private int currentIndex = 1;

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {

		binding = ImageListBinding.inflate(inflater, container, false);
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);

		int columnWidthDp = 120; // desired column width in dp
		int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
		int columnWidthPx = (int) (columnWidthDp * getResources().getDisplayMetrics().density);
		int numberOfColumns = screenWidthPx / columnWidthPx;

// Calculate remaining space and divide it as padding

		binding.imageList.setClipToPadding(false);
		binding.imageList.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
		binding.imageList.setPadding(40, 0, 40, 0);

		adapter = new ImageListAdapter((nrSelections) -> {
			if (nrSelections == 0) {
				binding.imageListMenu.setVisibility(View.GONE);
				binding.fabAdd.setVisibility(View.VISIBLE);
			} else {
				binding.imageListMenu.setVisibility(View.VISIBLE);
				binding.fabAdd.setVisibility(View.GONE);
				binding.imageListMenuTitle.setText(nrSelections + " selected files");
			}
		});
		binding.imageList.setAdapter(adapter);


		setupFabAdd();
		setupSystemBarListerner();

		setupEditButton();
		setupDeleteButton();
		setupCloseButton();

	}

	@Override
	public void onStart() {
		super.onStart();
		binding.imageListIconPhoneContent.setAlpha(1f);
		setHelpText();
	}

	@Override
	public void onStop() {
		super.onStop();
		handler.removeCallbacks(imageSwitcher); // Stop when not visible
	}	private final Runnable imageSwitcher = new Runnable() {
		@Override
		public void run() {
			binding.imageListIconPhoneContent.animate()
				.alpha(0f)
				.setDuration(300)
				.withEndAction(() -> {
					// Switch image when faded out
					Activity activity = getActivity();
					ImageListBinding tmpBinding = binding;
					if (activity == null || activity.isFinishing() || tmpBinding == null) {
						return;
					}
					tmpBinding.imageListIconPhoneContent.setImageResource(iconResIds[currentIndex]);

					// Fade in
					tmpBinding.imageListIconPhoneContent.animate()
						.alpha(1f)
						.setDuration(300)
						.start();

					// Prepare next index
					currentIndex = (currentIndex + 1) % iconResIds.length;

					// Schedule next run
					handler.postDelayed(imageSwitcher, 5000);
				}).start();
		}
	};

	private void setHelpText() {
		if (adapter.getItemCount() == 0) {
			binding.imageListEmptyList.setVisibility(View.VISIBLE);
			binding.imageListIconPhoneContent.setVisibility(View.VISIBLE);
			binding.imageListIconPhone.setVisibility(View.VISIBLE);
			handler.postDelayed(imageSwitcher, 4000);
		} else {
			binding.imageListEmptyList.setVisibility(View.GONE);
			binding.imageListIconPhoneContent.setVisibility(View.GONE);
			binding.imageListIconPhone.setVisibility(View.GONE);
			handler.removeCallbacks(imageSwitcher);
		}
	}

	private void setupEditButton() {
		binding.imageListMenuEdit.setOnClickListener(view -> {
			Bundle bundle = new Bundle();
			bundle.putParcelableArray(EditTagsFragment.ARG_IMAGES, adapter.getSelected());
			Navigation.findNavController(view).navigate(R.id.edit_tags, bundle);
		});
	}

	private void setupDeleteButton() {
		binding.imageListMenuDelee.setOnClickListener(view -> {
			int imagesSelectedLength = adapter.getSelected().length;

			AlertDialog dialogDeleteImages = new AlertDialog.Builder(getContext())
				.setPositiveButton("Delete images", (d, v) -> {
					if (DBImage.db.deleteImages(adapter.getSelected())) {
						adapter.notifySelectedRemovedImage();
						setHelpText();
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Deleted " + imagesSelectedLength + " images");
					} else {
						DBLog.db.addLog(DBLog.LEVELS.ERROR, "Failed to delete " + imagesSelectedLength + " images");
						Snackbar.make(binding.getRoot(), "Some error occurred while deleting images", Snackbar.LENGTH_LONG).show();
					}
					;
				})
				.setNeutralButton("Delete tags", (d, v) -> {
					DBImage.db.deleteTags(adapter.getSelected());
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Deleted tags for " + imagesSelectedLength + " images");
					adapter.notifySelectedRemovedTags();
				}).create();
			dialogDeleteImages.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
			dialogDeleteImages.show();
		});
	}

	private void setupCloseButton() {
		binding.imageListMenuClose.setOnClickListener(view -> {
			adapter.clearSelections();
		});
	}

	private void setupFabAdd() {
		imagePickerLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					Intent data = result.getData();
					if (data != null && data.getClipData() != null) {
						int count = data.getClipData().getItemCount();

						AtomicBoolean error = new AtomicBoolean(false);
						for (int i = 0; i < count; i++) {
							Uri imageUri = data.getClipData().getItemAt(i).getUri();
							ImageUtil.saveImageToAppstorage(imageUri, binding.getRoot()).ifPresentOrElse(
								(name) -> {
									adapter.notifyItemAdded(DBImage.db.setImage(name, null));
									setHelpText();

								},
								() -> error.set(true));
						}
						if (!error.get()) {
							Snackbar.make(binding.getRoot(), count + " image(s) added.", Snackbar.LENGTH_LONG).show();
						}
					} else if (data != null && data.getData() != null) {
						Uri imageUri = data.getData();
						ImageUtil.saveImageToAppstorage(imageUri, binding.getRoot()).ifPresent(name -> {
							DBImage.db.setImage(name, null);
							Snackbar.make(binding.getRoot(), "1 image added.", Snackbar.LENGTH_LONG).show();
							setHelpText();
						});

					}
					FirebaseLogUtil.logImagesAddEvent();
					binding.imageList.smoothScrollToPosition(0);
				}
			});

		binding.fabAdd.setOnClickListener(fab -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
			if (SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.LOCATION_LAT) == null) {
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Searching for an initial location");
				LocationUtil.getCurrentLocation((loc) -> {
				});
			}
		});
	}

	public void setupSystemBarListerner() {
		MainActivity.setSystemBarListener(offset -> {

			if (binding == null || isRemoving()) {
				return;
			}

			WindowInsets insets = binding.fabAdd.getRootWindowInsets();
			if (insets != null) {
				android.graphics.Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.navigationBars() | WindowInsetsCompat.Type.ime());

				int marginBottom = (systemBars.bottom == 0 ? 100 : systemBars.bottom / 2 + 200);
				((CoordinatorLayout.LayoutParams) binding.imageListMenu.getLayoutParams()).bottomMargin = marginBottom;
				((CoordinatorLayout.LayoutParams) binding.fabAdd.getLayoutParams()).bottomMargin = marginBottom;

			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}




}
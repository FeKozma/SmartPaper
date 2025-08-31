package com.fekozma.wallpaperchanger.lists.tags;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.ImageCategories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EditTagsDialogViewPageAdapter extends FragmentStateAdapter {
	private final DBImage[] images;

	private final List<ImageCategories> categories;

	public EditTagsDialogViewPageAdapter(DBImage[] images, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
		categories = new ArrayList<>(List.of(ImageCategories.values()));
		categories.sort(Comparator.comparingInt(ImageCategories::getStartingPos));
		this.images = images;
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return new EditTagsDialogViewPage(categories.get(position), images);
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public ImageCategories getCategory(int position) {
		return categories.get(position);
	}
}

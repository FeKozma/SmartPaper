package com.fekozma.wallpaperchanger.lists.tags;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.StaticValues;

public class EditTagsDialogViewPageAdapter extends FragmentStateAdapter {
	private final DBImage[] images;

	public EditTagsDialogViewPageAdapter(DBImage[] images, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
		this.images = images;
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return new EditTagsDialogViewPage(StaticValues.values()[position], images);
	}

	@Override
	public int getItemCount() {
		return StaticValues.values().length;
	}
}

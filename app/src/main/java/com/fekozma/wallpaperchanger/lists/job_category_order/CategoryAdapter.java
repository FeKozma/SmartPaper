package com.fekozma.wallpaperchanger.lists.job_category_order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.databinding.ItemCategoryBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

	private final List<ImageCategories> categories;

	public CategoryAdapter(List<ImageCategories> categories) {
		categories.sort(Comparator.comparingInt(ImageCategories::getStartingPos));
		this.categories = categories;
	}

	@NonNull
	@Override
	public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

		return new CategoryViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
		ImageCategories category = categories.get(position);
		holder.name.setText(category.getCategory());
		holder.toggle.setChecked(category.isActive());

		holder.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
			category.setActive(isChecked);
			if (!isChecked && categories.stream().noneMatch(ImageCategories::isActive)) {
				holder.toggle.setChecked(true);
				Snackbar.make(holder.itemView, "At least one category must be active", Snackbar.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public void onItemMove(int fromPosition, int toPosition) {
		if (fromPosition == toPosition) {
			return;
		}
		Collections.swap(categories, fromPosition, toPosition);
		for (int i = Math.min(fromPosition, toPosition); i <= Math.max(fromPosition, toPosition); i++) {
			categories.get(i).setStartingPos(i);
		}
		notifyItemMoved(fromPosition, toPosition);
	}

	static class CategoryViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		Switch toggle;

		public CategoryViewHolder(@NonNull ItemCategoryBinding itemView) {
			super(itemView.getRoot());

			name = itemView.categoryName;
			toggle = itemView.categorySwitch;
		}
	}
}
package com.fekozma.wallpaperchanger.lists.tags;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImagesAndTags;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagsListAdapter extends RecyclerView.Adapter<TagsListHolder> {

	private final FragmentActivity activity;
	private DBImage[] images;
	private List<TagItem> tags;

	public TagsListAdapter(DBImage[] images, FragmentActivity activity) {
		this.activity = activity;
		this.images = images;
		setTags();
	}

	@NonNull
	@Override
	public TagsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TagsListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TagsListHolder holder, int position) {
		if (tags.size() == holder.getBindingAdapterPosition()) {
			holder.setAdd(true, new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					LayoutInflater inflater = LayoutInflater.from(view.getContext());
					View dialogView = inflater.inflate(R.layout.edit_tags_dialog, null);

					TabLayout tablayout = dialogView.findViewById(R.id.edit_tags_dialog_tablayout);
					ViewPager2 viewpager = dialogView.findViewById(R.id.edit_tags_dialog_viewpager2);

					viewpager.setAdapter(new EditTagsDialogViewPageAdapter(images, activity.getSupportFragmentManager(), activity.getLifecycle()));

					tablayout.setBackgroundColor(Color.TRANSPARENT);

					tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
						@Override
						public void onTabSelected(TabLayout.Tab tab) {

						}

						@Override
						public void onTabUnselected(TabLayout.Tab tab) {
							TextView tabTextView = (TextView) tab.view.getChildAt(1); // 0 = icon, 1 = text
							if (tabTextView != null) {
								tabTextView.setTextColor(ContextUtil.getContext().getColor(ImageCategories.values()[tab.getPosition()].getColor())); // Your color here
							}
						}

						@Override
						public void onTabReselected(TabLayout.Tab tab) {
							// Optional: handle reselect if needed
						}
					});

					new TabLayoutMediator(tablayout, viewpager, (tab, position) -> {
						tab.setText(ImageCategories.values()[position].getCategory());

						TextView tabTextView = (TextView) tab.view.getChildAt(1); // 0 = icon, 1 = text
						if (tabTextView != null) {
							tabTextView.setTextColor(ContextUtil.getContext().getColor(ImageCategories.values()[position].getColor())); // Your color here
						}

						//tab.view.setBackgroundColor(ContextUtil.getContext().getColor(ImageCategories.values()[position].getColor()));
					}).attach();

					AlertDialog dialog = new AlertDialog.Builder(view.getContext())
						.setTitle("Add tags")
						.setView(dialogView)
						.setPositiveButton("close", (d, v) -> {
						})
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialogInterface) {
								images = DBImage.db.getImages(images);
								setTags();
								notifyDataSetChanged();
							}
						}).create();
					dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

						dialog.show();
				}
			}, holder.getBindingAdapterPosition() == 0);
		} else {
			TagItem tag = tags.get(holder.getBindingAdapterPosition());

			holder.setTag(tag.tag);
			holder.setBackground(tag.color);
		}
	}

	@Override
	public int getItemCount() {
		return tags.size() + 1;
	}

	private void setTags() {
		tags = new ArrayList<>();
		for (ImageCategories category : ImageCategories.values()) {
			if (category.getCondition() instanceof ConditionalImagesAndTags) {
				ConditionalImagesAndTags conditionalImagesAndTags = (ConditionalImagesAndTags) category.getCondition();
				conditionalImagesAndTags.getTags(Arrays.asList(images)).forEach(tag -> {
					this.tags.add(new TagItem(tag, category.getColor()));
				});
			} else {
				ImageCategories.getCommonSelections(category, this.images).forEach(tag -> {
					this.tags.add(new TagItem(tag.getVissibleName(), category.getColor()));
				});
			}
		}
	}

	private class TagItem {
		String tag;
		int color;
		TagItem(String tag, int color) {
			this.tag = tag;
			this.color = color;
		}
	}
}

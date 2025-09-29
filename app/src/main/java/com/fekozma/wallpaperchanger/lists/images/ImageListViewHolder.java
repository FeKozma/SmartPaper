package com.fekozma.wallpaperchanger.lists.images;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.ImageUtil;


public class ImageListViewHolder extends RecyclerView.ViewHolder {
	private final ImageView image;
	private final View selection;
	private final View bubbleTag;
	private final TextView bubbleTagNr;
	private final View bubbleCat;
	private final TextView bubbleCatNr;

	public ImageListViewHolder(@NonNull View itemView) {
		super(itemView);
		image = itemView.findViewById(R.id.image_item_imageview);
		selection = itemView.findViewById(R.id.image_item_selection);
		bubbleTag = itemView.findViewById(R.id.image_list_item_nr_tags_bubble);
		bubbleTagNr = itemView.findViewById(R.id.image_list_item_nr_tags_text);
		bubbleTag.setVisibility(View.GONE);

		bubbleCat = itemView.findViewById(R.id.image_list_item_nr_categories_bubble);
		bubbleCatNr = itemView.findViewById(R.id.image_list_item_nr_categories_text);
		bubbleCat.setVisibility(View.GONE);
	}

	public void setImage(DBImage dbImage) {

		ImageUtil.getImageFromAppstorage(dbImage).ifPresentOrElse((file) -> {
			Glide.with(ContextUtil.getContext())
				.load(file)
				.centerCrop()
				.into(image);
		}, () -> {
			image.setImageResource(R.drawable.ic_launcher_background);
		});
	}

	public void setSelected(boolean selected) {
		if (selected) {
			selection.setVisibility(View.VISIBLE);
		} else {
			selection.setVisibility(View.GONE);
		}
	}

	public void setDim(int height, int width) {
		image.getLayoutParams().height = height;
		image.getLayoutParams().width = width;
		selection.getLayoutParams().height = height;
		selection.getLayoutParams().width = width;
	}

	public void setNumberTag(int tags) {
		bubbleTag.setVisibility(View.VISIBLE);
		String message;
		if (tags == 0) {
			message = "No tags";
		} else if (tags == 1) {
			message = "1 tag";
		} else {
			message = tags + " tags";
		}

		bubbleTagNr.setText(message);
	}

	public void setNumberCat(int categories) {
		String message;
		if (categories == 0) {
			bubbleCat.setVisibility(View.GONE);
			return;
		} else if (categories == 1) {
			message = "1 category";
		} else {
			message = categories + " categories";
		}
		bubbleCat.setVisibility(View.VISIBLE);

		bubbleCatNr.setText(message);
	}

}

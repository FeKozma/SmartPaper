package com.fekozma.wallpaperchanger.lists.tags;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.StaticValues;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditTagsListAdapter extends RecyclerView.Adapter<TagsListHolder> {

	private final List<String> tags;
	private final StaticValues val;
	private DBImage[] images;

	private int selectedId;

	public EditTagsListAdapter(StaticValues val, DBImage[] images) {
		tags = val.getTags().stream().map(staticTags -> staticTags.getName()).collect(Collectors.toList());
		this.val = val;
		this.images = images;
	}

	@NonNull
	@Override
	public TagsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TagsListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TagsListHolder holder, int position) {
		// The adapter position of the item if it still exists in the adapter. NO_POSITION if item has been removed from the adapter, RecyclerView.Adapter.notifyDataSetChanged() has been called after the last layout pass or the ViewHolder has already been recycled.
		int bindingAdapterPosition = holder.getBindingAdapterPosition();

		long selections = Arrays.stream(images)
			.filter(image -> StaticValues.hasTag(tags.get(bindingAdapterPosition), image))
			.count();

		holder.setTag(tags.get(bindingAdapterPosition));
		holder.setIcon(R.drawable.add_circle_24dp);
		holder.setNumber();
		holder.setBackground(val.getColor());

		if (selections == images.length) {
			holder.setIcon(R.drawable.remove_24dp);
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					images = Arrays.stream(images).peek(image -> {
						DBImage.db.removeImageTag(image, tags.get(bindingAdapterPosition));
					}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);

					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Removed tag: " + tags.get(bindingAdapterPosition) + " from " + images.length + " images");

					onBindViewHolder(holder, bindingAdapterPosition);
				}
			});

		} else {
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					images = Arrays.stream(images).peek(image -> {
							DBImage.db.upsertImageTag(image, tags.get(bindingAdapterPosition));
						}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Added tag: " + tags.get(bindingAdapterPosition) + " on " + images.length + " images");

					onBindViewHolder(holder, bindingAdapterPosition);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return tags.size();
	}
}

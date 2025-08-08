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

	private List<String> tags;
	private StaticValues val;
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
		int i = holder.getBindingAdapterPosition();

		long selections = Arrays.stream(images)
			.filter(image -> StaticValues.hasTag(tags.get(i), image))
			.count();

		holder.setTag(tags.get(i));
		holder.setIcon(R.drawable.add_circle_24dp);
		holder.setNumber();
		holder.setBackground(val.getColor());

		if (selections == images.length) {
			holder.setIcon(R.drawable.remove_24dp);
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					images = Arrays.stream(images).peek(image -> {
						DBImage.db.removeImageTag(image, tags.get(i));
					}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);

					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Removed tag: " + tags.get(i) + " from " + images.length + " images");

					onBindViewHolder(holder, i);
				}
			});

		} else {
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					images = Arrays.stream(images).peek(image -> {
							DBImage.db.upsertImageTag(image, tags.get(i));
						}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Added tag: " + tags.get(i) + " on " + images.length + " images");

					onBindViewHolder(holder, i);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return tags.size();
	}
}

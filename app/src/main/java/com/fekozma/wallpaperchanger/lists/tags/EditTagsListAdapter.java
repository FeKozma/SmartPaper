package com.fekozma.wallpaperchanger.lists.tags;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.MainActivity;
import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.StaticValues;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImagesAndTags;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditTagsListAdapter extends RecyclerView.Adapter<TagsListHolder> {

	private List<String> tags;
	private StaticValues val;
	private DBImage[] images;

	private int selectedId;
	private Context context;

	public EditTagsListAdapter(StaticValues val, DBImage[] images) {
		this.tags = val.getTags(images);
		this.val = val;
		this.images = images;
	}

	@NonNull
	@Override
	public TagsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		context = parent.getContext();
		return new TagsListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TagsListHolder holder, int position) {
		int i = holder.getBindingAdapterPosition();
		if (i == tags.size()) {
			holder.setTag("Add " + val.name().toLowerCase());
			holder.setNumber();
			holder.setBackground(val.getColor());
			holder.setIcon(R.drawable.edit_24dp);

			holder.onClicklistener(view ->
				((ConditionalImagesAndTags)val.getCondition()).edit(context, images, (tags) -> {
					EditTagsListAdapter.this.tags = tags;
					holder.itemView.post(() -> {
						notifyDataSetChanged();
					});
			}));

			return;
		}
		String tag = tags.get(i);

		long selections = Arrays.stream(images)
			.filter(image -> StaticValues.hasTag(tags.get(i), image))
			.count();

		holder.setTag(tag);
		holder.setIcon(R.drawable.add_circle_24dp);
		holder.setNumber();
		holder.setBackground(val.getColor());

		if (val.getCondition() instanceof ConditionalImagesAndTags ) {
			ConditionalImagesAndTags conditionalImagesAndTags = (ConditionalImagesAndTags) val.getCondition();
			conditionalImagesAndTags.setHolder(images, tags.get(i), holder, () -> {
				int rmIndex = tags.indexOf(tag);
				tags.remove(rmIndex);
				notifyItemRemoved(rmIndex);
			});
		} else if (selections == images.length) {
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
		return
			(val.getCondition() instanceof ConditionalImagesAndTags ? 1 : 0 ) + tags.size();
	}
}

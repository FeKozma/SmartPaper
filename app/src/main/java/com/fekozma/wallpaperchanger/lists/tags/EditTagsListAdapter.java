package com.fekozma.wallpaperchanger.lists.tags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.database.ImageStaticTags;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImagesAndTags;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditTagsListAdapter extends RecyclerView.Adapter<TagsListHolder> {

	private List<String> tags;
	private DBImage[] images;
	private ImageCategories category;

	private int selectedId;
	private Context context;

	public EditTagsListAdapter(ImageCategories category, DBImage[] images) {
		this.tags = category.getTags(images);
		this.category = category;
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
		int pos = holder.getBindingAdapterPosition();
		if (pos == tags.size()) {
			holder.setTag("Add " + category.name().toLowerCase());
			holder.setNumber();
			holder.setBackground(category.getColor());
			holder.setIcon(R.drawable.edit_24dp);

			holder.onClicklistener(view ->
				((ConditionalImagesAndTags) category.getCondition()).edit(context, images, (tags) -> {
					EditTagsListAdapter.this.tags = tags;
					holder.itemView.post(() -> {
						notifyDataSetChanged();
					});
			}));

			return;
		}

		TagItem tag = new TagItem();
		try {
			ImageStaticTags tmpTag = ImageStaticTags.valueOf(tags.get(pos));
			tag.internalName = tmpTag.getInternalName();
			tag.visibleName = tmpTag.getVissibleName();
		} catch (IllegalArgumentException e) {
			tag.internalName = tags.get(pos);
			tag.visibleName = tags.get(pos);
		}

		long selections = Arrays.stream(images)
			.filter(image -> ImageCategories.hasTag(tags.get(pos), image))
			.count();

		holder.setTag(tag.visibleName);
		holder.setIcon(R.drawable.add_circle_24dp);
		holder.setNumber();
		holder.setBackground(category.getColor());

		if (category.getCondition() instanceof ConditionalImagesAndTags ) {
			ConditionalImagesAndTags conditionalImagesAndTags = (ConditionalImagesAndTags) category.getCondition();
			conditionalImagesAndTags.setHolder(images, tags.get(pos), holder, () -> {
				int rmIndex = tags.indexOf(tag.internalName);
				tags.remove(rmIndex);
				notifyItemRemoved(rmIndex);
			});
		} else if (selections == images.length) {
			holder.setIcon(R.drawable.remove_24dp);
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					images = Arrays.stream(images).peek(image -> {
						DBImage.db.removeImageTag(image, tags.get(pos));
					}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);

					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Removed tag: " + tags.get(pos) + " from " + images.length + " images");

					onBindViewHolder(holder, pos);
				}
			});

		} else {
			holder.onClicklistener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					images = Arrays.stream(images).peek(image -> {
							DBImage.db.upsertImageTag(image, tags.get(pos));
						}).map(image -> DBImage.db.getImageByName(image.image))
						.collect(Collectors.toList()).toArray(new DBImage[0]);
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Added tag: " + tags.get(pos) + " on " + images.length + " images");

					onBindViewHolder(holder, pos);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return
			(category.getCondition() instanceof ConditionalImagesAndTags ? 1 : 0 ) + tags.size();
	}

	private static class TagItem {
		public String internalName;
		public String visibleName;

	}
}

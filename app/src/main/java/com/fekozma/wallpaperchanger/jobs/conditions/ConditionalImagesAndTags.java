package com.fekozma.wallpaperchanger.jobs.conditions;

import android.content.Context;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.lists.tags.TagsListHolder;

import java.util.List;
import java.util.function.Consumer;

public abstract class ConditionalImagesAndTags extends ConditionalImages {

	public abstract List<String> getTags(List<DBImage> image);

	public abstract void edit(Context context, DBImage[] images, Consumer<List<String>> onTagsChanged);

	public abstract void setHolder(DBImage[] images, String address, TagsListHolder holder, Runnable onRemove);
}

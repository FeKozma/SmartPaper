package com.fekozma.wallpaperchanger.lists.images;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;

import java.util.List;

public class ImageTagEditorListAdapter extends RecyclerView.Adapter<ImageListViewHolder> {

	List<DBImage> images;
	//List<DBImage> images;

	public ImageTagEditorListAdapter(List<DBImage> images) {
		this.images = images;
	}

	@NonNull
	@Override
	public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ImageListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
		holder.setImage(images.get(position));
		holder.setDim(300 * 3, 150 * 3);
		holder.setSelected(false);
	}


	@Override
	public int getItemCount() {
		return images.size();
	}

}

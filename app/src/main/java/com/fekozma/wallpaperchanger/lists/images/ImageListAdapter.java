package com.fekozma.wallpaperchanger.lists.images;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.StaticValues;
import com.fekozma.wallpaperchanger.databinding.ImageListDialogBinding;
import com.fekozma.wallpaperchanger.lists.tags.TagsListAdapter;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.ImageUtil;
import com.google.android.flexbox.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListViewHolder> {

	List<ListImageItem> images;
	Consumer<Integer> onSelectionChange;
	private ImageListDialogBinding ImageListDialogBinding;
	//List<DBImage> images;

	public ImageListAdapter(Consumer<Integer> onSelectionChange) {
		this.onSelectionChange = onSelectionChange;
		images = DBImage.db.getImages().stream().map(img -> new ListImageItem(img)).collect(Collectors.toList());
	}

	@NonNull
	@Override
	public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ImageListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
		ListImageItem item = images.get(holder.getBindingAdapterPosition());
		holder.setImage(item.image);
		holder.setSelected(item.selected);

		long count = 0;
		for (StaticValues val : StaticValues.values()) {
			if (!StaticValues.getSelections(val, item.image).isEmpty()) {
				count++;
			}
		}

		holder.setNumberTag(item.image.tags.length);
		holder.setNumberCat((int)count);
		holder.itemView.setOnClickListener(v -> {
			if (getSelected().length == 0) {
				viewImage(v, holder.getBindingAdapterPosition());
			} else {
				item.selected = !item.selected;
				notifyItemChanged(holder.getBindingAdapterPosition());
				onSelectionChange.accept(nrSelections());
			}
		});

		holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				item.selected = !item.selected;
				notifyItemChanged(holder.getBindingAdapterPosition());
				onSelectionChange.accept(nrSelections());
				return false;
			}
		});
	}

	private boolean viewImage(View view, int i) {
		ImageListDialogBinding binding = com.fekozma.wallpaperchanger.databinding.ImageListDialogBinding.inflate(LayoutInflater.from(view.getContext()));


		FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(view.getContext());

		layoutManager.setFlexDirection(FlexDirection.ROW);
		layoutManager.setFlexWrap(FlexWrap.WRAP);
		layoutManager.setAlignItems(AlignItems.STRETCH);
		layoutManager.setJustifyContent(JustifyContent.FLEX_START);

		// Set up RecyclerView
		binding.imageItemDialogList.setLayoutManager(layoutManager);

		AlertDialog dialog = new AlertDialog.Builder(view.getContext())
			.setView(binding.getRoot())
			.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					images.get(i).image = DBImage.db.getImageByName(images.get(i).image.image);
					notifyItemChanged(i);
				}
			})
			.create();
		setImageDialogContent(view, binding, i, dialog);

		dialog.show();
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


		return false;
	}

	private AlertDialog setImageDialogContent(View view, ImageListDialogBinding binding, int i, AlertDialog dialog) {
		ImageUtil.getImageFromAppstorage(images.get(i).image).ifPresentOrElse((file) -> {
			Glide.with(ContextUtil.getContext())
				.load(file)
				.centerCrop()
				.into(binding.imageItemDialogImage);
		}, () -> {
			binding.imageItemDialogImage.setImageResource(R.drawable.ic_launcher_background);
		});
		binding.imageItemDialogImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

		TagsListAdapter tagsListAdapter = new TagsListAdapter(new DBImage[]{images.get(i).image}, (FragmentActivity) view.getContext());

		binding.imageItemDialogList.setAdapter(tagsListAdapter);

		binding.menu.setOnClickListener(view1 -> {
			PopupMenu popupMenu = new PopupMenu(view1.getContext(), view1);
			popupMenu.getMenuInflater().inflate(R.menu.image_dialog_menu, popupMenu.getMenu());

			popupMenu.setOnMenuItemClickListener(item -> {
				int itemId = item.getItemId();
				if (itemId == R.id.menu_remove_image) {
					DBImage.db.deleteImages(new DBImage[]{images.get(i).image});
					images.remove(i);
					notifyItemRemoved(i);
					if (i == images.size()) {
						if (i == 0) {
							dialog.dismiss();
						} else {
							binding.previous.callOnClick();
						}

					} else {
						binding.next.callOnClick();
					}
					return true;
				} else if (itemId == R.id.menu_remove_tags) {
					DBImage.db.deleteTags(new DBImage[]{images.get(i).image});
					images.get(i).image.tags = new String[0];
					notifyItemChanged(i);
					binding.imageItemDialogList.setAdapter(new TagsListAdapter(new DBImage[]{images.get(i).image}, (FragmentActivity) view.getContext()));
					return true;
				}
				return false;
			});

			popupMenu.show();
		});

		binding.close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});

		if (i == images.size()-1) {
			binding.previous.setVisibility(View.GONE);
		} else {
			binding.previous.setVisibility(View.VISIBLE);
			binding.next.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					images.get(i).image = DBImage.db.getImageByName(images.get(i).image.image);
					notifyItemChanged(i);
					setImageDialogContent(view, binding, i+1, dialog);
				}
			});
		}

		if (i == 0) {
			binding.previous.setVisibility(View.GONE);
		} else {
			binding.previous.setVisibility(View.VISIBLE);

			binding.previous.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					images.get(i).image = DBImage.db.getImageByName(images.get(i).image.image);
					notifyItemChanged(i);
					setImageDialogContent(view, binding, i-1, dialog);
				}
			});
		}

		return dialog;
	}

	private int nrSelections() {
		return (int)images.stream().filter(image -> image.selected).count();
	}

	@Override
	public int getItemCount() {
		return images.size();
	}

	public DBImage[] getSelected() {
		return images.stream().filter(image -> image.selected).map(image -> image.image).toArray(DBImage[]::new);
	}

	public void notifySelectedRemovedImage() {

		List<DBImage> selections = List.of(getSelected());
		List<ListImageItem> copyImages = new ArrayList<>(this.images);
		for (int i = copyImages.size()-1; i >= 0; i--) {
			if (selections.contains(copyImages.get(i).image)) {
				this.images.remove(i);
				notifyItemRemoved(i);
			}
		}
		onSelectionChange.accept(0);
	}

	public void notifySelectedRemovedTags() {
		List<DBImage> selections = List.of(getSelected());
		List<ListImageItem> copyImages = new ArrayList<>(this.images);
		for (int i = copyImages.size()-1; i >= 0; i--) {
			if (selections.contains(copyImages.get(i).image)) {
				this.images.get(i).selected = false;
				this.images.get(i).image.tags = new String[0];
				notifyItemChanged(i);
			}
		}
		onSelectionChange.accept(0);
	}

	public void clearSelections() {
		List<DBImage> selections = List.of(getSelected());
		List<ListImageItem> copyImages = new ArrayList<>(this.images);
		for (int i = copyImages.size()-1; i >= 0; i--) {
			if (selections.contains(copyImages.get(i).image)) {
				this.images.get(i).selected = false;
				notifyItemChanged(i);
			}
		}
		onSelectionChange.accept(0);
	}

	public void notifyItemAdded(DBImage dbImage) {
		images.add(0, new ListImageItem(dbImage));
		notifyItemInserted(0);
	}

	private class ListImageItem {
		public DBImage image;
		public boolean selected;

		public ListImageItem(DBImage image) {
			this.image = image;
			this.selected = false;
		}
	}
}

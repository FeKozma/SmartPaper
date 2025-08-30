package com.fekozma.wallpaperchanger.lists.tags;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.util.ContextUtil;

public class TagsListHolder extends RecyclerView.ViewHolder {

	private final View add;
	private final TextView text;
	private final RelativeLayout relativeLayout;
	private final ImageView icon;
	private final CardView background;

	public TagsListHolder(@NonNull View itemView) {
		super(itemView);
		add = itemView.findViewById(R.id.tag_list_item_add);
		text = itemView.findViewById(R.id.tag_list_item_text);
		relativeLayout = itemView.findViewById(R.id.tag_list_item_rel_lay);
		icon = itemView.findViewById(R.id.tag_list_item_icon);
		background = itemView.findViewById(R.id.tag_list_item_background);
	}

	public void setTag(String tag) {
		text.setText(tag);
		setIcon(R.drawable.tag_24dp);
		setAdd(false, null, false);
	}

	public void setIcon(@DrawableRes int id) {
		icon.setImageResource(id);
	}

	public void onClicklistener(View.OnClickListener listener) {
		this.itemView.setOnClickListener(listener);
	}

	public void setBackground(int color) {
		background.setCardBackgroundColor(ContextUtil.getContext().getColor(color));
	}

	public void setAdd(boolean add, View.OnClickListener listener, boolean withText) {
		this.itemView.setOnClickListener(listener);
		if (withText) {
			this.add.setVisibility(View.GONE);
			this.relativeLayout.setVisibility(View.VISIBLE);
			setIcon(R.drawable.edit_24dp);
			text.setText("Click here to manage tags");
			setBackground(R.color.selected);
		} else {
			if (add) {
				this.add.setVisibility(View.VISIBLE);
				this.relativeLayout.setVisibility(View.GONE);
				setBackground(R.color.selected);
			} else {
				this.add.setVisibility(View.GONE);
				this.relativeLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setNumber() {
	}
}

package com.fekozma.wallpaperchanger.lists.tags;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.StaticValues;
import com.google.android.flexbox.*;

public class EditTagsDialogViewPage extends Fragment {
	private final StaticValues values;
	private final DBImage[] image;

	public EditTagsDialogViewPage(StaticValues values, DBImage[] image) {
		super(R.layout.edit_tags_dialog_list);
		this.values = values;
		this.image = image;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Find the RecyclerView
		RecyclerView recyclerView = view.findViewById(R.id.edit_tags_dialog_list);

		FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());

		layoutManager.setFlexDirection(FlexDirection.ROW);
		layoutManager.setFlexWrap(FlexWrap.WRAP);
		layoutManager.setAlignItems(AlignItems.STRETCH);
		layoutManager.setJustifyContent(JustifyContent.FLEX_START);

		// Set up RecyclerView
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(new EditTagsListAdapter(values, image));
	}
}

package com.fekozma.wallpaperchanger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.databinding.EditTagsBinding;
import com.fekozma.wallpaperchanger.lists.images.ImageTagEditorListAdapter;
import com.fekozma.wallpaperchanger.lists.tags.TagsListAdapter;
import com.google.android.flexbox.*;

import java.util.List;

public class EditTagsFragment extends Fragment {

	public static final String ARG_IMAGES = "images";
	DBImage[] images;
	private EditTagsBinding binding;

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {

		Bundle bundle = getArguments();
		images = bundle.getParcelable(ARG_IMAGES, DBImage[].class);
		if (images == null) {
			images = new DBImage[0];

		} else {
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "-> Editing " + images.length + " images");
		}
		binding = EditTagsBinding.inflate(inflater, container, false);
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		if (images.length == 0) {
			NavController navcontroller = Navigation.findNavController(requireView());
			navcontroller.navigate(R.id.action_to_homescreen);
		}
		super.onViewCreated(view, savedInstanceState);

		binding.editTagsImageList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

		binding.editTagsImageList.setAdapter(new ImageTagEditorListAdapter(List.of(images)));


		FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());

		layoutManager.setFlexDirection(FlexDirection.ROW);
		layoutManager.setFlexWrap(FlexWrap.WRAP);
		layoutManager.setAlignItems(AlignItems.STRETCH);
		layoutManager.setJustifyContent(JustifyContent.FLEX_START);
		binding.editTagsList.setLayoutManager(layoutManager);

		binding.editTagsList.setAdapter(new TagsListAdapter(images, getActivity()));

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
